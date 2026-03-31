package org.hit.monitor.controller;

import org.hit.monitor.bo.QueryMetricsBO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.Metrics;
import org.hit.monitor.common.Metrics.NAMENODE;
import org.hit.monitor.common.TimeSeriesDTO;
import org.hit.monitor.model.MetricsDataDO;
import org.hit.monitor.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/namenode")
public class NameNodeController extends BaseController {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	MetricsService metricsService;

	@ResponseBody
	@RequestMapping("/addData")
	public String addData(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String processTime = get(request, "processTime");
		String metricFlag = get(request, "metricFlag");
		  System.out.println("收到请求: metricFlag=" + metricFlag + ", processTime=" + processTime);
		if (processTime == null)
			return null;

		MetricResponse metric = MetricResponse.getMetricResponse(metricFlag);
		QueryMetricsBO queryMetricsBO = new QueryMetricsBO();
		queryMetricsBO.setDesc(true);
		queryMetricsBO.setLimit(1);
		queryMetricsBO.setStart(Long.parseLong(processTime) + 1);
		BatchResultDTO<MetricsDataDO> resultdto = metricsService.fetchData(metric.metircs, queryMetricsBO);
		List<MetricsDataDO> data = resultdto.getModule();
		if (data == null || data.size() == 0)
			return null;
		else
			return responseJson(new TimeSeriesDTO(
					getDateFromProcessTime(data.get(0).getProcessTime()),
					new Double(data.get(0).getSum()), data.get(0).getProcessTime()));
	}

	@ResponseBody
	@RequestMapping("/addThreadsData")
	public String addThreadsData(HttpServletRequest request, HttpServletResponse response) throws Exception {

		String blocked_processTime = get(request, "blocked_processTime");
		String waiting_processTime = get(request, "waiting_processTime");
		if (blocked_processTime == null || waiting_processTime == null)
			return null;

		List<Object> result = new ArrayList<Object>();
		QueryMetricsBO queryMetricsBO = new QueryMetricsBO();
		queryMetricsBO.setDesc(true);
		queryMetricsBO.setLimit(1);
		queryMetricsBO.setStart(Long.parseLong(blocked_processTime) + 1);
		//blocked
		BatchResultDTO<MetricsDataDO> resultdto = metricsService.fetchData(NAMENODE.THREADS_BLOCKED, queryMetricsBO);
		List<MetricsDataDO> data = resultdto.getModule();
		if (data == null || data.size() == 0)
			return null;
		else
			result.add(new TimeSeriesDTO(
					getDateFromProcessTime(Long.parseLong(blocked_processTime)),
					new Double(data.get(0).getSum()), data.get(0).getProcessTime()));

		//waiting
		resultdto = metricsService.fetchData(NAMENODE.THREADS_WAITING, queryMetricsBO);
		data = resultdto.getModule();
		if (data == null || data.size() == 0)
			return null;
		else
			result.add(new TimeSeriesDTO(
					getDateFromProcessTime(Long.parseLong(waiting_processTime)),
					new Double(data.get(0).getSum()), data.get(0).getProcessTime()));

		return responseJson(result);

	}

	@ResponseBody
	@RequestMapping("/getGCTime")
	public String getGCTime(HttpServletRequest request, HttpServletResponse response) throws Exception {

		List<Object> graphs = new ArrayList<Object>();

		//遍历所有枚举
		for (MetricResponse metirc : MetricResponse.values()) {
			QueryMetricsBO queryMetricsBO = new QueryMetricsBO();
			queryMetricsBO.setLimit(8);
			queryMetricsBO.setDesc(true);
			BatchResultDTO<MetricsDataDO> resultdto = metricsService.fetchData(metirc.getMetircs(), queryMetricsBO);
			List<MetricsDataDO> records = resultdto.getModule();

			Map<String, Object> data = new HashMap<String, Object>();
			data.put("metricFlag", metirc.metricFlag);
			data.put("measurement", metirc.measurement);
			data.put("metricName", metirc.metricName);
			data.put("metricNameZH", metirc.metricNameZH);
			List<TimeSeriesDTO> list = new ArrayList<TimeSeriesDTO>();
			for (MetricsDataDO m : records) {
				if (m.getProcessTime() == null) {
					continue;
				}
				list.add(new TimeSeriesDTO(getDateFromProcessTime(m.getProcessTime()), new Double(m.getSum()), m.getProcessTime()));
			}
			data.put("timeseries", list);

			//MemHeapUsedM 纵轴设置最大值
			if ("MemHeapUsedM".equals(metirc.metricFlag)) {
				queryMetricsBO.setLimit(1);
				queryMetricsBO.setDesc(true);
				resultdto = metricsService.fetchData(NAMENODE.MEM_HEAP_MAX_M, queryMetricsBO);
				records = resultdto.getModule();
				if (records.size() != 0) {
					data.put("yMax", new Double(records.get(0).getSum()));
				}
			}

			graphs.add(data);
		}

		return responseJson(graphs);
	}

