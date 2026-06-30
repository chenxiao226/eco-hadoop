package org.hit.monitor.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.hit.monitor.bo.QueryMetricsBO;
import org.hit.monitor.common.Metrics;
import org.hit.monitor.model.MetricsDataDO;
import org.hit.monitor.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/cpu")
public class  CPUController extends BaseController {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	MetricsService metricsService;


	@ResponseBody
	@RequestMapping("/fetchCPU_Num_Spped_Idle")
	/**
	 * 根据传入的cpu参数，获取响应的信息，参数有：CPU参数名(m_cpu_num)，limit(1)
	 * @param request
	 * @return
	 */
	public String fetchCPU_Num_Spped_IdleMetrics(HttpServletRequest request) {
		try {
			QueryMetricsBO filter = new QueryMetricsBO();
			filter.setDesc(true);
			filter.setLimit(1); // 只取最新的一条数据

			List<MetricsDataDO> cpuNum = metricsService.fetchData(Metrics.CPU.NUM, filter).getModule();

			List<MetricsDataDO> cpuSpeed = metricsService.fetchData(Metrics.CPU.SPEED, filter).getModule();

			List<MetricsDataDO> cpuIdle = metricsService.fetchData(Metrics.CPU.IDLE, filter).getModule();

			Map<String, Object> result = new HashMap<String, Object>();
			if (CollectionUtils.isNotEmpty(cpuNum)) {
				result.put("cpuNum", cpuNum.get(0));
			
			}
			if (CollectionUtils.isNotEmpty(cpuSpeed)) {
				result.put("cpuSpeed", cpuSpeed.get(0));
			}
			if (CollectionUtils.isNotEmpty(cpuIdle)) {
				result.put("cpuIdle", cpuIdle.get(0));
			}
			return responseControllerResultSuccess(result);
		} catch (Exception e) {
			log.error("获取CPU静态数据出错", e);
			return responseControllerResultError("参数错误");
		}
	}

	@ResponseBody
	@RequestMapping("/fetchCPU_Ratio")
	/** 根据传入的cpu参数，获取响应的信息，参数有：CPU参数名(m_cpu_num)，limit(1)， **/
	public String fetchCPU_Ratio(HttpServletRequest request) {

		Integer limit = 1; // 获取的
		String limitStr = request.getParameter("limit");
		try {
			if (limitStr != null) {
				limit = Integer.parseInt(limitStr);
			}
			QueryMetricsBO filter = new QueryMetricsBO();
			filter.setDesc(true);
			filter.setLimit(limit);
			List<MetricsDataDO> cpuIdle = metricsService.fetchData(Metrics.CPU.IDLE, filter).getModule();// 空闲比例

			List<MetricsDataDO> cpuNice = metricsService.fetchData(Metrics.CPU.NICE, filter).getModule();// 用户进程空间内改变过优先级的进程占用CPU百分比

			List<MetricsDataDO> cpuSystem = metricsService.fetchData(Metrics.CPU.SYSTEM, filter).getModule();// 内核空间所占CPU

			List<MetricsDataDO> cpuUser = metricsService.fetchData(Metrics.CPU.USER, filter).getModule();// 用户空间所占CPU

			List<MetricsDataDO> cpuWio = metricsService.fetchData(Metrics.CPU.WIO, filter).getModule();// 等待I/O所占CPU

			Map<String, Object> result = new HashMap<String, Object>();

			if (CollectionUtils.isNotEmpty(cpuIdle)) {
				result.put("cpuIdle", cpuIdle);
			}
			if (CollectionUtils.isNotEmpty(cpuNice)) {
				result.put("cpuNice", cpuNice);
			}
			if (CollectionUtils.isNotEmpty(cpuSystem)) {
				result.put("cpuSystem", cpuSystem);
			}
			if (CollectionUtils.isNotEmpty(cpuUser)) {
				result.put("cpuUser", cpuUser);
			}
			if (CollectionUtils.isNotEmpty(cpuWio)) {
				result.put("cpuWio", cpuWio);
			}
			return responseControllerResultSuccess(result);
		} catch (Exception e) {
			log.error("获取CPU静态数据出错", e);
			return responseControllerResultError("参数错误");
		}
	}
	
	@ResponseBody
	@RequestMapping("/fetchCPU_Proc")
	/**
	 * 获得与进程有关的信息
	 * @param request
	 * @return
	 */
	public String fetchCPU_Proc(HttpServletRequest request) {

		Integer limit = 1; // 获取的
		String limitStr = request.getParameter("limit");
		try {
			if (limitStr != null) {
				limit = Integer.parseInt(limitStr);
			}
			QueryMetricsBO filter = new QueryMetricsBO();
			filter.setDesc(true);
			filter.setLimit(limit);
			List<MetricsDataDO> procRun = metricsService.fetchData(Metrics.PROCESS.PROC_RUN, filter).getModule();// 正在运行的进程

			List<MetricsDataDO> procTotal = metricsService.fetchData(Metrics.PROCESS.PROC_TOTAL, filter).getModule();//总的进程

			Map<String, Object> result = new HashMap<String, Object>();

			if (CollectionUtils.isNotEmpty(procRun)) {
				result.put("procRun", procRun);
			}
			if (CollectionUtils.isNotEmpty(procTotal)) {
				result.put("procTotal", procTotal);
			}
			return responseControllerResultSuccess(result);
		} catch (Exception e) {
			log.error("获取procTotal静态数据出错", e);
			return responseControllerResultError("参数错误");
		}
	}
}
