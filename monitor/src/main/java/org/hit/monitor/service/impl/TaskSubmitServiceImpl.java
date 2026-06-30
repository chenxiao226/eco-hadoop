package org.hit.monitor.service.impl;

import org.apache.log4j.Logger;
import org.hit.monitor.bo.QueryAlertTriggerBO;
import org.hit.monitor.common.ALERT;
import org.hit.monitor.common.BaseResultDTO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.dao.AlertTriggerDao;
import org.hit.monitor.dao.TaskSubmitDao;
import org.hit.monitor.model.AlertTriggerDO;
import org.hit.monitor.model.TaskSubmitDO;
import org.hit.monitor.service.AlertTriggerService;

import org.hit.monitor.service.TaskSubmitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskSubmitServiceImpl implements TaskSubmitService {

	private Logger log = Logger.getLogger(this.getClass());

	private final TaskSubmitDao taskSubmitDao;

	@Autowired
	public TaskSubmitServiceImpl(TaskSubmitDao taskSubmitDao) {
		this.taskSubmitDao = taskSubmitDao;
	}
	

	
	@Override
	public BaseResultDTO createTaskSubmit(TaskSubmitDO taskSubmitDO) {
		System.out.println("createTaskSubmit方法被调用");
		
		BaseResultDTO result = new BaseResultDTO();

		try {
			System.out.println("进入try代码");
			if(taskSubmitDO == null){
				return result.returnError("参数错误");
			}
			else{
				System.out.println("进入添加代码");
				System.out.println("taskSubmitDo================"+taskSubmitDO.toString());
//
				taskSubmitDao.insertTaskSubmit(taskSubmitDO);
//				System.out.println("taskSubmitDO");
			}

			return result.returnSuccess();
		} catch (Exception e) {
			log.error("TaskSubmitServiceImpl createTaskSubmit error", e);
			return result.returnError("333添加失败");
		}
	}
	

}