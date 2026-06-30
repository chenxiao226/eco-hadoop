package org.hit.monitor.controller;

import com.alibaba.fastjson.JSON;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.*;

/**
 * RLController - RL Optimizer API
 *
 * Endpoints:
 *   GET  /rl/tasks                          - list tasks + available lambdas
 *   POST /rl/train                          - start Python training process
 *   GET  /rl/train/status                   - training status
 *   GET  /rl/training-curves/{taskName}     - loss curve from log.txt
 *   GET  /rl/parameter-analysis/{taskName}  - energy/time from CSV
 *   GET  /rl/multi-task-comparison          - all tasks comparison
 *   GET  /rl/task/summary                   - single task final epoch summary
 */
@Controller
@RequestMapping("/rl")
public class RLController extends BaseController {

    // ── paths ────────────────────────────────────────────────────────────────
    private static final String BYH_ROOT      = "D:/Chenxiao/20260302VLDBDEMO/byh904";
    private static final String OUTPUT_DIR    = BYH_ROOT + "/byh/output";
    private static final String PARAM_SEARCH_OUTPUT = BYH_ROOT + "/byh/param_search/output";
    private static final String RUN_SCRIPT    = BYH_ROOT + "/run_param_search.py";
    private static final String PYTHON_EXE    = "D:/Anaconda3/envs/byh/python.exe";

    private static final String[] TASK_NAMES  = {
        "PageRank", "WordCount", "TeraSort", "RandomWriter", "NNBench", "PiEstimator", "Grep"
    };
    private static final Map<String, String> TASK_DIR_MAP = new HashMap<>();
    static {
        TASK_DIR_MAP.put("PageRank",     "pagerank");
        TASK_DIR_MAP.put("WordCount",    "wordcount");
        TASK_DIR_MAP.put("TeraSort",     "terasort");
        TASK_DIR_MAP.put("RandomWriter", "randomwriter");
        TASK_DIR_MAP.put("NNBench",      "nnbench");
        TASK_DIR_MAP.put("PiEstimator",  "pi");
        TASK_DIR_MAP.put("Grep",         "grep");
    }
    private static final double[] DEFAULT_LAMBDAS = {0.1, 0.5, 1.0};

    // ── training process state ───────────────────────────────────────────────
    private static final ConcurrentHashMap<String, Process>  runningProcesses = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String>   trainingStatus   = new ConcurrentHashMap<>();
    // key = taskName + "_" + lambda + "_" + nodeName

    // ── JAR submission state ─────────────────────────────────────────────────
    // stages: 0=submitting 1=dispatched 2=collecting 3=parsing 4=done
    private static final ConcurrentHashMap<String, Integer> jarStage   = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String>  jarMessage = new ConcurrentHashMap<>();
    private static final String JAR_UPLOAD_DIR = BYH_ROOT + "/uploaded_jars";

    private static final String[] JAR_STAGE_MSGS = {
        "Submitting JAR to Hadoop cluster...",
        "Job dispatched — Hadoop nodes executing...",
        "Job running — collecting execution metrics...",
        "Data collection complete. Parsing training samples...",
        "done"
    };
    private static final int[] JAR_STAGE_DELAYS_MS = { 0, 3000, 7000, 12000, 16000 };

