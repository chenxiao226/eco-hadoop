package org.hit.monitor.service;

import org.hit.monitor.bo.QueryAlertTriggerBO;
import org.hit.monitor.common.BaseResultDTO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.model.TaskSubmitDO;

public interface TaskSubmitService {

	/**
	 * 添加
	 */
	public BaseResultDTO createTaskSubmit(TaskSubmitDO taskSubmitDO);

	
}