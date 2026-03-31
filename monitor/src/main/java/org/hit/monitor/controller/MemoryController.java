package org.hit.monitor.controller;

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

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/memory")
public class MemoryController extends BaseController {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	private final MetricsService metricsService;
	
	@Autowired
	public MemoryController(MetricsService metricsService) {
		this.metricsService = metricsService;
	}
	
	/**
	 * 获取内存监控中的基本数据(总内存和总交换空间)
	 */
	@ResponseBody
	@RequestMapping("/basic")
	public String fetchMassiveData(HttpServletRequest request) {
		
		try {
			QueryMetricsBO filter = new QueryMetricsBO();
			filter.setDesc(true);
			filter.setLimit(1); // 只取最新的一条数据
			
			//总内存
			List<MetricsDataDO> totalMem = metricsService.fetchData(Metrics.MEMORY.TOTAL, filter).getModule();
			
			//总交换空间
			List<MetricsDataDO> totalSwap = metricsService.fetchData(Metrics.MEMORY.SWAP_TOTAL, filter).getModule();
			
			//磁盘空闲空间
			List<MetricsDataDO> diskFree = metricsService.fetchData(Metrics.DISK.DISK_FREE, filter).getModule();
			
			//内存空闲空间
			List<MetricsDataDO> memFree = metricsService.fetchData(Metrics.MEMORY.FREE, filter).getModule();
			
			Map<String, Object> result = new HashMap<String, Object>();
			if (CollectionUtils.isNotEmpty(totalMem)) {
				result.put("totalMem", totalMem.get(0));
			}
			if (CollectionUtils.isNotEmpty(totalSwap)) {
				result.put("totalSwap", totalSwap.get(0));
			}
			return responseControllerResultSuccess(result);
		} catch (Exception e) {
			log.error("获取内存基本数据出错", e);
			return responseControllerResultError("参数错误");
		}
	}
	
	/**
	 * 获取集群负载数据
	 */
	@ResponseBody
	@RequestMapping("/dynamic")
	public String fetchClusterLoadData(HttpServletRequest request) {
		
		try {
			
			Integer limit = 1;
			String limitStr = request.getParameter("limit");
			String memFreeLastFetchTimeStr = request.getParameter("memFreeLastFetchTime");
			String swapFreeLastFetchTimeStr = request.getParameter("swapFreeLastFetchTime");
			String memBufferLastFetchTimeStr = request.getParameter("memBufferLastFetchTime");
			String memCacheLastFetchTimeStr = request.getParameter("memCacheLastFetchTime");
			String memSharedLastFetchTimeStr = request.getParameter("memSharedLastFetchTime");
			String diskLastFetchTimeStr = request.getParameter("diskLastFetchTime");

			Long memFreeLastFetchTime = null;
			Long swapFreeLastFetchTime = null;
			Long memBufferLastFetchTime = null;
			Long memCacheLastFetchTime = null;
			Long memSharedLastFetchTime = null;
			Long diskLastFetchTime = null;
			
			if (limitStr != null) {
				limit = Integer.parseInt(limitStr);
			}
			
			if (memFreeLastFetchTimeStr != null) {
				memFreeLastFetchTime = Long.parseLong(memFreeLastFetchTimeStr);
			}
			if (swapFreeLastFetchTimeStr != null) {
				swapFreeLastFetchTime = Long.parseLong(swapFreeLastFetchTimeStr);
			}
			if (memBufferLastFetchTimeStr != null) {
				memBufferLastFetchTime = Long.parseLong(memBufferLastFetchTimeStr);
			}
			if (memCacheLastFetchTimeStr != null) {
				memCacheLastFetchTime = Long.parseLong(memCacheLastFetchTimeStr);
			}
			if (memSharedLastFetchTimeStr != null) {
				memSharedLastFetchTime = Long.parseLong(memSharedLastFetchTimeStr);
			}
			if (diskLastFetchTimeStr != null) {
				diskLastFetchTime = Long.parseLong(diskLastFetchTimeStr);
			}
			
			QueryMetricsBO filter = new QueryMetricsBO();
			filter.setDesc(true);
			filter.setLimit(limit);
			
			filter.setStart(memFreeLastFetchTime);
			List<MetricsDataDO> memFree = metricsService.fetchData(Metrics.MEMORY.FREE, filter).getModule();
			
			filter.setStart(swapFreeLastFetchTime);
			List<MetricsDataDO> swapFree = metricsService.fetchData(Metrics.MEMORY.SWAP_FREE, filter).getModule();
			
			filter.setStart(memBufferLastFetchTime);
			List<MetricsDataDO> memBuffer = metricsService.fetchData(Metrics.MEMORY.BUFFERS, filter).getModule();
			
			filter.setStart(memCacheLastFetchTime);
			List<MetricsDataDO> memCache = metricsService.fetchData(Metrics.MEMORY.CACHED, filter).getModule();
			
			filter.setStart(memSharedLastFetchTime);
			List<MetricsDataDO> memShared = metricsService.fetchData(Metrics.MEMORY.SHARED, filter).getModule();

			filter.setStart(diskLastFetchTime);
			List<MetricsDataDO> diskFree = metricsService.fetchData(Metrics.DISK.DISK_FREE, filter).getModule();

			filter.setStart(diskLastFetchTime);
			List<MetricsDataDO> diskTotal = metricsService.fetchData(Metrics.DISK.DISK_TOTAL, filter).getModule();

			Map<String, Object> result = new HashMap<String, Object>();
			if (CollectionUtils.isNotEmpty(memFree)) {
				result.put("memFree", memFree);
			}
			if (CollectionUtils.isNotEmpty(swapFree)) {
				result.put("swapFree", swapFree);
			}
			if (CollectionUtils.isNotEmpty(memBuffer)) {
				result.put("memBuffer", memBuffer);
			}
			if (CollectionUtils.isNotEmpty(memCache)) {
				result.put("memCache", memCache);
			}
			if (CollectionUtils.isNotEmpty(memShared)) {
				result.put("memShared", memShared);
			}
			if (CollectionUtils.isNotEmpty(diskFree)) {
				result.put("diskFree", diskFree);
			}
			if (CollectionUtils.isNotEmpty(diskTotal)) {
				result.put("diskTotal", diskTotal);
			}
			return responseControllerResultSuccess(result);
		} catch (Exception e) {
			log.error("获取内存动态数据出错", e);
			return responseControllerResultError("参数错误");
		}
	}
	
}
