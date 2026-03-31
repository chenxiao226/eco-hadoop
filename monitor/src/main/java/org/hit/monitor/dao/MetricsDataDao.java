package org.hit.monitor.dao;

import java.util.List;

import org.hit.monitor.bo.QueryMetricsBO;
import org.hit.monitor.model.MetricsDataDO;

public interface MetricsDataDao {

	/** 查询列表，一次最多查出10000条 **/
	public List<MetricsDataDO> selectMetricsList(QueryMetricsBO query);
	
	/** 分页查找 **/
	public List<MetricsDataDO> selectMetricsPage(QueryMetricsBO query);
	
	/** 分页计数 **/
	public int selectMetricsCount(QueryMetricsBO query);
	
	/** 按照指定条件删除数据 **/
	public int deleteMetricsData(QueryMetricsBO query);
}