	@ResponseBody
	@RequestMapping("/dynamic")
	public String dynamic(HttpServletRequest request) {
		try {
			Integer limit = 50;
			String limitStr = request.getParameter("limit");
			if (limitStr != null) limit = Integer.parseInt(limitStr);

			Long gcTimeStart      = parseLong(request.getParameter("gcTimeStart"));
			Long heapUsedStart    = parseLong(request.getParameter("heapUsedStart"));
			Long rpcStart         = parseLong(request.getParameter("rpcStart"));
			Long hdfsUsedStart    = parseLong(request.getParameter("hdfsUsedStart"));

			QueryMetricsBO filter = new QueryMetricsBO();
			filter.setDesc(true);
			filter.setLimit(limit);

			filter.setStart(gcTimeStart);
			List<MetricsDataDO> gcTime = metricsService.fetchData(NAMENODE.GC_TIME, filter).getModule();

			filter.setStart(heapUsedStart);
			List<MetricsDataDO> heapUsed = metricsService.fetchData(NAMENODE.MEM_HEAP_USED_M, filter).getModule();

			filter.setStart(rpcStart);
			List<MetricsDataDO> rpcTime = metricsService.fetchData(Metrics.RPC.RPC_PROCESSING_TIME_AVG_TIME, filter).getModule();

			filter.setStart(hdfsUsedStart);
			List<MetricsDataDO> hdfsUsed = metricsService.fetchData(Metrics.DISK.DISK_FREE, filter).getModule();

			Map<String, Object> result = new HashMap<String, Object>();
			result.put("gcTime",   gcTime);
			result.put("heapUsed", heapUsed);
			result.put("rpcTime",  rpcTime);
			result.put("hdfsUsed", hdfsUsed);

			// info-box latest values
			QueryMetricsBO sq = new QueryMetricsBO();
			sq.setDesc(true); sq.setLimit(1);
			try { result.put("capacityTotal",     metricsService.fetchData(Metrics.DISK.DISK_TOTAL, sq).getModule().get(0).getSum()); } catch(Exception e2){}
			try { result.put("capacityUsed",      metricsService.fetchData(Metrics.DISK.DISK_TOTAL, sq).getModule().get(0).getSum() - metricsService.fetchData(Metrics.DISK.DISK_FREE, sq).getModule().get(0).getSum()); } catch(Exception e2){}
			try { result.put("capacityRemaining", metricsService.fetchData(Metrics.DISK.DISK_FREE,  sq).getModule().get(0).getSum()); } catch(Exception e2){}

			return responseControllerResultSuccess(result);
		} catch (Exception e) {
			log.error("namenode dynamic error", e);
			return responseControllerResultError("error");
		}
	}

