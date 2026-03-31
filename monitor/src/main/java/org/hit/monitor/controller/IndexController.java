package org.hit.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.hit.monitor.bo.QueryMetricsBO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.CONFIG;
import org.hit.monitor.common.Metrics;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.model.MetricsDataDO;
import org.hit.monitor.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/index")
public class IndexController extends BaseController {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	MetricsService metricsService;

	/**
	 * 获取CPU数据
	 */
	@ResponseBody
	@RequestMapping("/cpu")
	public String fetchCpuData(HttpServletRequest request) {
		Integer limit = 1;
		Long start = null;
		String limitStr = request.getParameter("limit");
		String startStr = request.getParameter("start");
		if (limitStr != null) {
			limit = Integer.parseInt(limitStr);
		}
		if (startStr != null) {
			start = Long.parseLong(startStr);
		}
		QueryMetricsBO filter = new QueryMetricsBO();
		filter.setDesc(true);
		filter.setLimit(limit);
		filter.setStart(start);
		BatchResultDTO<MetricsDataDO> result = metricsService.fetchData(Metrics.CPU.IDLE, filter);

		if (result.isSuccess()) {
			return responseControllerResultSuccess(result.getModule());
		} else {
			return responseControllerResultError(result.getErrorDetail());
		}
	}
	/**
	 * 获取集群负载数据
	 */
	@ResponseBody
	@RequestMapping("/load")
	public String fetchClusterLoadData(HttpServletRequest request) {
		Integer limit = 1;
		Long start = null;
		String limitStr = request.getParameter("limit");
		String startStr = request.getParameter("start");
		if (limitStr != null) {
			limit = Integer.parseInt(limitStr);
		}
		if (startStr != null) {
			start = Long.parseLong(startStr);
		}
		QueryMetricsBO filter = new QueryMetricsBO();
		filter.setDesc(true);
		filter.setLimit(limit);
		filter.setStart(start);
		BatchResultDTO<MetricsDataDO> result = metricsService.fetchData(Metrics.LOAD.LOAD_ONE, filter);
		if (result.isSuccess()) {
			return responseControllerResultSuccess(result.getModule());
		} else {
			return responseControllerResultError(result.getErrorDetail());
		}
	}
	//获取功耗数据
	@ResponseBody
	@RequestMapping("/masterpower")
	public String fetchMasterPowerData(HttpServletRequest request) {
//		System.out.println("=== 请求到达 /index/masterpower ===");
//		System.out.println("请求方法: " + request.getMethod());
//		System.out.println("完整路径: " + request.getRequestURI());
		Integer limit = 1;
		Long start = null;
		String limitStr = request.getParameter("limit");
		String startStr = request.getParameter("start");
		System.out.println("limit: "+limit+"start: " +start);
		if (limitStr != null) {
			limit = Integer.parseInt(limitStr);
		}
		if (startStr != null) {
			start = Long.parseLong(startStr);
		}
		QueryMetricsBO filter = new QueryMetricsBO();
		filter.setDesc(true);
		filter.setLimit(limit);
		filter.setStart(start);
		BatchResultDTO<MetricsDataDO> result = metricsService.fetchData(Metrics.POWER.MASTER_POWER, filter);

		if (result.isSuccess()) {
			return responseControllerResultSuccess(result.getModule());
		} else {
			return responseControllerResultError(result.getErrorDetail());
		}
	}
	//获取功耗数据
	@ResponseBody
	@RequestMapping("/slave1power")
	public String fetchSlave1PowerData(HttpServletRequest request) {

		Integer limit = 1;
		Long start = null;
		String limitStr = request.getParameter("limit");
		String startStr = request.getParameter("start");
		if (limitStr != null) {
			limit = Integer.parseInt(limitStr);
		}
		if (startStr != null) {
			start = Long.parseLong(startStr);
		}
		QueryMetricsBO filter = new QueryMetricsBO();
		filter.setDesc(true);
		filter.setLimit(limit);
		filter.setStart(start);
		BatchResultDTO<MetricsDataDO> result = metricsService.fetchData(Metrics.POWER.SLAVE_ONE_POWER, filter);

		if (result.isSuccess()) {
			return responseControllerResultSuccess(result.getModule());
		} else {
			return responseControllerResultError(result.getErrorDetail());
		}
	}
	//获取功耗数据
	@ResponseBody
	@RequestMapping("/slave2power")
	public String fetchSlave2PowerData(HttpServletRequest request) {


		Integer limit = 1;
		Long start = null;
		String limitStr = request.getParameter("limit");
		String startStr = request.getParameter("start");

		if (limitStr != null) {
			limit = Integer.parseInt(limitStr);
		}
		if (startStr != null) {
			start = Long.parseLong(startStr);
		}
		QueryMetricsBO filter = new QueryMetricsBO();
		filter.setDesc(true);
		filter.setLimit(limit);
		filter.setStart(start);
		BatchResultDTO<MetricsDataDO> result = metricsService.fetchData(Metrics.POWER.SLAVE_TWO_POWER,filter);

		if (result.isSuccess()) {
			return responseControllerResultSuccess(result.getModule());
		} else {
			return responseControllerResultError(result.getErrorDetail());
		}
	}

