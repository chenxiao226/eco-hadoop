package org.hit.monitor.controller;

import org.hit.monitor.bo.QueryMetricsBO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.CONFIG;
import org.hit.monitor.common.Metrics;
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
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/datanode")
public class DataNodeController extends BaseController{

	private Logger log = LoggerFactory.getLogger(getClass());
	
	static final int START_LIMIT = 20;
	@Autowired
	MetricsService metricsService;
	@ResponseBody
	@RequestMapping("/dataNodeInf")
//	這個接口根本不調用 DataNodechart.js 237
	public String dataNodeInf(HttpServletRequest request, HttpServletResponse response) throws Exception{
		System.out.println("接口dataNodeInf");
		HttpURLConnection con = (HttpURLConnection) new URL(CONFIG.HDFS_WEB + "/jmx?qry=Hadoop:service=NameNode,name=NameNodeInfo").openConnection();

		int responseCode = con.getResponseCode();
		System.out.println(responseCode);
		if(responseCode==200){
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			StringBuffer content=new StringBuffer();
			String temp;
			while((temp=in.readLine())!=null){
				content.append(temp);
				System.out.println(content);
			}
			in.close();
			return content.toString();
		}
		return null;
	}
	
	@ResponseBody
	@RequestMapping("/loadData")
	public String loadData(HttpServletRequest request, HttpServletResponse response) throws Exception{
//		datanodechart.js 35
		List<Object> graphs = new ArrayList<Object>();
		String[] metrics = request.getParameterValues("metrics[]");
		int limit = 180;
		boolean diff = false;
		if (request.getParameter("limit")!=null) limit = Integer.parseInt(request.getParameter("limit"));
		if (request.getParameter("diff")!=null && Boolean.parseBoolean(request.getParameter("diff")))  {
			limit++;
			diff=true;
		}
		if(metrics!=null){
			for (int i = 0 ; i < metrics.length ; i++) {
				MetricResponse metric = MetricResponse.getMetricResponse(metrics[i]);
				QueryMetricsBO queryMetricsBO = new QueryMetricsBO();
				queryMetricsBO.setDesc(true);
				queryMetricsBO.setLimit(limit);
				BatchResultDTO<MetricsDataDO> resultDTO = metricsService.fetchData(metric.getMetrics(), queryMetricsBO);
				List<MetricsDataDO> records = resultDTO.getModule();
				
				Map<String, Object> data = new HashMap<String, Object>();
				data.put("metricFlag", metric.metricFlag);
				List<TimeSeriesDTO> list = new ArrayList<TimeSeriesDTO>();
				if(diff){
					//System.out.println("uin");
					if(records.size()>1) {
						double previous = new Double(records.get(records.size()-1).getSum());
						for(int j=records.size()-2;j>=0;j--){
							MetricsDataDO m = records.get(j);
							list.add(new TimeSeriesDTO(getDateFromProcessTime(m.getProcessTime()), new Double(m.getSum())-previous,m.getProcessTime()));
							previous = new Double(m.getSum());
						}
						data.put("lastData",records.get(0).getSum());
					}
				}
				else 
					for(int j=records.size()-1;j>=0;j--){
						MetricsDataDO m=records.get(j);
						list.add(new TimeSeriesDTO(getDateFromProcessTime(m.getProcessTime()), new Double(m.getSum()),m.getProcessTime()));
					}

				data.put("timeseries", list);
				graphs.add(data);
			}
		}
		return responseJson(graphs);
	}
	