	private Long parseLong(String s) {
		if (s == null) return null;
		try { return Long.parseLong(s); } catch(Exception e) { return null; }
	}
	public String getSquares(HttpServletRequest request, HttpServletResponse response) throws Exception {
		//读取TotalFiles,TotalBlocks,CorruptBlocks,MissingBlocks,PercentUsed
		//数据格式规范
		Map<String, String> map = new HashMap<String, String>();

		try {
			//TotalFiles
			QueryMetricsBO queryMetricsBO = new QueryMetricsBO();
			queryMetricsBO.setDesc(true);
			queryMetricsBO.setLimit(1);
			DecimalFormat df_int = new DecimalFormat("#");
			BatchResultDTO<MetricsDataDO> resultdto = metricsService.fetchData(NAMENODE.TOTAL_FILES, queryMetricsBO);
			map.put("TotalFiles", df_int.format(new Double(resultdto.getModule().get(0).getSum())));

			//TotalBlocks
			resultdto = metricsService.fetchData(NAMENODE.BLOCKS_TOTAL, queryMetricsBO);
			map.put("TotalBlocks", df_int.format(new Double(resultdto.getModule().get(0).getSum())));

			//CorruptBlocks
			resultdto = metricsService.fetchData(NAMENODE.CORRUPT_BLOCKS, queryMetricsBO);
			map.put("CorruptBlocks", df_int.format(new Double(resultdto.getModule().get(0).getSum())));

			//MissingBlocks
			resultdto = metricsService.fetchData(NAMENODE.MISSING_BLOCKS, queryMetricsBO);
			map.put("MissingBlocks", df_int.format(new Double(resultdto.getModule().get(0).getSum())));

			//PercentUsed
			resultdto = metricsService.fetchData(NAMENODE.CAPACITY_TOTAL, queryMetricsBO);
			Double CapacityTotal = resultdto.getModule().get(0).getSum();
			resultdto = metricsService.fetchData(NAMENODE.CAPACITY_USED, queryMetricsBO);
			Double CapacityUsed = resultdto.getModule().get(0).getSum();
			DecimalFormat df = new DecimalFormat("#.00");
			String PercentUsed = df.format(new Double(CapacityUsed) / new Double(CapacityTotal) * 100);
			map.put("PercentUsed", PercentUsed);
			return responseJson(map);
		}catch (Exception e) {
    // 方式1: 打印完整堆栈信息
    e.printStackTrace();

    // 方式2: 打印异常基本信息
    System.out.println("异常信息: " + e.getMessage());

    // 方式3: 打印详细信息
    System.out.println("异常类型: " + e.getClass().getName());
    System.out.println("异常消息: " + e.getMessage());

    log.error("获取雷达图数据错误", e);
    return responseControllerResultError("获取雷达图数据错误");
}
	}
	
	protected String getDateFromProcessTime(Long processTime) {
		String date = new java.text.SimpleDateFormat("HH:mm:ss")
				.format(new java.util.Date(processTime * 1000));
		return date;
	}
	
	protected String get(HttpServletRequest request, String param) {
		String value = request.getParameter(param);
		return value != null ? value.trim() : null;
	}
	
	enum MetricResponse {
		
		GC_TIME(NAMENODE.GC_TIME, "ms", "NameNodeGC Time", "垃圾回收时间", "GCTime"),
		GC_COUNT(NAMENODE.GC_COUNT, "次数", "NameNodeGC Count", "垃圾回收次数", "GCCount"),
		MEM_HEAP_USED_M(NAMENODE.MEM_HEAP_USED_M, "MB", "MemHeapUsedM", "堆内存使用", "MemHeapUsedM"),
		THREADS_BLOCKED(NAMENODE.THREADS_BLOCKED, "个", "ThreadsBlocked", "线程阻塞", "THREADS_BLOCKED"),
		THREADS_WAITING(NAMENODE.THREADS_WAITING, "个", "ThreadsWaiting", "线程阻塞", "THREADS_WAITING");
		
		private Metrics metircs;
		private String measurement;// 单位
		private String metricName;
		private String metricNameZH;
		private String metricFlag;
		
		private MetricResponse(Metrics metircs, String measurement, String metricName, String metricNameZH, String metricFlag) {
			this.metircs = metircs;
			this.measurement = measurement;
			this.metricName = metricName;
			this.metricNameZH = metricNameZH;
			this.metricFlag = metricFlag;
		}
		
		
		public String getMetricFlag() {
			return metricFlag;
		}
		
		
		public void setMetricFlag(String metricFlag) {
			this.metricFlag = metricFlag;
		}
		
		
		public Metrics getMetircs() {
			return metircs;
		}
		
		
		public void setMetircs(Metrics metircs) {
			this.metircs = metircs;
		}
		
		
		public String getMeasurement() {
			return measurement;
		}
		
		public void setMeasurement(String measurement) {
			this.measurement = measurement;
		}
		
		public String getMetricName() {
			return metricName;
		}
		
		public void setMetricName(String metricName) {
			this.metricName = metricName;
		}
		
		public String getMetricNameZH() {
			return metricNameZH;
		}
		
		public void setMetricNameZH(String metricNameZH) {
			this.metricNameZH = metricNameZH;
		}
		
		
		public static MetricResponse getMetricResponse(String metricFlag) {
			switch (metricFlag) {
				case "GCTime":
					return GC_TIME;
				case "GCCount":
					return GC_COUNT;
				case "MemHeapUsedM":
					return MEM_HEAP_USED_M;
				case "THREADS_BLOCKED":
					return THREADS_BLOCKED;
				case "THREADS_WAITING":
					return THREADS_WAITING;
				default:
					return GC_TIME;
			}
		}
		
	}
	
}