	@ResponseBody
	@RequestMapping("/cluster/nodes")
	public String getNodes(HttpServletRequest request) {
		ResultDTO<String> yarnNodeData = metricsService.fetchMetricsJSONByAPI(CONFIG.YARN_RESOURCE_MANAGER + "/nodes");
		 if (yarnNodeData == null || !yarnNodeData.isSuccess() || yarnNodeData.getModule() == null) {
            return responseControllerResultError("YARNNodes API返回数据异常");
        }

		System.out.println("getNodes的原始相应"+yarnNodeData);
		JSONObject result = new JSONObject();
		JSONObject yarnResponse = JSON.parseObject(yarnNodeData.getModule());
		System.out.println("getNodes的转化后的相应"+yarnResponse);
		JSONArray nodes = yarnResponse.getJSONObject("nodes").getJSONArray("node");

		int totalNodes = nodes.size();
		int aliveNodes = 0;

		for (int i = 0; i < nodes.size(); i++) {
			JSONObject node = nodes.getJSONObject(i);
			if ("RUNNING".equals(node.getString("state"))) {
				aliveNodes++;
			}
		}
		// 直接使用put方法设置属性
		result.put("totalNodes", totalNodes);
		result.put("aliveNodes", aliveNodes);
		result.put("success", true);

		return responseControllerResultSuccess(result);
	}

	
	/**
	 * 获取集群运行应用数据
	 */
	@ResponseBody
	@RequestMapping("/app")
	public String fetchClusterAppData(HttpServletRequest request) {
		try {
			JSONObject result = new JSONObject();
			String mapReduceUri = CONFIG.YARN_RESOURCE_MANAGER + "/appstatistics?applicationTypes=mapreduce";
			ResultDTO<String> mapReduceAppData = metricsService.fetchMetricsJSONByAPI(mapReduceUri);
//			System.out.println("YARN API返回数据: {}" + mapReduceAppData.getModule());

		    JSONObject mapReduceAppResponse = JSON.parseObject(mapReduceAppData.getModule());
//			System.out.println("解析后的JSON{}"+ mapReduceAppResponse.toJSONString());
			JSONObject appStatInfo = mapReduceAppResponse.getJSONObject("appStatInfo");

			JSONArray statItems = appStatInfo.getJSONArray("statItem");
//			System.out.println("解析后的statItems: {}"
//				+ statItems.toJSONString());


			int finishedCount = 0;
			int runningCount = 0;
			int totalCount = 0;
			int failedCount = 0;
			int killedCount = 0;
			for (int i = 0; i < statItems.size(); i++) {
            JSONObject statItem = statItems.getJSONObject(i);

            // 防御性检查
            if (statItem == null ||
                !statItem.containsKey("type") ||
                !statItem.containsKey("state") ||
                !statItem.containsKey("count")) {
                log.warn("跳过无效的统计项: {}", statItem);
                continue;
            }

            String type = statItem.getString("type");
            String state = statItem.getString("state");
            int count = statItem.getIntValue("count");

            if ("mapreduce".equalsIgnoreCase(type)) {
                totalCount += count;

                switch (state.toUpperCase()) {
                    case "FINISHED":
                        finishedCount = count;
                        break;
                    case "RUNNING":
                        runningCount += count;
						break;

					case "ACCEPTED":
						runningCount += count;
					case "FAILED":
						failedCount = count;
						break;
						case "KILLED":
										killedCount = count;
										break;
                    default:
                        // 其他状态可以在这里处理
                        break;
                }
            }
        }

        // 6. 构建返回结果
        result.put("finishedCount", finishedCount);
        result.put("runningCount", runningCount);
        result.put("totalMapReduceCount", totalCount);
		result.put("failedCount", failedCount);
		result.put("killedCount", killedCount);

        log.info("应用统计结果 - 已完成: {}, 运行中: {}, 总数: {}",
                finishedCount, runningCount, totalCount);

        return responseControllerResultSuccess(result);

    } catch (JSONException e) {
        log.error("JSON解析异常", e);
        return responseControllerResultError("数据解析错误");
    } catch (Exception e) {
        log.error("处理应用数据时发生未知异常", e);
        return responseControllerResultError("系统内部错误");
    }
	}
	
