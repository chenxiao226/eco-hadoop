package org.hit.monitor.dao;

import org.hit.monitor.bo.QueryMonitorItemsBO;
import org.hit.monitor.model.MonitorItemsDO;

import java.util.List;

public interface MonitorItemsDao {

	/** 查询一个，必须传入能唯一确定一个的参数 **/
	public MonitorItemsDO selectOne(QueryMonitorItemsBO query);
	
	/** 查询列表，一次最多查出1000条 **/
	public List<MonitorItemsDO> selectMonitorItemsList(QueryMonitorItemsBO query);
}