    // ── /rl/tasks ────────────────────────────────────────────────────────────
    @ResponseBody
    @RequestMapping("/tasks")
    public String getTasks() {
        try {
            List<Map<String, Object>> tasks = new ArrayList<>();
            File outDir = new File(OUTPUT_DIR);

            for (String taskName : TASK_NAMES) {
                Set<Double> lambdaSet = new TreeSet<>();
                if (outDir.exists() && outDir.isDirectory()) {
                    for (File f : outDir.listFiles()) {
                        if (!f.getName().endsWith(".csv")) continue;
                        // pattern: PageRank_Lambda0.10_20260311_144201.csv
                        Pattern p = Pattern.compile("^" + Pattern.quote(taskName) + "_Lambda([\\d.]+)_");
                        Matcher m = p.matcher(f.getName());
                        if (m.find()) lambdaSet.add(Double.parseDouble(m.group(1)));
                    }
                }
                Map<String, Object> t = new LinkedHashMap<>();
                t.put("name", taskName);
                t.put("hasData", !lambdaSet.isEmpty());
                t.put("lambdas", lambdaSet.isEmpty()
                    ? doubleArrayToList(DEFAULT_LAMBDAS)
                    : new ArrayList<>(lambdaSet));
                tasks.add(t);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("tasks", tasks);
            return JSON.toJSONString(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ── /rl/train (POST) ─────────────────────────────────────────────────────
    @ResponseBody
    @RequestMapping("/train")
    public String startTraining(HttpServletRequest request) {
        try {
            String taskName  = request.getParameter("taskName");
            String nodeName  = request.getParameter("nodeName");
            String lambdaStr = request.getParameter("lambda");
            String epsilonStr= request.getParameter("epsilon");
            String thetaStr  = request.getParameter("theta");

            if (taskName == null || nodeName == null || lambdaStr == null) {
                return error("Missing parameters: taskName, nodeName, lambda");
            }

            // defaults if not provided
            String epsilon = (epsilonStr != null && !epsilonStr.isEmpty()) ? epsilonStr : "0.01";
            String theta   = (thetaStr   != null && !thetaStr.isEmpty())   ? thetaStr   : "5";

            String taskDir = TASK_DIR_MAP.getOrDefault(taskName, taskName.toLowerCase());
            String key = taskName + "_" + lambdaStr + "_" + nodeName;

            // check if already running
            Process existing = runningProcesses.get(key);
            if (existing != null && existing.isAlive()) {
                return error("Training already running for " + key);
            }

            String outputDir = PARAM_SEARCH_OUTPUT + "/" + taskDir + "/" + nodeName;
            String activePowerWeights = BYH_ROOT + "/byh/active_power/output/" + taskDir + "/" + nodeName + "/model_final.pth";
            String cpuIoWeights       = BYH_ROOT + "/byh/cpu_io/output/"       + taskDir + "/" + nodeName + "/model_final.pth";

            // check weights exist
            if (!new File(activePowerWeights).exists()) {
                return error("active_power model weights not found: " + activePowerWeights);
            }
            if (!new File(cpuIoWeights).exists()) {
                return error("cpu_io model weights not found: " + cpuIoWeights);
            }

            // clear old log
            File logFile = new File(outputDir + "/log.txt");
            new File(outputDir).mkdirs();
            if (logFile.exists()) logFile.delete();

            // build command: python -m byh.param_search.train_net with all args
            // epsilon and theta override default.py via extra CLI args parsed in __main__
            List<String> cmd = new ArrayList<>(Arrays.asList(
                PYTHON_EXE, "-u", "-m", "byh.param_search.train_net",
                "--output-dir",                outputDir,
                "--active_power_model-weights", activePowerWeights,
                "--cpu_io_model-weights",       cpuIoWeights,
                "--TASK_NAME",                  taskName,
                "--NODE_NAME",                  nodeName,
                "--epsilon",                    epsilon,
                "--theta",                      theta,
                "--lambda",                     lambdaStr
            ));

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(new File(BYH_ROOT));
            pb.environment().put("PYTHONIOENCODING", "utf-8");
            // pass epsilon and theta to Python via env vars (train_net.py reads these)
            pb.environment().put("RL_CONVERGENCE_THRESH", epsilon);
            pb.environment().put("RL_PATIENCE",           theta);
            pb.redirectErrorStream(true);
            pb.redirectOutput(new File(outputDir + "/stdout.log"));

            Process proc = pb.start();
            runningProcesses.put(key, proc);
            trainingStatus.put(key, "running");

            // watch process in background thread
            final String fKey = key;
            new Thread(() -> {
                try {
                    int exit = proc.waitFor();
                    trainingStatus.put(fKey, exit == 0 ? "done" : "error:" + exit);
                    runningProcesses.remove(fKey);
                } catch (InterruptedException ignored) {}
            }).start();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("key", key);
            result.put("outputDir", outputDir);
            result.put("epsilon", epsilon);
            result.put("theta", theta);
            result.put("msg", "Training started");
            return JSON.toJSONString(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    @ResponseBody
    @RequestMapping("/train-debug")
    public String getStdout(HttpServletRequest request) {
        try {
            String taskName  = request.getParameter("taskName");
            String nodeName  = request.getParameter("nodeName");
            String lambdaStr = request.getParameter("lambda");
            if (taskName == null || taskName.isEmpty()) {
                return error("taskName parameter is required");
            }
            if (nodeName == null || nodeName.isEmpty()) nodeName = "master";

            String taskDir   = TASK_DIR_MAP.getOrDefault(taskName, taskName.toLowerCase());
            String outputDir = PARAM_SEARCH_OUTPUT + "/" + taskDir + "/" + nodeName;

            File stdoutFile = new File(outputDir + "/stdout.log");
            File logFile    = new File(outputDir + "/log.txt");

            String stdoutContent = "stdout.log not found";
            String logContent    = "log.txt not found";

            if (stdoutFile.exists()) {
                stdoutContent = readFileSafe(stdoutFile, 50);
            }
            if (logFile.exists()) {
                logContent = readFileSafe(logFile, 50);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success",      true);
            result.put("stdoutExists", stdoutFile.exists());
            result.put("logExists",    logFile.exists());
            result.put("stdout",       stdoutContent);
            result.put("log",          logContent);
            result.put("outputDir",    outputDir);
            return JSON.toJSONString(result);
        } catch (Exception e) {
            return error(e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /** Read last N lines of a file, trying UTF-8 then GBK to handle mixed encodings. */
    private String readFileSafe(File file, int tailLines) {
        java.nio.charset.Charset[] charsets = {
            java.nio.charset.StandardCharsets.UTF_8,
            java.nio.charset.Charset.forName("GBK"),
            java.nio.charset.StandardCharsets.ISO_8859_1
        };
        for (java.nio.charset.Charset cs : charsets) {
            try {
                List<String> lines = Files.readAllLines(file.toPath(), cs);
                int from = Math.max(0, lines.size() - tailLines);
                return String.join("\n", lines.subList(from, lines.size()));
            } catch (Exception ignored) {}
        }
        // Last resort: read raw bytes and decode replacing bad chars
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
            java.nio.charset.CharsetDecoder decoder = java.nio.charset.StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(java.nio.charset.CodingErrorAction.REPLACE)
                    .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPLACE);
            String content = decoder.decode(java.nio.ByteBuffer.wrap(bytes)).toString();
            String[] parts = content.split("\n");
            int from = Math.max(0, parts.length - tailLines);
            StringBuilder sb = new StringBuilder();
            for (int i = from; i < parts.length; i++) {
                if (sb.length() > 0) sb.append("\n");
                sb.append(parts[i]);
            }
            return sb.toString();
        } catch (Exception e2) {
            return "[read error: " + e2.getMessage() + "]";
        }
    }

    // ── /rl/train/status ─────────────────────────────────────────────────────
    @ResponseBody
    @RequestMapping("/train/status")
    public String getTrainingStatus(HttpServletRequest request) {
        try {
            String taskName = request.getParameter("taskName");
            String nodeName = request.getParameter("nodeName");
            String lambdaStr = request.getParameter("lambda");
            String key = taskName + "_" + lambdaStr + "_" + nodeName;

            String status = trainingStatus.getOrDefault(key, "idle");
            boolean running = runningProcesses.containsKey(key) && runningProcesses.get(key).isAlive();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("key", key);
            result.put("status", running ? "running" : status);
            result.put("running", running);
            return JSON.toJSONString(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ── /rl/train/stop ───────────────────────────────────────────────────────
    @ResponseBody
    @RequestMapping("/train/stop")
    public String stopTraining(HttpServletRequest request) {
        try {
            String taskName = request.getParameter("taskName");
            String nodeName = request.getParameter("nodeName");
            String lambdaStr = request.getParameter("lambda");
            String key = taskName + "_" + lambdaStr + "_" + nodeName;

            Process proc = runningProcesses.get(key);
            if (proc != null && proc.isAlive()) {
                proc.destroyForcibly();
                trainingStatus.put(key, "stopped");
                runningProcesses.remove(key);
            }

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("msg", "Stopped");
            return JSON.toJSONString(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ── /rl/training-curves ──────────────────────────────────────────────────
    @ResponseBody
    @RequestMapping("/training-curves")
    public String getTrainingCurves(HttpServletRequest request) {
        try {
            String taskName = request.getParameter("taskName");
            String nodeName = request.getParameter("nodeName");
            if (nodeName == null) nodeName = "master";

            String taskDir = TASK_DIR_MAP.getOrDefault(taskName, taskName.toLowerCase());
            File logFile = new File(PARAM_SEARCH_OUTPUT + "/" + taskDir + "/" + nodeName + "/log.txt");

            if (!logFile.exists()) {
                return error("log.txt not found: " + logFile.getPath());
            }

            ParsedLog parsed = parseLogFile(logFile);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("taskName", taskName);
            result.put("curve", parsed.iterations);
            result.put("curveType", "iteration");
            result.put("checkpoints", parsed.checkpoints);
            result.put("converged", parsed.converged);
            result.put("totalIterations", parsed.totalIterations);
            result.put("energyOptimization", buildEnergyOpt(parsed));
            return JSON.toJSONString(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ── /rl/parameter-analysis ───────────────────────────────────────────────
    @ResponseBody
    @RequestMapping("/parameter-analysis")
    public String getParameterAnalysis(HttpServletRequest request) {
        try {
            String taskName = request.getParameter("taskName");
            String lambdaParam = request.getParameter("lambda");

            double[] lambdas = lambdaParam != null
                ? new double[]{Double.parseDouble(lambdaParam)}
                : DEFAULT_LAMBDAS;

            List<Map<String, Object>> lambdaResults = new ArrayList<>();
            for (double lam : lambdas) {
                List<File> files = findCsvFiles(taskName, lam);
                if (files.isEmpty()) continue;
                files.sort(Comparator.comparing(File::getName));
                File latest = files.get(files.size() - 1);
                List<Map<String, Object>> rows = parseCsvFile(latest);
                if (rows.isEmpty()) continue;

                Map<String, Object> first = rows.get(0);
                Map<String, Object> last  = rows.get(rows.size() - 1);

                List<Map<String, Object>> epochSeries = new ArrayList<>();
                for (Map<String, Object> row : rows) {
                    Map<String, Object> ep = new LinkedHashMap<>();
                    ep.put("epoch",           row.get("Epoch"));
                    ep.put("energy",          row.get("Best_Energy_Avg"));
                    ep.put("energyImprovement", row.get("Energy_Improvement_Percent"));
                    ep.put("runTime",         row.get("Best_RunTime_Avg"));
                    ep.put("runTimeChange",   row.get("RunTime_Change_Percent"));
                    epochSeries.add(ep);
                }

                Map<String, Object> before = new LinkedHashMap<>();
                before.put("energy",  first.get("Best_Energy_Avg"));
                before.put("runTime", first.get("Best_RunTime_Avg"));

                Map<String, Object> after = new LinkedHashMap<>();
                after.put("energy",  last.get("Best_Energy_Avg"));
                after.put("runTime", last.get("Best_RunTime_Avg"));
                after.put("energyImprovementPercent", last.get("Energy_Improvement_Percent"));
                after.put("runTimeChangePercent",     last.get("RunTime_Change_Percent"));

                Map<String, Object> lr = new LinkedHashMap<>();
                lr.put("lambda",      lam);
                lr.put("totalEpochs", rows.size() - 1);
                lr.put("before",      before);
                lr.put("after",       after);
                lr.put("epochSeries", epochSeries);
                lambdaResults.add(lr);
            }

            if (lambdaResults.isEmpty()) {
                return error("No data found for task: " + taskName);
            }

            // build chart structures
            List<String> cats = new ArrayList<>();
            List<Object> eBefore = new ArrayList<>(), eAfter = new ArrayList<>();
            List<Object> tBefore = new ArrayList<>(), tAfter = new ArrayList<>();
            List<Object> eImprove = new ArrayList<>();
            for (Map<String, Object> lr : lambdaResults) {
                cats.add("λ=" + lr.get("lambda"));
                Map<String, Object> b = (Map<String, Object>) lr.get("before");
                Map<String, Object> a = (Map<String, Object>) lr.get("after");
                eBefore.add(b.get("energy"));   eAfter.add(a.get("energy"));
                tBefore.add(b.get("runTime"));  tAfter.add(a.get("runTime"));
                eImprove.add(a.get("energyImprovementPercent"));
            }

            Map<String, Object> energyComp = new LinkedHashMap<>();
            energyComp.put("categories", cats);
            energyComp.put("before", eBefore);
            energyComp.put("after",  eAfter);
            energyComp.put("improvementPercent", eImprove);

            Map<String, Object> timeComp = new LinkedHashMap<>();
            timeComp.put("categories", cats);
            timeComp.put("before", tBefore);
            timeComp.put("after",  tAfter);

            List<Map<String, Object>> trainingCurves = new ArrayList<>();
            for (Map<String, Object> lr : lambdaResults) {
                List<Map<String, Object>> series = (List<Map<String, Object>>) lr.get("epochSeries");
                List<Object> epochs = new ArrayList<>(), energies = new ArrayList<>();
                for (Map<String, Object> ep : series) {
                    epochs.add(ep.get("epoch"));
                    energies.add(ep.get("energy"));
                }
                Map<String, Object> tc = new LinkedHashMap<>();
                tc.put("lambda", lr.get("lambda"));
                tc.put("name",   "λ=" + lr.get("lambda"));
                tc.put("epochs", epochs);
                tc.put("energy", energies);
                trainingCurves.add(tc);
            }

            Map<String, Object> charts = new LinkedHashMap<>();
            charts.put("energyComparison", energyComp);
            charts.put("timeComparison",   timeComp);
            charts.put("trainingCurves",   trainingCurves);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success",      true);
            result.put("taskName",     taskName);
            result.put("lambdas",      lambdaResults.stream().map(r -> r.get("lambda")).collect(java.util.stream.Collectors.toList()));
            result.put("detail",       lambdaResults);
            result.put("charts",       charts);
            return JSON.toJSONString(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ── /rl/multi-task-comparison ────────────────────────────────────────────
    @ResponseBody
    @RequestMapping("/multi-task-comparison")
    public String getMultiTaskComparison(HttpServletRequest request) {
        try {
            String lambdaParam = request.getParameter("lambda");
            double[] lambdas = lambdaParam != null
                ? new double[]{Double.parseDouble(lambdaParam)}
                : DEFAULT_LAMBDAS;

            List<Map<String, Object>> list = new ArrayList<>();
            for (String taskName : TASK_NAMES) {
                for (double lam : lambdas) {
                    List<File> files = findCsvFiles(taskName, lam);
                    if (files.isEmpty()) continue;
                    files.sort(Comparator.comparing(File::getName));
                    File latest = files.get(files.size() - 1);
                    List<Map<String, Object>> rows = parseCsvFile(latest);
                    if (rows.isEmpty()) continue;

                    Map<String, Object> first = rows.get(0);
                    Map<String, Object> last  = rows.get(rows.size() - 1);

                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("task_name",               taskName);
                    item.put("lambda",                  lam);
                    item.put("initialEnergy",           first.get("Best_Energy_Avg"));
                    item.put("finalEnergy",             last.get("Best_Energy_Avg"));
                    item.put("energyImprovementPercent",last.get("Energy_Improvement_Percent"));
                    item.put("initialRunTime",          first.get("Best_RunTime_Avg"));
                    item.put("finalRunTime",            last.get("Best_RunTime_Avg"));
                    item.put("runTimeChangePercent",    last.get("RunTime_Change_Percent"));
                    item.put("totalEpochs",             rows.size() - 1);
                    item.put("converged",               "True".equals(last.get("Converged")) || Boolean.TRUE.equals(last.get("Converged")));
                    list.add(item);
                }
            }

            if (list.isEmpty()) return error("No comparison data found");

            // chart data
            Set<String> taskSet = new LinkedHashSet<>();
            for (Map<String, Object> r : list) taskSet.add((String) r.get("task_name"));

            List<Object> eBefore = new ArrayList<>(), eAfter = new ArrayList<>(), eImprove = new ArrayList<>();
            for (Map<String, Object> r : list) {
                eBefore.add(r.get("initialEnergy"));
                eAfter.add(r.get("finalEnergy"));
                eImprove.add(r.get("energyImprovementPercent"));
            }

            Map<String, Object> chartData = new LinkedHashMap<>();
            chartData.put("tasks",        new ArrayList<>(taskSet));
            chartData.put("energyBefore", eBefore);
            chartData.put("energyAfter",  eAfter);
            chartData.put("improvement",  eImprove);

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success",   true);
            result.put("count",     list.size());
            result.put("list",      list);
            result.put("chartData", chartData);
            return JSON.toJSONString(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ── /rl/task/summary ─────────────────────────────────────────────────────
    @ResponseBody
    @RequestMapping("/task/summary")
    public String getTaskSummary(HttpServletRequest request) {
        try {
            String taskName  = request.getParameter("taskName");
            String lambdaStr = request.getParameter("lambda");
            if (taskName == null || lambdaStr == null) return error("Missing taskName or lambda");

            double lam = Double.parseDouble(lambdaStr);
            List<File> files = findCsvFiles(taskName, lam);
            if (files.isEmpty()) return error("No data for " + taskName + " lambda=" + lambdaStr);

            files.sort(Comparator.comparing(File::getName));
            List<Map<String, Object>> rows = parseCsvFile(files.get(files.size() - 1));
            if (rows.isEmpty()) return error("Empty CSV");

            Map<String, Object> first = rows.get(0);
            Map<String, Object> last  = rows.get(rows.size() - 1);

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("taskName",                 taskName);
            summary.put("lambda",                   lam);
            summary.put("totalEpochs",              rows.size() - 1);
            summary.put("initialEnergy",            first.get("Best_Energy_Avg"));
            summary.put("finalEnergy",              last.get("Best_Energy_Avg"));
            summary.put("energyImprovementPercent", last.get("Energy_Improvement_Percent"));
            summary.put("initialRunTime",           first.get("Best_RunTime_Avg"));
            summary.put("finalRunTime",             last.get("Best_RunTime_Avg"));
            summary.put("runTimeChangePercent",     last.get("RunTime_Change_Percent"));
            summary.put("converged",                "True".equals(last.get("Converged")) || Boolean.TRUE.equals(last.get("Converged")));
            summary.put("trainingTime",             first.get("Training_Time"));

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("summary", summary);
            return JSON.toJSONString(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private List<File> findCsvFiles(String taskName, double lambda) {
        List<File> result = new ArrayList<>();
        File dir = new File(OUTPUT_DIR);
        if (!dir.exists()) return result;
        String prefix = String.format("%s_Lambda%.2f", taskName, lambda);
        for (File f : dir.listFiles()) {
            if (f.getName().startsWith(prefix) && f.getName().endsWith(".csv"))
                result.add(f);
        }
        return result;
    }

    private List<Map<String, Object>> parseCsvFile(File file) throws IOException {
        List<Map<String, Object>> rows = new ArrayList<>();
        // Use encoding-safe reader
        String raw = readFileSafe(file, Integer.MAX_VALUE);
        String[] lines = raw.split("\n");
        if (lines.length < 2) return rows;

        String[] fullHeaders = lines[0].split(",");
        for (int i = 0; i < fullHeaders.length; i++) fullHeaders[i] = fullHeaders[i].trim();

        String[] shortFields = {"Epoch","Best_Energy_Avg","Energy_Improvement_Percent",
            "Best_RunTime_Avg","RunTime_Change_Percent","Energy_Time_Ratio",
            "No_Improve_Epochs","Converged","Lambda_Perf"};

        String taskName = null;
        for (int i = 1; i < lines.length; i++) {
            String[] vals = lines[i].split(",");
            Map<String, Object> row = new LinkedHashMap<>();
            if (vals.length >= fullHeaders.length) {
                // full row (epoch=0)
                for (int j = 0; j < fullHeaders.length; j++) {
                    String v = j < vals.length ? vals[j].trim() : "";
                    row.put(fullHeaders[j], parseVal(v));
                }
                taskName = (String) row.get("Task_Name");
            } else {
                // short row (epoch 1+)
                row.put("Task_Name", taskName);
                for (int j = 0; j < shortFields.length && j < vals.length; j++) {
                    row.put(shortFields[j], parseVal(vals[j].trim()));
                }
            }
            rows.add(row);
        }
        return rows;
    }

    private Object parseVal(String v) {
        if (v == null || v.isEmpty()) return null;
        try { return Double.parseDouble(v); } catch (NumberFormatException e) { return v; }
    }

    // ── log.txt parser ───────────────────────────────────────────────────────

    private static class ParsedLog {
        List<Map<String, Object>> iterations  = new ArrayList<>();
        List<Map<String, Object>> checkpoints = new ArrayList<>();
        boolean converged = false;
        int totalIterations = 0;
        Double initialEnergy = null;
        Double finalEnergy   = null;
    }

    private ParsedLog parseLogFile(File logFile) throws IOException {
        ParsedLog p = new ParsedLog();
        Pattern iterPat    = Pattern.compile("^iter:\\s*(\\d+),\\s*loss:\\s*([\\d.eE+\\-]+)(?:,\\s*q:\\s*([\\d.eE+\\-]+))?");
        Pattern evalPat    = Pattern.compile("mse_loss:\\s*([\\d.]+),.*relative_error:\\s*([\\d.]+)");
        Pattern baselinePat= Pattern.compile("\\[Baseline\\].*?Avg energy:\\s*([\\d.eE+\\-]+)");
        Pattern energyPat  = Pattern.compile("The best energys:\\s*tensor\\(\\[\\[([\\d.]+)");

        String raw = readFileSafe(logFile, Integer.MAX_VALUE);
        String[] lines = raw.split("\n");

        for (String line : lines) {
            String l = line.trim();
            Matcher m;

            m = iterPat.matcher(l);
            if (m.find()) {
                Map<String, Object> pt = new LinkedHashMap<>();
                pt.put("iter", Integer.parseInt(m.group(1)));
                pt.put("loss", Double.parseDouble(m.group(2)));
                pt.put("q",    m.group(3) != null ? Double.parseDouble(m.group(3)) : null);
                p.iterations.add(pt);
                p.totalIterations = Integer.parseInt(m.group(1));
                continue;
            }
            m = evalPat.matcher(l);
            if (m.find()) {
                Map<String, Object> cp = new LinkedHashMap<>();
                cp.put("mse_loss",       Double.parseDouble(m.group(1)));
                cp.put("relative_error", Double.parseDouble(m.group(2)));
                p.checkpoints.add(cp);
                continue;
            }
            // [Baseline] → initialEnergy (only set once)
            m = baselinePat.matcher(l);
            if (m.find()) {
                if (p.initialEnergy == null) p.initialEnergy = Double.parseDouble(m.group(1));
                continue;
            }
            // [Result] line: "Initial Avg: X, Optimized Avg: Y"
            Matcher resultM = Pattern.compile("\\[Result\\].*?Initial Avg:\\s*([\\d.eE+\\-]+),.*?Optimized Avg:\\s*([\\d.eE+\\-]+)").matcher(l);
            if (resultM.find()) {
                if (p.initialEnergy == null) p.initialEnergy = Double.parseDouble(resultM.group(1));
                p.finalEnergy = Double.parseDouble(resultM.group(2));
                continue;
            }
            // fallback: The best energys
            m = energyPat.matcher(l);
            if (m.find()) {
                double val = Double.parseDouble(m.group(1));
                if (p.initialEnergy == null) p.initialEnergy = val;
                p.finalEnergy = val;
                continue;
            }
            if (l.toLowerCase().contains("early stop") || l.toLowerCase().contains("converged")) {
                p.converged = true;
            }
        }
        return p;
    }

    private Map<String, Object> buildEnergyOpt(ParsedLog p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("initialEnergy", p.initialEnergy);
        m.put("finalEnergy",   p.finalEnergy);
        if (p.initialEnergy != null && p.finalEnergy != null && p.initialEnergy > 0) {
            double pct = (p.initialEnergy - p.finalEnergy) / p.initialEnergy * 100;
            m.put("improvementPercent", String.format("%.2f", pct));
        } else {
            m.put("improvementPercent", null);
        }
        return m;
    }

    private List<Double> doubleArrayToList(double[] arr) {
        List<Double> l = new ArrayList<>();
        for (double d : arr) l.add(d);
        return l;
    }

    // ── /rl/submit-jar (POST multipart) ──────────────────────────────────────
    @ResponseBody
    @RequestMapping(value = "/submit-jar", method = RequestMethod.POST)
    public String submitJar(@RequestParam(value = "jarFile", required = false) MultipartFile jarFile) {
        try {
            if (jarFile == null || jarFile.isEmpty()) {
                return error("No JAR file received");
            }
            String fileName = jarFile.getOriginalFilename();
            if (fileName == null || !fileName.endsWith(".jar")) {
                return error("File must be a .jar");
            }

            // Save file to disk
            File uploadDir = new File(JAR_UPLOAD_DIR);
            uploadDir.mkdirs();
            File dest = new File(uploadDir, fileName);
            jarFile.transferTo(dest);

            // Use filename (minus .jar) as the submission key
            final String jobKey = fileName.replaceAll("\\.jar$", "").toLowerCase();

            // Reset state
            jarStage.put(jobKey, 0);
            jarMessage.put(jobKey, JAR_STAGE_MSGS[0]);

            // Background thread walks through stages
            new Thread(() -> {
                for (int i = 1; i < JAR_STAGE_MSGS.length; i++) {
                    final int stage = i;
                    int delay = JAR_STAGE_DELAYS_MS[i] - JAR_STAGE_DELAYS_MS[i - 1];
                    try { Thread.sleep(delay); } catch (InterruptedException ignored) {}
                    jarStage.put(jobKey, stage);
                    jarMessage.put(jobKey, JAR_STAGE_MSGS[stage]);
                }
            }).start();

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("jobKey",  jobKey);
            result.put("savedAs", dest.getName());
            return JSON.toJSONString(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    // ── /rl/submit-status (GET) ───────────────────────────────────────────────
    @ResponseBody
    @RequestMapping("/submit-status")
    public String getSubmitStatus(HttpServletRequest request) {
        try {
            String jobKey = request.getParameter("jobKey");
            if (jobKey == null || jobKey.isEmpty()) {
                return error("Missing jobKey parameter");
            }
            int stage   = jarStage.getOrDefault(jobKey, -1);
            String msg  = jarMessage.getOrDefault(jobKey, "Unknown job");
            boolean done = stage >= JAR_STAGE_MSGS.length - 1;

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("success", true);
            result.put("jobKey",  jobKey);
            result.put("stage",   stage);
            result.put("message", msg);
            result.put("done",    done);
            return JSON.toJSONString(result);
        } catch (Exception e) {
            return error(e.getMessage());
        }
    }

    private String error(String msg) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("success", false);
        r.put("error", msg);
        return JSON.toJSONString(r);
    }
}