	/**
	 * 若干非图表类信息：RPC队列时间、磁盘空闲空间、内存空闲空间
	 */
	@ResponseBody
	@RequestMapping("/mass")
	public String fetchMassiveData(HttpServletRequest request) {
		
		try {
			QueryMetricsBO filter = new QueryMetricsBO();
			filter.setDesc(true);
			filter.setLimit(1); // 只取最新的一条数据
			
			//RPC平均处理时间
			List<MetricsDataDO> rpcProcessTimeAvg = metricsService.fetchData(Metrics.RPC.RPC_PROCESSING_TIME_AVG_TIME, filter).getModule();
			
			//磁盘空闲空间
			List<MetricsDataDO> diskFree = metricsService.fetchData(Metrics.DISK.DISK_FREE, filter).getModule();
			
			//内存空闲空间
			List<MetricsDataDO> memFree = metricsService.fetchData(Metrics.MEMORY.FREE, filter).getModule();
			
			Map<String, Object> result = new HashMap<String, Object>();
			if (CollectionUtils.isNotEmpty(rpcProcessTimeAvg)) {
				result.put("rpc", rpcProcessTimeAvg.get(0));
			}
			if (CollectionUtils.isNotEmpty(diskFree)) {
				result.put("diskFree", diskFree.get(0));
			}
			if (CollectionUtils.isNotEmpty(memFree)) {
				result.put("memFree", memFree.get(0));
			}
			return responseControllerResultSuccess(result);
		} catch (Exception e) {
			log.error("获取首页数据出错", e);
			return responseControllerResultError("参数错误");
		}
	}
	
	/**
	 * 获取一些变化概率很低的信息：总磁盘空间、总内存大小
	 */
	@ResponseBody
	@RequestMapping("/stable")
	public String fetchStableData(HttpServletRequest request) {
		QueryMetricsBO filter = new QueryMetricsBO();
		filter.setDesc(true);
		filter.setLimit(1);
		List<MetricsDataDO> diskTotal = metricsService.fetchData(Metrics.DISK.DISK_TOTAL, filter).getModule();
		List<MetricsDataDO> memTotal = metricsService.fetchData(Metrics.MEMORY.TOTAL, filter).getModule();
		List<MetricsDataDO> cpuNum = metricsService.fetchData(Metrics.CPU.NUM, filter).getModule();
		
		Map<String, Object> result = new HashMap<String, Object>();
		if (CollectionUtils.isNotEmpty(diskTotal)) {
			result.put("diskTotal", diskTotal.get(0));
		}
		if (CollectionUtils.isNotEmpty(memTotal)) {
			result.put("memTotal", memTotal.get(0));
		}
		if (CollectionUtils.isNotEmpty(cpuNum)) {
			result.put("cpuNum", cpuNum.get(0));
		}
		return responseControllerResultSuccess(result);
	}
	
