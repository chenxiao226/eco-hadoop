================================================================
  analysis.js 任务配置总结文档
  RL Optimizer Interactive Demo — 新增任务操作指南
  生成日期：2026-03-23
================================================================

一、整体架构
─────────────────────────────────────────────────────────────
五阶段流程：
  Phase 1 选任务
  Phase 2 Baseline 跑动画
  Phase 3 AI 推荐参数
  Phase 4 手动调参对比
  Phase 5 三路并行对比动画 + Final Result Chart

数据来源分层（优先级从高到低）：
  1. 真实实验数据（最权威）
     → TASKS[i].baseline.totalEnergy / runTime
     → TASKS[i].aiOpt.totalEnergy / runTime
     → 这两个数字决定所有能耗/节能%的最终显示，绝不能用API预测值替代

  2. Flask API（辅助，http://localhost:5001）
     → /api/recommend  调用 ParamEffiRainbow 模型
     → /api/predict    调用 ActivePowerModel 模型
     → 仅用于：Phase5 待机功率(IDLE)、Phase3 节能%（来自REAL_RESULTS锚点）
     → 不可用于：能耗绝对值计算（模型未校准绝对功率）

  3. 本地 curated 数据（展示用）
     → aiOpt.param1~param8 / cpuFreq / summary / reasons
     → Phase3 参数卡片和分析文字永远用本地数据，不用API返回值


二、添加新任务 —— TASKS 数组字段模板
─────────────────────────────────────────────────────────────
在 analysis.js 文件顶部 TASKS 数组中新增：

{
  id: 'wordcount',          // 必须与 app.py REAL_RESULTS 的 key 一致（小写）
  name: 'WordCount',        // 页面显示名称
  icon: '📝',               // Emoji 图标（Phase1 任务卡）
  type: 'CPU + IO Mixed',   // 类型标签（Phase1 任务卡副标题）
  desc: '一句话任务描述',

  // ── Phase 2：Baseline Run ──────────────────────────────
  // 全部来自真实实验测量值
  baseline: {
    avgPower:    32.5,    // 平均功率 (W)，用于 Phase2 动画波动基准
    totalEnergy: 9306.6,  // 总能耗 (Ws)，Phase2 结束时显示的最终值 ★最重要
    runTime:     286.3,   // 运行时长 (s) ★最重要
    cpuFreq:     800,     // 基线 CPU 频率，实验基线固定 800 MHz
    mapTasks:    60,      // 并发 Map 任务数（实验基线）
    reduceTasks: 20,      // 并发 Reduce 任务数
    cpuUtil:     0.78,    // CPU 利用率 0~1（用于 Phase5 API 调用）
    ioUtil:      0.032    // IO 利用率 0~1（同上）
  },

  // ── Phase 3 / 4 / 5：AI 推荐 ──────────────────────────
  aiOpt: {
    totalEnergy: 6767.6,  // AI 优化后总能耗 (Ws)，来自真实实验 ★最重要
    runTime:     228.6,   // AI 优化后运行时长 (s) ★最重要
    cpuFreq:     1100,    // AI 推荐 CPU 频率 (MHz)，Phase3卡片和Phase4提示

    // 以下 8 个参数仅用于 Phase3 参数卡片展示
    // 与 app.py 的 BASELINE_PARAMS 无关（模型无任务上下文，不可信）
    param1:  70,    // Map Task Slots（推荐值，与基线20对比）
    param2:  0.75,  // Reduce Slowstart
    param3:  200,   // Sort Buffer (MB)
    param4:  0.55,  // Spill Percent
    param5:  0.65,  // Merge Factor
    param6:  650,   // JVM Heap (MB)
    param7:  1,     // Native Task   (>=0.5 显示 On，否则 Off)
    param8:  0,     // Compress Output

    // Phase3 底部 Decision Rationale 区域，显示 5 条原因
    reasons: [
      { param: 'CPU Frequency',      val: '1100 MHz', unit: '',      cls: 'good',
        reason: '解释为什么推荐这个频率...' },
      { param: 'Map Parallelism',    val: '70',        unit: 'tasks', cls: 'good',
        reason: '解释 Map 并发调整原因...' },
      { param: 'Reduce Parallelism', val: '15',        unit: 'tasks', cls: 'warn',
        reason: '解释 Reduce 并发调整原因...' },
      { param: 'JVM Heap (param_6)', val: '650 MB',    unit: '',      cls: '',
        reason: '解释 Heap 调整原因...' },
      { param: 'λ=0.10 Savings',     val: '27.3',      unit: '%',     cls: 'good',
        reason: 'RL 收敛结果：能耗从 X→Y Ws (−Z%), 运行时缩短 W%.' }
    ],

    // Phase3 AI Analysis 文字（HTML 格式，支持 <strong><br>）
    // 必须包含：workload 类型、推荐 freq、节能%、运行时%
    // 永远用这里的本地数据，不用 API 返回的 reason 字段
    summary: 'Workload identified as <strong>类型</strong> (CPU util. X%, IO Y%).<br>'
      + 'ParamEffiRainbow (λ=0.10) recommends CPU frequency <strong>ZZZ MHz</strong>, '
      + 'achieving <strong>A%</strong> energy reduction and <strong>B%</strong> runtime speedup.'
  }
}


