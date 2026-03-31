package org.hit.monitor.dao;

import org.hit.monitor.bo.QueryHistoryBO;
import org.hit.monitor.bo.QueryMetricsBO;
import org.hit.monitor.model.HistoryDO;
import org.hit.monitor.model.MetricsDataDO;

import java.util.List;

public interface HistoryDao {
	
	/** 查询一个，必须传入能唯一确定一个的参数 **/
	public HistoryDO selectOne(QueryHistoryBO query);
	
	/** 查询列表，一次最多查出1000条 **/
	public List<HistoryDO> selectHistoryList(QueryHistoryBO query);
	
	/** 查询集群历史数据，涉及到多个节点 **/
	public List<MetricsDataDO> selectClusterHistoryData(QueryMetricsBO query);
	
	/** 查询单个节点的历史数据 **/
	public List<MetricsDataDO> selectSingleNodeHistoryData(QueryMetricsBO query);
	
	/** 分页查找 **/
	public List<HistoryDO> selectHistoryPage(QueryHistoryBO query);
	
	/** 分页计数 **/
	public int selectHistoryCount(QueryHistoryBO query);
}