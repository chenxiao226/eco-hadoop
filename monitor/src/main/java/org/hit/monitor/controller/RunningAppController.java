package org.hit.monitor.controller;

import org.hit.monitor.common.CONFIG;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 节点相关信息
 */
@Controller
@RequestMapping("/app")
public class RunningAppController extends BaseController {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	MetricsService metricsService;
	
	/**
	 * 获取节点列表
	 */
	@ResponseBody
	@RequestMapping("/list")
	public String fetchRunningYarnAppList() {
		
		ResultDTO<String> result = metricsService.fetchMetricsJSONByAPI(CONFIG.YARN_RESOURCE_MANAGER + "/apps");
		if (result.isSuccess()) {
			return result.getModule();
		} else {
			return responseControllerResultError("获取运行中的APP信息失败");
		}
	}
	
	/**
	 * 获取运行中的Spark应用
	 */
	@ResponseBody
	@RequestMapping("/spark")
	public String fetchRunningSparkAppList() {
		
		ResultDTO<String> result = metricsService.fetchMetricsJSONByAPI(CONFIG.SPARK_HISTORY_SERVER + "/applications?status=running");
		if (result.isSuccess()) {
			return result.getModule();
		} else {
			return responseControllerResultError(result.getErrorDetail());
		}
	}
	
	/**
	 * 获取某一Spark应用的Job列表信息
	 */
	@ResponseBody
	@RequestMapping("/spark/{appId}/jobs")
	public String fetchRunningSparkAppJobs(@PathVariable String appId) {
		
		ResultDTO<String> result = metricsService.fetchMetricsJSONByAPI(CONFIG.SPARK_HISTORY_SERVER + "/applications/"+appId+"/jobs");
		if (result.isSuccess()) {
			return result.getModule();
		} else {
			return responseControllerResultError(result.getErrorDetail());
		}
	}
}