	@ResponseBody
	@RequestMapping("/getre")
	public String getre(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		//System.out.println("in");
		List<Object> graphs = new ArrayList<Object>();
		
		//遍历所有枚举
		for(MetricResponse metirc : MetricResponse.values()){
			QueryMetricsBO queryMetricsBO = new QueryMetricsBO();
			queryMetricsBO.setLimit(START_LIMIT);
			queryMetricsBO.setDesc(true);
			// System.out.println(metirc.getMetrics().metricsName());
			BatchResultDTO<MetricsDataDO> resultdto = metricsService.fetchData(metirc.getMetrics(), queryMetricsBO);
			List<MetricsDataDO> records = resultdto.getModule();
			
			Map<String, Object> data = new HashMap<String, Object>();
			data.put("metricFlag", metirc.metricFlag);
			List<TimeSeriesDTO> list = new ArrayList<TimeSeriesDTO>();
			for(MetricsDataDO m : records){
				list.add(new TimeSeriesDTO(getDateFromProcessTime(m.getProcessTime()), new Double(m.getSum()),m.getProcessTime()));
			}
//			System.out.println("dto:");
//			for (TimeSeriesDTO timeSeriesDTO : list) {
//				System.out.println(timeSeriesDTO.toString());
//			}
			data.put("timeseries", list);
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

			Long bytesReadStart     = parseLong(request.getParameter("bytesReadStart"));
			Long bytesWrittenStart  = parseLong(request.getParameter("bytesWrittenStart"));
			Long blocksReadStart    = parseLong(request.getParameter("blocksReadStart"));
			Long blocksWrittenStart = parseLong(request.getParameter("blocksWrittenStart"));
			Long readAvgStart       = parseLong(request.getParameter("readAvgStart"));
			Long writeAvgStart      = parseLong(request.getParameter("writeAvgStart"));

			QueryMetricsBO filter = new QueryMetricsBO();
			filter.setDesc(true);
			filter.setLimit(limit);

			filter.setStart(bytesReadStart);
			List<MetricsDataDO> bytesRead = metricsService.fetchData(Metrics.DataNode.Bytes_Read, filter).getModule();

			filter.setStart(bytesWrittenStart);
			List<MetricsDataDO> bytesWritten = metricsService.fetchData(Metrics.DataNode.Bytes_Writen, filter).getModule();

			filter.setStart(blocksReadStart);
			List<MetricsDataDO> blocksRead = metricsService.fetchData(Metrics.DataNode.Blocks_Read, filter).getModule();

			filter.setStart(blocksWrittenStart);
			List<MetricsDataDO> blocksWritten = metricsService.fetchData(Metrics.DataNode.Blocks_Written, filter).getModule();

			filter.setStart(readAvgStart);
			List<MetricsDataDO> readAvgTime = metricsService.fetchData(Metrics.DataNode.Read_Block_Op_Avg_Time, filter).getModule();

			filter.setStart(writeAvgStart);
			List<MetricsDataDO> writeAvgTime = metricsService.fetchData(Metrics.DataNode.Write_Block_Op_Avg_Time, filter).getModule();

			Map<String, Object> result = new HashMap<String, Object>();
			result.put("bytesRead",     bytesRead);
			result.put("bytesWritten",  bytesWritten);
			result.put("blocksRead",    blocksRead);
			result.put("blocksWritten", blocksWritten);
			result.put("readAvgTime",   readAvgTime);
			result.put("writeAvgTime",  writeAvgTime);
			return responseControllerResultSuccess(result);
		} catch (Exception e) {
			log.error("datanode dynamic error", e);
			return responseControllerResultError("error");
		}
	}

	private Long parseLong(String s) {
		if (s == null) return null;
		try { return Long.parseLong(s); } catch (Exception e) { return null; }
	}

