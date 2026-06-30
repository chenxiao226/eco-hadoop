package org.hit.monitor.controller;

import org.apache.commons.collections4.CollectionUtils;
/**
 * 2017-07-28
 * I/O有关的参数获取
 */
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

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/io")

public class IOController extends BaseController {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	MetricsService metricsService;

	@ResponseBody
	@RequestMapping("/fetchDiskTotalAndFree")
	public String fetchDiskTotalAndFree() {
		try {
			QueryMetricsBO filter = new QueryMetricsBO();
			filter.setDesc(true);
			filter.setLimit(1); // 只取最新的一条数据

			List<MetricsDataDO> DISK_TOTAL = metricsService.fetchData(Metrics.DISK.DISK_TOTAL, filter).getModule();

			List<MetricsDataDO> DISK_FREE = metricsService.fetchData(Metrics.DISK.DISK_FREE, filter).getModule();

			Map<String, Object> result = new HashMap<String, Object>();
			if (CollectionUtils.isNotEmpty(DISK_TOTAL)) {
				result.put("diskTotal", DISK_TOTAL.get(0));
			}
			if (CollectionUtils.isNotEmpty(DISK_FREE)) {
				result.put("diskFree", DISK_FREE.get(0));
			}
			return responseControllerResultSuccess(result);
		} catch (Exception e) {
			log.error("获取CPU静态数据出错", e);
			return responseControllerResultError("参数错误");
		}
	}

	@ResponseBody
	@RequestMapping("/fetchIOByte_Pkts")
	public String fetchIOByte_Pkts(HttpServletRequest request) {
		try {
			Integer limit = 1;
			String limitStr = request.getParameter("limit");
			
			String bytesinLastFetchTimeStr = request.getParameter("bytesinLastFetchTime");
			String bytesoutLastFetchTimeStr = request.getParameter("bytesoutLastFetchTime");
			String pktsinLastFetchTimeStr = request.getParameter("pktsinLastFetchTime");
			String pktsoutLastFetchTimeStr = request.getParameter("pktsoutLastFetchTime");

			Long bytesinLastFetchTime = null;
			Long bytesoutLastFetchTime = null;
			Long pktsinLastFetchTime = null;
			Long pktsoutLastFetchTime = null;

			if (limitStr != null) {
				limit = Integer.parseInt(limitStr);
			}

			if (bytesinLastFetchTimeStr != null) {
				bytesinLastFetchTime = Long.parseLong(bytesinLastFetchTimeStr);
			}
			if (bytesoutLastFetchTimeStr != null) {
				bytesoutLastFetchTime = Long.parseLong(bytesoutLastFetchTimeStr);
			}
			if (pktsinLastFetchTimeStr != null) {
				pktsinLastFetchTime = Long.parseLong(pktsinLastFetchTimeStr);
			}
			if (pktsoutLastFetchTimeStr != null) {
				pktsoutLastFetchTime = Long.parseLong(pktsoutLastFetchTimeStr);
			}

			QueryMetricsBO filter = new QueryMetricsBO();
			filter.setDesc(true);
			filter.setLimit(limit);

			filter.setStart(bytesinLastFetchTime);
			List<MetricsDataDO> BYTES_IN = metricsService.fetchData(Metrics.NETWORK.BYTES_IN, filter).getModule();

			filter.setStart(bytesoutLastFetchTime);
			List<MetricsDataDO> BYTES_OUT = metricsService.fetchData(Metrics.NETWORK.BYTES_OUT, filter).getModule();

			filter.setStart(pktsinLastFetchTime);
			List<MetricsDataDO> PKTS_IN = metricsService.fetchData(Metrics.NETWORK.PKTS_IN, filter).getModule();

			filter.setStart(pktsoutLastFetchTime);
			List<MetricsDataDO> PKTS_OUT = metricsService.fetchData(Metrics.NETWORK.PKTS_OUT, filter).getModule();

			Map<String, Object> result = new HashMap<String, Object>();
			if (CollectionUtils.isNotEmpty(BYTES_IN)) {
				result.put("BYTES_IN", BYTES_IN);
			}
			if (CollectionUtils.isNotEmpty(BYTES_OUT)) {
				result.put("BYTES_OUT", BYTES_OUT);
			}
			if (CollectionUtils.isNotEmpty(PKTS_IN)) {
				result.put("PKTS_IN", PKTS_IN);
			}
			if (CollectionUtils.isNotEmpty(PKTS_OUT)) {
				result.put("PKTS_OUT", PKTS_OUT);
			}
			return responseControllerResultSuccess(result);
		} catch (Exception e) {
			log.error("获取内存动态数据出错", e);
			return responseControllerResultError("参数错误");
		}
	}

	@ResponseBody
	@RequestMapping("/fetchDiskIO")
	public String fetchDiskIO(HttpServletRequest request) {
		try {
			Integer limit = 1;
			String limitStr = request.getParameter("limit");
			String readLastFetchTimeStr = request.getParameter("readLastFetchTime");
			String writeLastFetchTimeStr = request.getParameter("writeLastFetchTime");

			Long readLastFetchTime = null;
			Long writeLastFetchTime = null;

			if (limitStr != null) {
				limit = Integer.parseInt(limitStr);
			}
			if (readLastFetchTimeStr != null) {
				readLastFetchTime = Long.parseLong(readLastFetchTimeStr);
			}
			if (writeLastFetchTimeStr != null) {
				writeLastFetchTime = Long.parseLong(writeLastFetchTimeStr);
			}

			QueryMetricsBO filter = new QueryMetricsBO();
			filter.setDesc(true);
			filter.setLimit(limit);

			filter.setStart(readLastFetchTime);
			List<MetricsDataDO> DISK_READ = metricsService.fetchData(Metrics.DISK.DISK_READ_BYTES, filter).getModule();

			filter.setStart(writeLastFetchTime);
			List<MetricsDataDO> DISK_WRITE = metricsService.fetchData(Metrics.DISK.DISK_WRITE_BYTES, filter).getModule();

			Map<String, Object> result = new HashMap<String, Object>();
			if (CollectionUtils.isNotEmpty(DISK_READ)) {
				result.put("DISK_READ", DISK_READ);
			}
			if (CollectionUtils.isNotEmpty(DISK_WRITE)) {
				result.put("DISK_WRITE", DISK_WRITE);
			}
			return responseControllerResultSuccess(result);
		} catch (Exception e) {
			log.error("获取磁盘IO数据出错", e);
			return responseControllerResultError("参数错误");
		}
	}
}
