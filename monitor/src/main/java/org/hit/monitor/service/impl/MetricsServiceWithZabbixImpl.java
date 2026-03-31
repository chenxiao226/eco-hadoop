package org.hit.monitor.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.hit.monitor.bo.QueryMetricsBO;
import org.hit.monitor.bo.QueryMetricsDefineBO;
import org.hit.monitor.bo.QueryMonitorItemsBO;
import org.hit.monitor.common.BaseResultDTO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.Metrics;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.dao.HistoryDao;
import org.hit.monitor.dao.MetricsDataDao;
import org.hit.monitor.dao.MetricsDefineDao;
import org.hit.monitor.dao.MonitorItemsDao;
import org.hit.monitor.model.HostDO;
import org.hit.monitor.model.MetricsDataDO;
import org.hit.monitor.model.MetricsDefineDO;
import org.hit.monitor.model.MonitorItemsDO;
import org.hit.monitor.service.HostsService;
import org.hit.monitor.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

// @Service("metricsServiceViaZabbix")
public class MetricsServiceWithZabbixImpl implements MetricsService {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	private MetricsDataDao metricsDataDao;
	
	@Autowired
	private HostsService hostsService;
	
	@Autowired
	private MetricsDefineDao metricsDefineDao;
	
	@Autowired
	private MonitorItemsDao monitorItemsDao;
	
	@Autowired
	private HistoryDao historyDao;
	
	private final long ONE_HOUR = 60 * 60; // 秒为单位
	
	@Override
	public ResultDTO<MetricsDefineDO> fetchMetricsDefineByName(String name) {
		return null;
	}
	
	@Override
	public BatchResultDTO<MetricsDefineDO> queryMetricsDefineList(QueryMetricsDefineBO filter) {
		return null;
	}
	
	@Override
	public BatchResultDTO<MetricsDefineDO> queryMetricsDefinePage(QueryMetricsDefineBO query) {
		return null;
	}
	
	@Override
	public BatchResultDTO<MetricsDataDO> fetchData(Metrics metrics, QueryMetricsBO filter) {
		
		// 参数校验
		if (metrics == null) {
			BatchResultDTO<MetricsDataDO> result = new BatchResultDTO<MetricsDataDO>();
			return result.returnError("参数错误");
		}
		if (filter == null) {
			filter = new QueryMetricsBO();
		}
		filter.setMetricsName(metrics.metricsName());
		// 切换成ZABBIX需要使用!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// filter.setTable(metrics.table());
		return fetchData(filter);
	}
	
