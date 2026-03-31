package org.hit.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hit.monitor.bo.QueryMetricsBO;
import org.hit.monitor.common.CONFIG;
import org.hit.monitor.common.Metrics;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.model.HostDO;
import org.hit.monitor.model.MetricsDataDO;
import org.hit.monitor.service.HostsService;
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

/**
 * 节点相关信息
 */
@Controller
@RequestMapping("/node")
public class NodeController extends BaseController {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	MetricsService metricsService;
	
	@Autowired
	HostsService hostsService;
	
	/**
	 * 获取节点列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	public String fetchNodeList() {
		
		ResultDTO<String> result = metricsService.fetchMetricsJSONByAPI(CONFIG.MESOS_MASTER + "/slaves");
		if (result.isSuccess()) {
			
			//节点数组
			JSONArray nodes = (JSONArray) (JSON.parseObject(result.getModule()).get("slaves"));
			
			//添加节点类型信息
			for (Object jo : nodes) {
				((JSONObject) jo).put("nodeType", "从节点");
			}
			return nodes.toJSONString();
		} else {
			return responseControllerResultError(result.getErrorDetail());
		}
	}
	
	/**
	 * 获取主节点信息
	 */
	@ResponseBody
	@RequestMapping("/master")
	public String fetchMasterInfo() {
		
		ResultDTO<String> result = metricsService.fetchMetricsJSONByAPI(CONFIG.MESOS_MASTER + "/system/stats.json");
		if (result.isSuccess()) {
			JSONObject data = JSON.parseObject(result.getModule());
			data.put("masterHost", CONFIG.MASTER_HOST_NAME);
			return data.toJSONString();
		} else {
			return responseControllerResultError(result.getErrorDetail());
		}
	}
	
	/**
	 * 通过Ganlgia接口获取某一节点的指标信息
	 */
	@ResponseBody
	@RequestMapping("/detail")
	public String fetchNodeDetail(HttpServletRequest request) {
		
		// 或者需要查询的主机名
		String hostName = request.getParameter("host");
		if (StringUtils.isBlank(hostName)) {
			return responseControllerResultError("参数错误");
		}
		
		
		/**************    Ganglia方案   *************/
		
		// String[] param = new String[]{
		//         "host=" + hostName,
		//         "metric=cpu_speed",
		//         "metric=cpu_idle",
		//         "metric=disk_free",
		//         "metric=disk_total",
		//         "metric=mem_free",
		//         "metric=mem_total",
		//         "metric=mem_buffers",
		//         "metric=mem_cached",
		//         "metric=mem_shared",
		//         "metric=load_one",
		//         "metric=load_five"
		// };
		// String paramStr = StringUtils.join(param, '&');
		// String uri = CONFIG.GANGLIA + "?" + paramStr;
		// ResultDTO<String> result = metricsService.fetchMetricsJSONByAPI(uri);
		// if (result.isSuccess()) {
		// 	return result.getModule();
		// } else {
		// 	return responseControllerResultError(result.getErrorDetail());
		// }
		
		/**************    Mesos方案   *************/
		
		ResultDTO<String> result = metricsService.fetchMetricsJSONByAPI("http://" + hostName + ":" + CONFIG.MESOS_AGENT_PORT + "/metrics/snapshot");
		if (result.isSuccess()) {
			return result.getModule();
		} else {
			return responseControllerResultError(result.getErrorDetail());
		}
		
		/**************    Zabbix方案   *************/
		
		// //查询host的id
		// HostDO hostDO = hostsService.queryHostByName(hostName);
		// if(hostDO == null){
		// 	return responseControllerResultError("参数错误");
		// }
		//
		// QueryMetricsBO filter = new QueryMetricsBO();
		// filter.setDesc(true);
		// filter.setLimit(1); // 只取最新的一条数据
		// filter.setHostId(hostDO.getHostid());
		//
		// List<MetricsDataDO> cpuSpeed = metricsService.fetchData(Metrics.CPU.SPEED, filter).getModule();
		// List<MetricsDataDO> cpuIdle = metricsService.fetchData(Metrics.CPU.IDLE, filter).getModule();
		// List<MetricsDataDO> diskFree = metricsService.fetchData(Metrics.DISK.DISK_FREE, filter).getModule();
		// List<MetricsDataDO> diskTotal = metricsService.fetchData(Metrics.DISK.DISK_TOTAL, filter).getModule();
		// List<MetricsDataDO> memFree = metricsService.fetchData(Metrics.MEMORY.FREE, filter).getModule();
		// List<MetricsDataDO> memTotal = metricsService.fetchData(Metrics.MEMORY.TOTAL, filter).getModule();
		// List<MetricsDataDO> memBuffers = metricsService.fetchData(Metrics.MEMORY.BUFFERS, filter).getModule();
		// List<MetricsDataDO> memCached = metricsService.fetchData(Metrics.MEMORY.CACHED, filter).getModule();
		// List<MetricsDataDO> memShared = metricsService.fetchData(Metrics.MEMORY.SHARED, filter).getModule();
		// List<MetricsDataDO> loadOne = metricsService.fetchData(Metrics.LOAD.LOAD_ONE, filter).getModule();
		// List<MetricsDataDO> loadFive = metricsService.fetchData(Metrics.LOAD.LOAD_FIVE, filter).getModule();
		//
		//
		// Map<String, Object> result = new HashMap<String, Object>();
		// if (CollectionUtils.isNotEmpty(cpuSpeed)) {
		// 	result.put("cpuSpeed", cpuSpeed.get(0));
		// }
		// if (CollectionUtils.isNotEmpty(cpuIdle)) {
		// 	result.put("cpuIdle", cpuIdle.get(0));
		// }
		// if (CollectionUtils.isNotEmpty(diskFree)) {
		// 	result.put("diskFree", diskFree.get(0));
		// }
		// if (CollectionUtils.isNotEmpty(diskTotal)) {
		// 	result.put("diskTotal", diskTotal.get(0));
		// }
		// if (CollectionUtils.isNotEmpty(memFree)) {
		// 	result.put("memFree", memFree.get(0));
		// }
		// if (CollectionUtils.isNotEmpty(memTotal)) {
		// 	result.put("memTotal", memTotal.get(0));
		// }
		// if (CollectionUtils.isNotEmpty(memBuffers)) {
		// 	result.put("memBuffers", memBuffers.get(0));
		// }
		// if (CollectionUtils.isNotEmpty(memCached)) {
		// 	result.put("memCached", memCached.get(0));
		// }
		// if (CollectionUtils.isNotEmpty(memShared)) {
		// 	result.put("memShared", memShared.get(0));
		// }
		// if (CollectionUtils.isNotEmpty(loadOne)) {
		// 	result.put("loadOne", loadOne.get(0));
		// }
		// if (CollectionUtils.isNotEmpty(loadFive)) {
		// 	result.put("loadFive", loadFive.get(0));
		// }
		// return responseControllerResultSuccess(result);
	}
}
