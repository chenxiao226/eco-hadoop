package org.hit.monitor.dao;

import org.hit.monitor.bo.QueryAlertLogBO;
import org.hit.monitor.model.AlertLogDO;

import java.util.List;

public interface AlertLogDao {

	/** 根据主键查询 **/
	public AlertLogDO selectAlertLogById(Long id);
	
	/** 根据主键查询是否存在 **/
	public int selectAlertLogCountById(Long id);

	/** 查询一个，必须传入能唯一确定一个的参数 **/
	public AlertLogDO selectOne(QueryAlertLogBO query);
	
	/** 查询列表，一次最多查出1000条 **/
	public List<AlertLogDO> selectAlertLogList(QueryAlertLogBO query);
	
	/** 分页查找 **/
	public List<AlertLogDO> selectAlertLogPage(QueryAlertLogBO query);
	
	/** 分页计数 **/
	public int selectAlertLogCount(QueryAlertLogBO query);

	/** 添加 **/
	public int insertAlertLog(AlertLogDO alertLog);

	/** 完全修改 **/
	public int updateAlertLog(AlertLogDO alertLog);

	/** 选择性修改 **/
	public int updateAlertLogSelective(AlertLogDO alertLog);
}