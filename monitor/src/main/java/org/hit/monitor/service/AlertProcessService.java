package org.hit.monitor.service;

import org.hit.monitor.bo.QueryAlertProcessBO;
import org.hit.monitor.common.BaseResultDTO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.model.AlertProcessDO;
import org.hit.monitor.model.AlertTriggerDO;

public interface AlertProcessService {
	
	/**
	 * 根据id查询
	 */
	public ResultDTO<AlertProcessDO> queryAlertProcessByMP(String machine,String process);
	/**
	 * 查询一个，必须传入能唯一确定一个的参数
	 */
	public ResultDTO<AlertProcessDO> queryOne(QueryAlertProcessBO query);

	/**
	 * 查询列表
	 */
	public BatchResultDTO<AlertProcessDO> queryAlertProcessList(QueryAlertProcessBO query);
	
	/**
	 * 分页查询
	 */
	public BatchResultDTO<AlertProcessDO> queryAlertProcessPage(QueryAlertProcessBO query);

	/**
	 * 添加
	 */
	public BaseResultDTO createAlertProcess(AlertProcessDO alertProcess);
	
	/**
	 * 选择性修改，对于空的字段，不进行修改
	 */
	public BaseResultDTO modifyAlertProcess(AlertProcessDO alertProcess);
	
	/**
	 * 完全修改，对于空的字段，也修改
	 */
	public BaseResultDTO modifyAlertProcessCompletely(AlertProcessDO alertProcess);
	
	/**
	 * 逻辑删除
	 */
	public BaseResultDTO removeAlertProcess(AlertProcessDO alertProcess);
	
}
