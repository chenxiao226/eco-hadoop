package org.hit.monitor.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
 * 2017-07-28
 * mesos 有关的controller
 */

@Controller
@RequestMapping("mesos")
public class MesosController extends BaseController {
	private Logger log = LoggerFactory.getLogger(getClass());
	
	@Autowired
	MetricsService metricsService;
	
	@ResponseBody
	@RequestMapping("/metrics/snapshot")
	public String getMetricsSnapshot() {
		String MesosUrl = CONFIG.MESOS_MASTER + "/metrics/snapshot";
		try {
			ResultDTO<String> result = metricsService.fetchMetricsJSONByAPI(MesosUrl);
			if (result.isSuccess()) {
				return responseControllerResultSuccess(result.getModule());
			} else {
				return responseControllerResultError("获取mesos快照信息失败！");
			}
			
		} catch (Exception e) {
			log.error("getMesos快照数据转换异常", e);
			return responseControllerResultError("获取mesosCPU信息失败！");
		}
	}
	
	@ResponseBody
	@RequestMapping("/frameworks")
	public String getFrameworks() {
		String MesosUrl = CONFIG.MESOS_MASTER + "/frameworks";
		try {
			ResultDTO<String> result = metricsService.fetchMetricsJSONByAPI(MesosUrl);
			if (result.isSuccess()) {
				return responseControllerResultSuccess(result.getModule());
			} else {
				return responseControllerResultError("获取mesos的frameworks信息失败！");
			}
			
		} catch (Exception e) {
			log.error("获取mesos frameworks数据转换异常", e);
			return responseControllerResultError("获取mesos的frameworks信息失败！");
		}
	}
	
	@ResponseBody
	@RequestMapping("/getFrameworksByMesos")
	public String getFrameworksByMesos() {
		try {
			ResultDTO<String> mesosFrameworks = metricsService.fetchMetricsJSONByAPI(CONFIG.MESOS_MASTER + "/frameworks");
			//Map<String, Object> resultMap = new HashMap<String, Object>();
			JSONArray result = new JSONArray();
			if (mesosFrameworks.isSuccess() && mesosFrameworks.getModule() != null) {
				JSONArray mesosFrameworksArr = JSON.parseObject(mesosFrameworks.getModule()).getJSONArray("frameworks");
				for (int i = 0; i < mesosFrameworksArr.size(); i++) {
					JSONObject frame = mesosFrameworksArr.getJSONObject(i);
					if ("false".equals(frame.get("active").toString())) { //过滤不活跃的框架
						continue;
					}
					JSONObject resource = frame.getJSONObject("resources");
					JSONArray tasks = frame.getJSONArray("tasks");
					
					JSONObject jo = new JSONObject();
					jo.put("name", frame.get("name"));
					jo.put("hostname", frame.get("hostname"));
					jo.put("user", frame.get("user"));
					jo.put("role", frame.get("role"));
					jo.put("registered_time", frame.get("registered_time"));
					jo.put("tasks", tasks.size());
					jo.put("cpus", resource.get("cpus"));
					jo.put("gpus", resource.get("gpus"));
					jo.put("mem", resource.get("mem"));
					jo.put("disk", resource.get("disk"));
					result.add(jo);
				}
			}
			//resultMap.put("activeFrameworks", result);
			return responseControllerResultSuccess(result);
		} catch (Exception e) {
			log.error("服务器错误", e);
			return responseControllerResultError("服务器错误");
		}
	}
	
	/**
	 * @return String 返回类型
	 * @method: getFrameworksByMesosAndMarathon
	 * @Description: 通过MESOS_API和 MARATHON_API共同获得运行的所有框架任务
	 */
	@ResponseBody
	@RequestMapping("/getFrameworksByMarathon")
	public String getFrameworksByMarathon() {
		
		try {
			ResultDTO<String> marathonApps = metricsService.fetchMetricsJSONByAPI(CONFIG.MARATHON_API + "/v2/apps?label=monitor");
			JSONArray resultApps = new JSONArray();
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
					jo.put("instances", app.get("instances"));
					resultApps.add(jo);
				}
			}
			//return result.toJSONString();
			return responseControllerResultSuccess(resultApps);
		} catch (Exception e) {
			log.error("服务器错误", e);
			return responseControllerResultError("服务器错误");
		}
	}
	
	/**
	 * @method: getMarathonTasksByAppId
	 * @Description: 获得一个Marathon APP的详细实例信息
	 */
	@ResponseBody
	@RequestMapping("/marathon/{appId}/tasks")
	public String getMarathonTasksByAppId(@PathVariable String appId) {
		
		try {
			ResultDTO<String> result = metricsService.fetchMetricsJSONByAPI(CONFIG.MARATHON_API + "/v2/apps/" + appId + "/tasks");
			if (result.isSuccess()) {
				return result.getModule();
			} else {
				return responseControllerResultError(result.getErrorDetail());
			}
		} catch (Exception e) {
			log.error("服务器错误", e);
			return responseControllerResultError("服务器错误");
		}
	}
}