	@Override
	public BatchResultDTO<MetricsDataDO> fetchTopNData(Metrics metrics, QueryMetricsBO filter) {
		
		BatchResultDTO<MetricsDataDO> result = new BatchResultDTO<MetricsDataDO>();
		
		// 参数校验
		if (metrics == null || filter == null || filter.getLimit() == null) {
			return result.returnError("参数错误");
		}
		filter.setMetricsName(metrics.metricsName());
		// 切换成ZABBIX需要使用!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		// filter.setTable(metrics.table());
		
		// 检查查询类型为集群数据聚合查询还是单节点数据查询
		if (filter.getHostId() == null) {
			
			QueryMonitorItemsBO queryItemsBO = new QueryMonitorItemsBO();
			
			// 获得主机列表
			List<HostDO> hostList = hostsService.queryHostsList();
			if (CollectionUtils.isEmpty(hostList)) {
				return result.returnError("集群中无任何节点");
			}
			
			// 设置查询的主机列表
			List<Long> hostIds = new ArrayList<Long>();
			for (HostDO host : hostList) {
				hostIds.add(host.getHostid());
			}
			
			// 设定要查询的指标名称
			queryItemsBO.setHostIds(hostIds);
			queryItemsBO.setKey(filter.getMetricsName());
			List<MonitorItemsDO> monitorItems = monitorItemsDao.selectMonitorItemsList(queryItemsBO);
			
			if (CollectionUtils.isEmpty(monitorItems)) {
				return result.returnError("无此指标");
			}
			
			Integer metricsDelay = null;
			
			// 设置查询的指标ID列表
			List<Long> monitorItemIds = new ArrayList<Long>();
			for (MonitorItemsDO itemsDO : monitorItems) {
				monitorItemIds.add(itemsDO.getItemid());
				if (metricsDelay == null) {
					metricsDelay = itemsDO.getDelay();
				}
			}
			filter.setMonitorItemIds(monitorItemIds);
			
			List<MetricsDataDO> historyClusterData = historyDao.selectClusterHistoryData(filter);
			
			ArrayList<MetricsDataDO> aggregationData = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(historyClusterData)) {
				Map<Long, List<MetricsDataDO>> dataGroupByHost = new HashMap<>();
				for (MetricsDataDO m : historyClusterData) {
					List hostItemDataList = dataGroupByHost.get(m.getItemId());
					if (hostItemDataList == null) {
						hostItemDataList = new ArrayList<MetricsDataDO>();
						dataGroupByHost.put(m.getItemId(), hostItemDataList);
					}
					hostItemDataList.add(m);
				}
				for (int i = 0; i < filter.getLimit(); i++) {
					MetricsDataDO m = new MetricsDataDO();
					m.setNum(0);
					m.setSum(0.0);
					long maxProcessTime = 0L;
					for (Map.Entry<Long, List<MetricsDataDO>> entry : dataGroupByHost.entrySet()) {
						if (i < entry.getValue().size()) {
							MetricsDataDO tempM = entry.getValue().get(i);
							m.setSum(m.getSum()+tempM.getSum());
							m.setNum(m.getNum()+1);
							if(maxProcessTime < tempM.getProcessTime()){
								maxProcessTime = tempM.getProcessTime();
							}
						}
					}
					m.setProcessTime(maxProcessTime);
					aggregationData.add(m);
				}
			}
			return result.returnSuccess(aggregationData);
		} else {
			return fetchSingleNodeData(filter);
		}
	}
	
	private BatchResultDTO<MetricsDataDO> fetchData(QueryMetricsBO filter) {
		
		BatchResultDTO<MetricsDataDO> result = new BatchResultDTO<MetricsDataDO>();

		try {
			// 如果起始时间、结束时间、查询条数限制都没有指定，则默认查询最近1小时的数据
			if (filter.getStart() == null && filter.getEnd() == null && filter.getLimit() == null) {
				long now = System.currentTimeMillis() / 1000;
				filter.setStart(now - ONE_HOUR);
				filter.setEnd(now);
			}
			
			// 检查查询类型为集群数据聚合查询还是单节点数据查询
			if (filter.getHostId() == null) {
				return fetchClusterData(filter);
			}
			return fetchSingleNodeData(filter);
		} catch (Exception e) {
			log.error("MetricsServiceImpl fetchData error", e);
			return result.returnError("查询列表失败");
		}
	}
	
	/**
	 * 获得集群数据
	 */
	private BatchResultDTO<MetricsDataDO> fetchClusterData(QueryMetricsBO filter) {
		
		BatchResultDTO<MetricsDataDO> result = new BatchResultDTO<MetricsDataDO>();
		
		if (filter == null) return result.returnError("参数错误");
		if (StringUtils.isBlank(filter.getMetricsName())) return result.returnError("参数错误，指标名不能为空");
		
		try {
			QueryMonitorItemsBO queryItemsBO = new QueryMonitorItemsBO();
			
			// 获得主机列表
			List<HostDO> hostList = hostsService.queryHostsList();
			if (CollectionUtils.isEmpty(hostList)) {
				return result.returnError("集群中无任何节点");
			}
			
			// 设置查询的主机列表
			List<Long> hostIds = new ArrayList<Long>();
			for (HostDO host : hostList) {
				hostIds.add(host.getHostid());
			}
			
			// 设定要查询的指标名称
			queryItemsBO.setHostIds(hostIds);
			queryItemsBO.setKey(filter.getMetricsName());
			List<MonitorItemsDO> monitorItems = monitorItemsDao.selectMonitorItemsList(queryItemsBO);
			
			if (CollectionUtils.isEmpty(monitorItems)) {
				return result.returnError("无此指标");
			}
			
			Integer metricsDelay = null;
			
			// 设置查询的指标ID列表
			List<Long> monitorItemIds = new ArrayList<Long>();
			for (MonitorItemsDO itemsDO : monitorItems) {
				monitorItemIds.add(itemsDO.getItemid());
				if (metricsDelay == null) {
					metricsDelay = itemsDO.getDelay();
				}
			}
			filter.setMonitorItemIds(monitorItemIds);
			
			List<MetricsDataDO> historyClusterData = historyDao.selectClusterHistoryData(filter);
			
			// /*** 不考虑时间，直接按照主机分组，分别取每组的第一个聚合，第二个聚合，依次。需要保证只要主机不宕机，对应的指标就会发出 ***/
			// if (CollectionUtils.isNotEmpty(historyClusterData)) {
			// 	Map<Long, List> dataGroupByHost = new HashMap<>();
			// 	for (MetricsDataDO m : historyClusterData) {
			// 		List hostItemDataList = dataGroupByHost.get(m.getItemId());
			// 		if (hostItemDataList == null) {
			// 			hostItemDataList = new ArrayList<MetricsDataDO>();
			// 			dataGroupByHost.put(m.getItemId(), hostItemDataList);
			// 		}
			// 		hostItemDataList.add(m);
			// 	}
			// 	ArrayList<MetricsDataDO> aggregationData = new ArrayList<>();
			// 	for (int i = 0; i < dataGroupByHost.size(); i++) {
			// 		for (Map.Entry<Long, List> entry : dataGroupByHost.entrySet()) {
			// 			if (i < entry.getValue().size()) {
			// 				entry.getValue().get(i);
			// 			}
			// 		}
			// 	}
			// } else {
			//
			// }
			
			
			/***  根据时间差间隔进行数据对齐  ***/
			// 聚合后的集群数据(聚合节点数据)
			//这里使用monitorItemIds的大小，因为有的指标只存在于部分节点上，同时，为了防止出现某一节点数据缺失导致实时数据出现(n-1)/n=0 无数据的问题，+1
			int resultSize = historyClusterData.size() / monitorItemIds.size() + 1;
			List<MetricsDataDO> aggregationData = new ArrayList<MetricsDataDO>(resultSize);
			for (int i = 0; i < resultSize; i++) {
				aggregationData.add(new MetricsDataDO());
			}

			// 寻找时间最大的一项，作为基准数据
			long maxTime = -1L;
			for (MetricsDataDO singleData : historyClusterData) {
				if (singleData.getProcessTime() > maxTime) {
					maxTime = singleData.getProcessTime();
				}
			}
			// 将数据按照和最大时间的差将数据放入对应的聚合块
			for (MetricsDataDO singleData : historyClusterData) {
				if (singleData.getSum() == null || singleData.getProcessTime() == null) {
					continue;
				}
				long processTime = singleData.getProcessTime();
				int index = (int) Math.round(1.0 * (maxTime - processTime) / metricsDelay);
				if (index < resultSize) {
					MetricsDataDO metricsAg = aggregationData.get(index);
					if (metricsAg.getNum() == null) {
						metricsAg.setNum(1);
						metricsAg.setSum(singleData.getSum());
						metricsAg.setProcessTime(maxTime - index * metricsDelay);
					} else {
						metricsAg.setNum(metricsAg.getNum() + 1);
						metricsAg.setSum(metricsAg.getSum() + singleData.getSum());
					}
				}
			}

			//处理空数据
			List<MetricsDataDO> finalResult = new ArrayList<MetricsDataDO>(aggregationData.size());
			for (int i = 0; i < aggregationData.size(); i++) {
				if (aggregationData.get(i).getNum() != null) {
					finalResult.add(aggregationData.get(i));
				}
			}
			/***  根据时间差间隔进行数据对齐  ***/
			
			//调试内容
			if (filter.getMetricsName().equals(Metrics.LOAD.LOAD_ONE.metricsName())) {
				log.info("一次查询结果开始：********************");
				for (MetricsDataDO metricsData : finalResult) {
					log.info(metricsData.getNum() + "\t" + metricsData.getSum() + "\t" + metricsData.getProcessTime());
				}
				log.info("一次查询结果结束：********************\n");
			}
			return result.returnSuccess(finalResult);
		} catch (Exception e) {
			log.error("MetricsServiceImpl fetchClusterData error", e);
			return result.returnError("查询集群聚合指标失败");
		}
	}
	
	/**
	 * 获得单节点数据
	 */
	private BatchResultDTO<MetricsDataDO> fetchSingleNodeData(QueryMetricsBO filter) {
		
		BatchResultDTO<MetricsDataDO> result = new BatchResultDTO<MetricsDataDO>();
		
		if (filter == null)
			return result.returnError("参数错误");
		if (StringUtils.isBlank(filter.getMetricsName()))
			return result.returnError("参数错误，指标名不能为空");
		if (filter.getHostId() == null)
			return result.returnError("参数错误，主机id不能为空");
		
		try {
			//根据已有的指标名和host查询监控项id
			QueryMonitorItemsBO queryItemsBO = new QueryMonitorItemsBO();
			queryItemsBO.setKey(filter.getMetricsName());
			queryItemsBO.setHostid(filter.getHostId());
			MonitorItemsDO monitorItem = monitorItemsDao.selectOne(queryItemsBO);
			filter.setMonitorItemId(monitorItem.getItemid());
			
			List<MetricsDataDO> historyClusterData = historyDao.selectSingleNodeHistoryData(filter);
			return result.returnSuccess(historyClusterData);
		} catch (Exception e) {
			log.error("MetricsServiceImpl fetchSingleNodeData error", e);
			return result.returnError("查询单节点指标失败");
		}
	}
	
	
	@Override
	public BatchResultDTO<MetricsDataDO> fetchDataPage(Metrics metrics, QueryMetricsBO filter) {
		return null;
	}
	
	@Override
	public BaseResultDTO removeMetricsData(QueryMetricsBO filter) {
		return null;
	}
	
	@Override
	public ResultDTO<String> fetchMetricsJSONByAPI(String uri) {
		
		ResultDTO<String> resultDTO = new ResultDTO<String>();
		try {
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet get = new HttpGet(uri);
			get.setHeader("Accept-Charset", "utf-8");
			get.setHeader("Accept", "application/json");
			get.setHeader("Cache-Control", "no-cache");
			HttpResponse res = httpClient.execute(get);
			System.out.println(res.getStatusLine().getStatusCode() +  "===" + "33333");
			System.out.println(HttpStatus.SC_OK + "===" + "55555");
			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(res.getEntity());// 返回json格式：
				return resultDTO.returnSuccess(result);
			} else {
				return resultDTO.returnError("获取API数据出错");
			}
		} catch (Exception e) {
			log.error("获取集群指标异常", e);
			return resultDTO.returnError("获取API数据异常");
		}
	}
}