三、Phase 3（AI 推荐）关键规则
─────────────────────────────────────────────────────────────
Phase3 显示 9 个参数对比卡片，基线值固定如下（所有任务相同）：

  参数             基线值
  ─────────────────────────────
  CPU Frequency    b.cpuFreq（通常 800 MHz）
  Map Task Slots   20
  Reduce Slowstart 0.80
  Sort Buffer      200 MB
  Spill Percent    0.70
  Merge Factor     0.6
  JVM Heap         512 MB
  Native Task      On（1）
  Compress Output  On（1）

【绝对不能用】：
  × apiData.params_display —— app.py 的 BASELINE_PARAMS 对所有任务一样，
    模型无任务上下文，三个任务输出完全相同
  × apiData.reason —— 模型推荐的 freq 与本地数据可能矛盾
    （如 Sort 本地推荐 800 但模型输出 1500，会造成界面矛盾）

【必须用】：
  ✓ 参数卡片：t.aiOpt.param1~param8, t.aiOpt.cpuFreq
  ✓ Analysis 文字：t.aiOpt.summary
  ✓ 节能%：优先用 apiData.energy_saving_pct（来自 REAL_RESULTS 锚点），
            没有则本地计算 (1 - aiOpt.totalEnergy / baseline.totalEnergy) * 100


四、Phase 4（手动调参）_applyManualUI 计算逻辑
─────────────────────────────────────────────────────────────
手动能耗/时间用"频率距离插值"计算，不能用 API 预测值直接累积：

  aiFreq   = ai.cpuFreq
  freqSpan = |aiFreq - baseline.cpuFreq|

  若 freqSpan < 1（IO 密集型，AI 不改频率，如 TeraSort）：
      optScore = 0.20  （固定小分，代表仅靠参数调优的收益）

  否则（CPU/Mixed 任务）：
      dist     = |manualCpuFreq - aiFreq|
      optScore = max(0, min(1,  1 - dist / freqSpan))
      （三角核：手动=AI频率 → score=1，手动=基线频率 → score=0）

  mE = baseline.totalEnergy - optScore × (baseline.totalEnergy - ai.totalEnergy × 1.05)
  mT = baseline.runTime     - optScore × (baseline.runTime     - ai.runTime     × 1.02)

为什么不用 API：
  ActivePowerModel 在 800 MHz 时预测 ~18W，真实值 ~32.5W，
  直接累积 API 功率会在手动=基线时显示 44% 节能（应为 0%）。

