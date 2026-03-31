package org.hit.monitor.service;

import org.hit.monitor.bo.QueryAlertLogBO;
import org.hit.monitor.common.BaseResultDTO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.model.AlertLogDO;

public interface AlertLogService {

	/**
	 * 根据id查询
	 */
	public ResultDTO<AlertLogDO> queryAlertLogById(Long id);
	
	/**
	 * 查询一个，必须传入能唯一确定一个的参数
	 */
	public ResultDTO<AlertLogDO> queryOne(QueryAlertLogBO query);

	/**
	 * 查询列表
	 */
	public BatchResultDTO<AlertLogDO> queryAlertLogList(QueryAlertLogBO query);
	
	/**
	 * 分页查询
	 */
	public BatchResultDTO<AlertLogDO> queryAlertLogPage(QueryAlertLogBO query);

	/**
	 * 添加
	 */
	public BaseResultDTO createAlertLog(AlertLogDO alertLog);
	
	/**
	 * 选择性修改，对于空的字段，不进行修改
	 */
	public BaseResultDTO modifyAlertLog(AlertLogDO alertLog);
	
	/**
	 * 完全修改，对于空的字段，也修改
	 */
	public BaseResultDTO modifyAlertLogCompletely(AlertLogDO alertLog);
	
	/**
	 * 逻辑删除
	 */
	public BaseResultDTO removeAlertLog(AlertLogDO alertLog);
	
}