	@ResponseBody
	@RequestMapping("/addData")
	public String addData(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		Map<String, Object> graphs = new HashMap<>();
		String[] metrics = request.getParameterValues("metrics[]");
		String[] processTime = request.getParameterValues("processTime[]");
		String[] lastData = new String[]{};
		boolean diff = false;
		if (request.getParameter("diff")!=null && Boolean.parseBoolean(request.getParameter("diff")))  {
			lastData = request.getParameterValues("lastData[]");
			if(metrics.length != lastData.length) return "size doesn't match";
			diff=true;
		}
		if(processTime.length==0 || processTime.length != metrics.length){
			return "size doesn't match or processTime is null";
		}
		for(int i=0;i<metrics.length;i++){
			MetricResponse metric = MetricResponse.getMetricResponse(metrics[i]);
			QueryMetricsBO queryMetricsBO = new QueryMetricsBO();
			queryMetricsBO.setDesc(true);
			//queryMetricsBO.setLimit(1);
			queryMetricsBO.setStart(Long.parseLong(processTime[i])+1);
			BatchResultDTO<MetricsDataDO> resultdto = metricsService.fetchData(metric.getMetrics(), queryMetricsBO);
			List<MetricsDataDO> data = resultdto.getModule();
			if(data==null || data.size()==0) continue;
			else{
				Map<String, Object> temp = new HashMap<>();
				List<TimeSeriesDTO> datas = new ArrayList<TimeSeriesDTO>();
				if(diff){
					double previous = new Double(lastData[i]);
					for (int j=data.size()-1;j>=0;j--) {
						Long dataTime = data.get(j).getProcessTime();
						double currentSum = new Double(data.get(j).getSum());
						datas.add(new TimeSeriesDTO(getDateFromProcessTime(dataTime),currentSum-previous,dataTime));
						previous = currentSum;
					}
				}else 
					for (int j=data.size()-1;j>=0;j--) {
						MetricsDataDO m = data.get(j);
						datas.add(new TimeSeriesDTO(getDateFromProcessTime(m.getProcessTime()),new Double(m.getSum()),m.getProcessTime()));
					}
				temp.put("addData", datas);
				temp.put("lastData", data.get(0).getSum());
				graphs.put(metrics[i], temp);
			}
		}
		return responseJson(graphs);
	}
	
	protected String getDateFromProcessTime(Long processTime){
		String date = new java.text.SimpleDateFormat("HH:mm:ss")
				.format(new java.util.Date(processTime * 1000));
		return date;
	}
	
	//其实这个controller已经不太需要这个枚举常量了 不过也可以留着
	enum MetricResponse {
		Blocks_Written(Metrics.DataNode.Blocks_Written,"BlocksWritten"),
		Blocks_Read(Metrics.DataNode.Blocks_Read,"BlocksRead"),
		Blocks_Write_Ops_Avg_Time(Metrics.DataNode.Write_Block_Op_Avg_Time,"WriteBlockOpAvgTime"),
		Blocks_Read_Ops_Avg_Time(Metrics.DataNode.Read_Block_Op_Avg_Time,"ReadBlockOpAvgTime"),
		Bytes_Read(Metrics.DataNode.Bytes_Read,"BytesRead"),
		Bytes_Writen(Metrics.DataNode.Bytes_Writen,"BytesWritten");
	     
		private Metrics metrics;
		private String metricFlag;
	     
	    private MetricResponse( Metrics metrics, String metricFlag ){
	        this.metrics = metrics;
	        this.metricFlag = metricFlag;
	    }
	     
	    
		public String getMetricFlag() {
			return metricFlag;
		}


		public void setMetricFlag(String metricFlag) {
			this.metricFlag = metricFlag;
		}


		public Metrics getMetrics() {
			return metrics;
		}


		public void setMetrics(Metrics metrics) {
			this.metrics = metrics;
		}


		
		
		public static MetricResponse getMetricResponse(String metricFlag){
			switch(metricFlag){
				case "BytesWritten":
					return Bytes_Writen;
				case "BytesRead":
					return Bytes_Read;
				case "BlocksWritten":
					return Blocks_Written;
				case "BlocksRead":
					return Blocks_Read;
				case "WriteBlockOpAvgTime":
					return Blocks_Write_Ops_Avg_Time;
				case "ReadBlockOpAvgTime":
					return Blocks_Read_Ops_Avg_Time;
				default:
					return null;
			}
		}
	 
	}
}
