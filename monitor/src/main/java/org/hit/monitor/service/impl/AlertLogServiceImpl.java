package org.hit.monitor.service.impl;

import org.apache.log4j.Logger;
import org.hit.monitor.bo.QueryAlertLogBO;
import org.hit.monitor.common.BaseResultDTO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.dao.AlertLogDao;
import org.hit.monitor.model.AlertLogDO;
import org.hit.monitor.service.AlertLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertLogServiceImpl implements AlertLogService {

	private Logger log = Logger.getLogger(this.getClass());

	private final AlertLogDao alertLogDao;
	
	@Autowired
	public AlertLogServiceImpl(AlertLogDao alertLogDao) {
		this.alertLogDao = alertLogDao;
	}
	
	@Override
	public ResultDTO<AlertLogDO> queryAlertLogById(Long id) {
	
		ResultDTO<AlertLogDO> result = new ResultDTO<AlertLogDO>();
		
		try {
			AlertLogDO alertLog = alertLogDao.selectAlertLogById(id);
			result.setModule(alertLog);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("根据主键查询失败");
			log.error("AlertLogServiceImpl queryAlertLogById error", e);
		}
		return result;
	}
	
	@Override
	public ResultDTO<AlertLogDO> queryOne(QueryAlertLogBO query) {
	
		ResultDTO<AlertLogDO> result = new ResultDTO<AlertLogDO>();
		
		try {
			AlertLogDO alertLog = alertLogDao.selectOne(query);
			result.setModule(alertLog);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("查询失败");
			log.error("AlertLogServiceImpl queryAlertLogById error", e);
		}
		return result;
	}

	@Override
	public BatchResultDTO<AlertLogDO> queryAlertLogList(QueryAlertLogBO query) {
	
		BatchResultDTO<AlertLogDO> result = new BatchResultDTO<AlertLogDO>();
		
		try {
			List<AlertLogDO> list = alertLogDao.selectAlertLogList(query);
			result.setModule(list);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("查询列表失败");
			log.error("AlertLogServiceImpl queryAlertLogList error", e);
		}
		return result;
	}
	
	@Override
	public BatchResultDTO<AlertLogDO> queryAlertLogPage(QueryAlertLogBO query) {
	
		BatchResultDTO<AlertLogDO> result = new BatchResultDTO<AlertLogDO>();
		
		try {
			int count = alertLogDao.selectAlertLogCount(query);
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
			
			List<AlertLogDO> list = alertLogDao.selectAlertLogList(query);
			result.setModule(list);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("分页查询失败");
			log.error("AlertLogServiceImpl queryAlertLogPage error", e);
		}
		return result;
	}

	@Override
	public BaseResultDTO createAlertLog(AlertLogDO alertLog) {
	
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			if(alertLog == null){
				return result.returnError("参数错误");
			}
			if(alertLog.getOccurTime() == null){
				alertLog.setOccurTime(System.currentTimeMillis());
			}
			alertLogDao.insertAlertLog(alertLog);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("添加失败");
			log.error("AlertLogServiceImpl createAlertLog error", e);
		}
		return result;
	}

	@Override
	public BaseResultDTO modifyAlertLog(AlertLogDO alertLog) {
	
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			//只修改设定值得字段，属于选择性修改
			alertLogDao.updateAlertLogSelective(alertLog);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("修改失败");
			log.error("AlertLogServiceImpl modifyAlertLog error", e);
		}
		return result;
	}

	@Override
	public BaseResultDTO modifyAlertLogCompletely(AlertLogDO alertLog) {
	
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			//不断字段是否非空，都进行修改，属于完全修改
			alertLogDao.updateAlertLog(alertLog);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("修改失败");
			log.error("AlertLogServiceImpl modifyAlertLogCompletely error", e);
		}
		return result;
	}

	@Override
	public BaseResultDTO removeAlertLog(AlertLogDO alertLog) {
		return null;
	}
}