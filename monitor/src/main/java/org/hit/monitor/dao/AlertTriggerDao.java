package org.hit.monitor.dao;

import org.hit.monitor.bo.QueryAlertTriggerBO;
import org.hit.monitor.model.AlertTriggerDO;

import java.util.List;

public interface AlertTriggerDao {

	/** 根据主键查询 **/
	public AlertTriggerDO selectAlertTriggerById(Long id);
	
	/** 根据主键查询是否存在 **/
	public int selectAlertTriggerCountById(Long id);

	/** 查询一个，必须传入能唯一确定一个的参数 **/
	public AlertTriggerDO selectOne(QueryAlertTriggerBO query);
	
	/** 查询列表，一次最多查出1000条 **/
	public List<AlertTriggerDO> selectAlertTriggerList(QueryAlertTriggerBO query);
	
	/** 分页查找 **/
	public List<AlertTriggerDO> selectAlertTriggerPage(QueryAlertTriggerBO query);
	
	/** 分页计数 **/
	public int selectAlertTriggerCount(QueryAlertTriggerBO query);

	/** 添加 **/
	public int insertAlertTrigger(AlertTriggerDO alertTrigger);

	/** 完全修改 **/
	public int updateAlertTrigger(AlertTriggerDO alertTrigger);

	/** 选择性修改 **/
	public int updateAlertTriggerSelective(AlertTriggerDO alertTrigger);

	/** 删除 **/
	public int deleteAlertTrigger(AlertTriggerDO alertTrigger);
}