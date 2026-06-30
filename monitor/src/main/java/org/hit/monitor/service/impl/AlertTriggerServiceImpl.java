package org.hit.monitor.service.impl;

import org.apache.log4j.Logger;
import org.hit.monitor.bo.QueryAlertTriggerBO;
import org.hit.monitor.common.ALERT;
import org.hit.monitor.common.BaseResultDTO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.dao.AlertTriggerDao;
import org.hit.monitor.model.AlertTriggerDO;
import org.hit.monitor.service.AlertTriggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertTriggerServiceImpl implements AlertTriggerService {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private final AlertTriggerDao alertTriggerDao;
	
	@Autowired
	public AlertTriggerServiceImpl(AlertTriggerDao alertTriggerDao) {
		this.alertTriggerDao = alertTriggerDao;
	}
	
	@Override
	public ResultDTO<AlertTriggerDO> queryAlertTriggerById(Long id) {
		
		ResultDTO<AlertTriggerDO> result = new ResultDTO<AlertTriggerDO>();
		
		try {
			AlertTriggerDO alertTrigger = alertTriggerDao.selectAlertTriggerById(id);
			result.setModule(alertTrigger);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("根据主键查询失败");
			log.error("AlertTriggerServiceImpl queryAlertTriggerById error", e);
		}
		return result;
	}
	
	@Override
	public ResultDTO<AlertTriggerDO> queryOne(QueryAlertTriggerBO query) {
		
		ResultDTO<AlertTriggerDO> result = new ResultDTO<AlertTriggerDO>();
		
		try {
			AlertTriggerDO alertTrigger = alertTriggerDao.selectOne(query);
			result.setModule(alertTrigger);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("查询失败");
			log.error("AlertTriggerServiceImpl queryAlertTriggerById error", e);
		}
		return result;
	}
	
	@Override
	public BatchResultDTO<AlertTriggerDO> queryAlertTriggerList(QueryAlertTriggerBO query) {
		
		BatchResultDTO<AlertTriggerDO> result = new BatchResultDTO<AlertTriggerDO>();
		
		try {
			List<AlertTriggerDO> list = alertTriggerDao.selectAlertTriggerList(query);
			result.setModule(list);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("查询列表失败");
			log.error("AlertTriggerServiceImpl queryAlertTriggerList error", e);
		}
		return result;
	}
	
	@Override
	public BatchResultDTO<AlertTriggerDO> queryAlertTriggerPage(QueryAlertTriggerBO query) {
		
		BatchResultDTO<AlertTriggerDO> result = new BatchResultDTO<AlertTriggerDO>();
		
		try {
			int count = alertTriggerDao.selectAlertTriggerCount(query);
			query.setRecord(count);
			
			// 没数据
			if (count < 1) {
				result.setSuccess(true);
				return result;
			}
			// 查询页面超过最大页码
			if (query.getPageNo() > query.getTotalPages()) {
				result.setSuccess(true);
				return result;
			}
			
			List<AlertTriggerDO> list = alertTriggerDao.selectAlertTriggerList(query);
			result.setModule(list);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("分页查询失败");
			log.error("AlertTriggerServiceImpl queryAlertTriggerPage error", e);
		}
		return result;
	}
	
	@Override
	public BaseResultDTO createAlertTrigger(AlertTriggerDO alertTrigger) {
		
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			
			//检查是否存在相同的报警
			QueryAlertTriggerBO query = new QueryAlertTriggerBO();
			query.setTriggerId(alertTrigger.getTriggerId());
			query.setHost(alertTrigger.getHost());
			AlertTriggerDO oldAlert = alertTriggerDao.selectOne(query);
			
			if (oldAlert != null) {
				//如果收到相同状态的提醒，则不进行任何操作
				if(oldAlert.getStatus().equals(alertTrigger.getStatus())){
					;
				} else {
					if(alertTrigger.getStatus() == ALERT.TRIGGER.OK){
						//可以进行一些消除警报的内容
					} else {
						//可以进行一些警报提醒的内容
					}
					oldAlert.setStatus(alertTrigger.getStatus());
					oldAlert.setTime(System.currentTimeMillis());
					alertTriggerDao.updateAlertTrigger(oldAlert);
				}
			} else {
				alertTriggerDao.insertAlertTrigger(alertTrigger);
			}
			return result.returnSuccess();
		} catch (Exception e) {
			log.error("AlertTriggerServiceImpl createAlertTrigger error", e);
			return result.returnError("添加失败");
		}
	}
	
	@Override
	public BaseResultDTO modifyAlertTrigger(AlertTriggerDO alertTrigger) {
		
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			//只修改设定值得字段，属于选择性修改
			alertTriggerDao.updateAlertTriggerSelective(alertTrigger);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("修改失败");
			log.error("AlertTriggerServiceImpl modifyAlertTrigger error", e);
		}
		return result;
	}
	
	@Override
	public BaseResultDTO modifyAlertTriggerCompletely(AlertTriggerDO alertTrigger) {
		
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			//不断字段是否非空，都进行修改，属于完全修改
			alertTriggerDao.updateAlertTrigger(alertTrigger);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("修改失败");
			log.error("AlertTriggerServiceImpl modifyAlertTriggerCompletely error", e);
		}
		return result;
	}
	
	@Override
	public BaseResultDTO removeAlertTrigger(AlertTriggerDO alertTrigger) {
		
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			alertTriggerDao.deleteAlertTrigger(alertTrigger);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("删除失败");
			log.error("AlertTriggerServiceImpl removeAlertTrigger error", e);
		}
		return result;
	}
	
}