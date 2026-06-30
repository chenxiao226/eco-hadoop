package org.hit.monitor.controller;

/*
 * 与Yarn资源有关的Controller
 */
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hit.monitor.bo.QueryMetricsBO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.Metrics;
import org.hit.monitor.model.MetricsDataDO;
import org.hit.monitor.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import org.hit.monitor.common.CONFIG;
import org.hit.monitor.common.ResultDTO;

@Controller
@RequestMapping("/yarn")
public class YarnController extends BaseController {

	@Autowired
	MetricsService metricsService;

	@ResponseBody
	@RequestMapping("/clusterMetrics")
	public String getClusterMetrics() throws Exception {
		ResultDTO<String> result = metricsService.fetchMetricsJSONByAPI(CONFIG.YARN_RESOURCE_MANAGER + "/metrics");
		if (result.isSuccess()) {
			return result.getModule();
		} else {
			return responseControllerResultError("Failed to fetch YARN cluster metrics");
		}
	}

	@ResponseBody
	@RequestMapping("/getNodeManagerActiveAndLost")
	public String getNodeManagerActiveAndLost(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
//		System.out.println("测试yarn");
		QueryMetricsBO queryMetricsBO = new QueryMetricsBO();
		queryMetricsBO.setLimit(8);
		queryMetricsBO.setDesc(true);
		// 设置一个限制limit
		String limit = request.getParameter("limit");
		if (limit != null) {
			queryMetricsBO.setLimit(Integer.valueOf(limit));
		}

		BatchResultDTO<MetricsDataDO> result_NUM_ACTIVENMS = metricsService.fetchData(Metrics.YARN.NUM_ACTIVENMS,
				queryMetricsBO);
		BatchResultDTO<MetricsDataDO> result_NUM_LOSTNMS = metricsService.fetchData(Metrics.YARN.NUM_LOSTNMS,
				queryMetricsBO);
		System.out.println("NUM_ACTIVENMS: " + result_NUM_ACTIVENMS);
		System.out.println("NUM_LOSTNMS: " + result_NUM_LOSTNMS);

		Map<String, List<MetricsDataDO>> result = new HashMap<String, List<MetricsDataDO>>();
		result.put("NUM_ACTIVENMS", result_NUM_ACTIVENMS.getModule());
		result.put("NUM_LOSTNMS", result_NUM_LOSTNMS.getModule());

		return responseJson(result);
	}

	protected String get(HttpServletRequest request, String param) {
		String value = request.getParameter(param);
		return value != null ? value.trim() : null;
	}

}