	/**
	 * 通过Mesos接口获取集群总从节点、存活节点等信息
	 */
	@ResponseBody
	@RequestMapping("/cluster/agent")
	public String fetchAgentData(HttpServletRequest request) {
		ResultDTO<String> result = metricsService.fetchMetricsJSONByAPI(CONFIG.MESOS_MASTER + "/metrics/snapshot");
//		System.out.println("这里是测试");
		if (result.isSuccess()) {
			return result.getModule();
		} else {
			return responseControllerResultError(result.getErrorDetail());
		}
	}
	
	/**
	 * 通过ResourceManager接口获取集群总节点、存活节点、运行中的应用等信息
	 */




	/**
	 * 获得异构资源分配信息
	 * ResultDTO<String> result = metricsService.fetchMetricsJSONByAPI("http://mesos/frameworks");
	 * if (result.isSuccess()) {
	 *     String json = result.getModule(); // 获取原始 JSON 数据
	 *     JSONObject data = JSON.parseObject(json); // 解析为 JSON
	 *     // 处理数据...
	 * } else {
	 *     System.err.println("请求失败: " + result.getMessage());
	 * }
	 */
	@ResponseBody
	@RequestMapping("/cluster/resource")
	public String fetchClusterResourceData(HttpServletRequest request) {
		try {
			//ResultDTO自定义的传输类对象，在common下，封装请求方法的返回数据，包含状态、数据、错误信息，
			ResultDTO<String> mesosFrameworks = metricsService.fetchMetricsJSONByAPI(CONFIG.MESOS_MASTER + "/frameworks");
			ResultDTO<String> marathonApps = metricsService.fetchMetricsJSONByAPI(CONFIG.MARATHON_API + "/v2/apps?label=monitor");
			
			JSONArray result = new JSONArray();//创建一个空的json数组
			//首先检查从获取mesosFrameworks数据的请求是否成功，并且返回的数据不为空。
			if (mesosFrameworks.isSuccess() && mesosFrameworks.getModule() != null) {
				//解析返回的数据 解析成jason
				//mesosFrameworksArr是从mesos API获取的响应数据，getModule()是ResultDTO定义的一个方法，返回module
				//module: API 返回的原始 JSON 字符串module (没有解析成JSon)
				JSONArray mesosFrameworksArr = JSON.parseObject(mesosFrameworks.getModule()).getJSONArray("frameworks");
				//mesosFrameworksArr解析后的jason，含有一组一组的数据
				for (int i = 0; i < mesosFrameworksArr.size(); i++) {
					JSONObject frame = mesosFrameworksArr.getJSONObject(i);
					//JSONObject是map，key-value,"marathon".equals(frame.get("name")提取key为nanme，value为marathon
					//"false".equals(frame.get("active")提取key为active为false
					if ("marathon".equals(frame.get("name").toString()) || "false".equals(frame.get("active").toString())) {
						continue;
					}
					//used_resources可能是总的jsonObject里面嵌套的一个jsonObject
					JSONObject resource = frame.getJSONObject("used_resources");
					JSONObject jo = new JSONObject();
					jo.put("name", frame.get("name"));//name是frame提取的
					jo.put("cpus", resource.get("cpus"));//以下三个是从resource提取的
					jo.put("gpus", resource.get("gpus"));
					jo.put("mem", resource.get("mem"));
					jo.put("disk", resource.get("disk"));
					result.add(jo);
				}
			}
			
			if (marathonApps.isSuccess() && marathonApps.getModule() != null) {
				JSONArray marathonAppArr = JSON.parseObject(marathonApps.getModule()).getJSONArray("apps");
				for (int i = 0; i < marathonAppArr.size(); i++) {
					JSONObject app = marathonAppArr.getJSONObject(i);
					if (app.getInteger("instances") == 0) {
						continue;
					}
					JSONObject jo = new JSONObject();
					jo.put("name", app.get("id"));
					jo.put("cpus", app.get("cpus"));
					jo.put("gpus", app.get("gpus"));
					jo.put("mem", app.get("mem"));
					jo.put("disk", app.get("disk"));
					result.add(jo);
				}
			}
			return result.toJSONString();
		} catch (Exception e) {
			log.error("服务器错误", e);
			return responseControllerResultError("服务器错误");
		}
	}
}