新任务对应的 _localManualPrediction 权重（freqW/mapW）：
  IO 密集型（如 TeraSort）：freqW ≈ 0.22，mapW ≈ 0.28
  CPU 密集型（如 Pi）：      freqW ≈ 0.52，mapW ≈ 0.14
  Mixed（如 PageRank）：     freqW ≈ 0.40，mapW ≈ 0.18（参考值，可微调）

  代码位置：analysis.js _localManualPrediction() 函数中
  var freqW = (t.id === 'sort') ? 0.22 : 0.52;
  新任务需加一行：(t.id === 'wordcount') ? 0.40 : ...


五、Phase 5（三路对比）关键规则
─────────────────────────────────────────────────────────────
功率推导（动画中三路均从能耗/时间反推，不用 API 预测值）：

  pBase = baseline.totalEnergy / baseline.runTime
  pMan  = S._manE / S._manT        // 来自 _applyManualUI 插值结果
  pAI   = ai.totalEnergy / ai.runTime

  唯一例外：待机功率用 API 预测值
  IDLE = pIdleOverride || 15       // API 不可用时默认 15W

Summary Bar 三个数字：
  sbEDrop   = '▼ ' + aiSave%                     // AI 节能%
  sbTDelta  = '▼ ' + |ai.runTime - b.runTime| + 's'  // 时间缩短
  sbManDiff = '+ ' + (AI节能% - Manual节能%) + '%'   // Manual 比 AI 差多少

  ★ Summary Bar 数字在动画结束时才显示（与 Final Result 图同步），
    初始值设为 '--'，防止数字提前跳出来。


六、向 app.py 添加新任务（后端）
─────────────────────────────────────────────────────────────
1. REAL_RESULTS 添加真实实验值：
   "wordcount": {
       "baseline":  {"energy": 9306.6, "runtime": 286.3},
       "optimized": {"energy": 6900.0, "runtime": 240.0},
   }

2. BASELINE_UTILS 添加利用率：
   "wordcount": {"cpu": 0.75, "io": 0.05},

3. BASELINE_PARAMS 无需修改（所有任务共用相同基线参数）

4. /api/tasks 可选添加（前端不依赖此接口）：
   {"id": "wordcount", "label": "WordCount", "icon": "📝"},

5. 模型文件（可选，无则自动 fallback 到 pagerank/master）：
   byh/active_power/output/wordcount/master/model_final.pth
   byh/param_search/output/wordcount/master/model_final.pth


七、已修复的坑（不要再踩）
─────────────────────────────────────────────────────────────
坑                                  根因                         解法
─────────────────────────────────────────────────────────────
Phase3 三任务参数完全一样            用了 API params_display       改用本地 aiOpt 数据
Phase3 只显示 5 个参数              遍历 reasons(5项)而非9个定义   改为遍历固定 PARAM_DEFS
Phase3 分析文字与参数卡矛盾          用了 API reason 字段           改用本地 summary 字段
Phase4 手动800MHz显示44%节能         直接用API功率累积，模型低估功率  改用实验锚点插值
Phase4 Manual vs AI Gap 固定3.6%   Gap 用 API 返回值，对不同freq差不多  改为 (AI节能%)-(Manual节能%)
Phase5 Final Chart 能耗值偏低14     用 API 功率(~18W)×时间          改为 pBase=bE/bT 反推
Summary Bar 数字立刻跳出            setPhase(5)时就写入了文字       改为动画结束时才写入


八、文件位置
─────────────────────────────────────────────────────────────
前端演示：
  src/main/webapp/asset/module/rl/analysis.html   页面结构
  src/main/webapp/asset/module/rl/analysis.js     所有逻辑（本文档对应此文件）

后端 Flask API：
  D:\Chenxiao\20260302VLDBDEMO\byh904\flask_api\app.py       主入口
  D:\Chenxiao\20260302VLDBDEMO\byh904\flask_api\start.bat    启动脚本
  D:\Chenxiao\20260302VLDBDEMO\byh904\flask_api\requirements.txt

启动后端：
  双击 start.bat（激活 byh conda 环境，监听 localhost:5001）


================================================================
  END OF DOCUMENT
================================================================