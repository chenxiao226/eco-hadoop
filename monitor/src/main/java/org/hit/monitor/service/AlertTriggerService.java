package org.hit.monitor.service;

import org.hit.monitor.bo.QueryAlertTriggerBO;
import org.hit.monitor.common.BaseResultDTO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.model.AlertTriggerDO;

public interface AlertTriggerService {

	/**
	 * 根据id查询
	 */
	public ResultDTO<AlertTriggerDO> queryAlertTriggerById(Long id);
	
	/**
	 * 查询一个，必须传入能唯一确定一个的参数
	 */
	public ResultDTO<AlertTriggerDO> queryOne(QueryAlertTriggerBO query);

	/**
	 * 查询列表
	 */
	public BatchResultDTO<AlertTriggerDO> queryAlertTriggerList(QueryAlertTriggerBO query);
	
	/**
	 * 分页查询
	 */
	public BatchResultDTO<AlertTriggerDO> queryAlertTriggerPage(QueryAlertTriggerBO query);

	/**
	 * 添加
	 */
	public BaseResultDTO createAlertTrigger(AlertTriggerDO alertTrigger);
	
	/**
	 * 选择性修改，对于空的字段，不进行修改
	 */
	public BaseResultDTO modifyAlertTrigger(AlertTriggerDO alertTrigger);
	
	/**
	 * 完全修改，对于空的字段，也修改
	 */
	public BaseResultDTO modifyAlertTriggerCompletely(AlertTriggerDO alertTrigger);
	
	/**
	 * 逻辑删除
	 */
	public BaseResultDTO removeAlertTrigger(AlertTriggerDO alertTrigger);
	
}