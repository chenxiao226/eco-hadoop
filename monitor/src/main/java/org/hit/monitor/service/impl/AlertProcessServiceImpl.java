package org.hit.monitor.service.impl;

import org.apache.log4j.Logger;
import org.hit.monitor.bo.QueryAlertProcessBO;
import org.hit.monitor.bo.QueryAlertTriggerBO;
import org.hit.monitor.common.ALERT;
import org.hit.monitor.common.BaseResultDTO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.dao.AlertProcessDao;
import org.hit.monitor.model.AlertProcessDO;
import org.hit.monitor.model.AlertTriggerDO;
import org.hit.monitor.service.AlertProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertProcessServiceImpl implements AlertProcessService {

	private Logger log = Logger.getLogger(this.getClass());

	private final AlertProcessDao alertProcessDao;
	
	@Autowired
	public AlertProcessServiceImpl(AlertProcessDao alertProcessDao) {
		this.alertProcessDao = alertProcessDao;
	}


	@Override
	public ResultDTO<AlertProcessDO> queryAlertProcessByMP(String machine,String process) {
		 ResultDTO<AlertProcessDO> result = new ResultDTO<AlertProcessDO>();
			
			try {
				AlertProcessDO alertProcess = alertProcessDao.selectAlertProcessByMP(machine,process);
				result.setModule(alertProcess);
				result.setSuccess(true);
			} catch (Exception e) {
				result.setSuccess(false);
				result.setErrorDetail("根据主键查询失败");
				log.error("AlertProcessServiceImpl queryAlertProcessById error", e);
			}
			return result;
    
	}

	@Override
	public ResultDTO<AlertProcessDO> queryOne(QueryAlertProcessBO query) {
	ResultDTO<AlertProcessDO> result = new ResultDTO<AlertProcessDO>();
		
		try {
			AlertProcessDO alertProcess = alertProcessDao.selectOne(query);
			result.setModule(alertProcess);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("查询失败");
			log.error("AlertProcessServiceImpl queryAlertProcessById error", e);
		}
		return result;
	}

	@Override    //查询列表
	public BatchResultDTO<AlertProcessDO> queryAlertProcessList(QueryAlertProcessBO query) {
    BatchResultDTO<AlertProcessDO> result = new BatchResultDTO<AlertProcessDO>();
		
		try {
			List<AlertProcessDO> list = alertProcessDao.selectAlertProcessList(query);
			result.setModule(list);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("查询列表失败");
			log.error("AlertProcessServiceImpl queryAlertProcessList error", e);
		}
		return result;
	}

	@Override
	public BatchResultDTO<AlertProcessDO> queryAlertProcessPage(QueryAlertProcessBO query) {
		
    BatchResultDTO<AlertProcessDO> result = new BatchResultDTO<AlertProcessDO>();		
		try {
			int count = alertProcessDao.selectAlertProcessCount(query);
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
			
			List<AlertProcessDO> list = alertProcessDao.selectAlertProcessList(query);
			result.setModule(list);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("分页查询失败");
			log.error("AlertProcessServiceImpl queryAlertProcessPage error", e);
		}
		return result;
	}

	@Override
	public BaseResultDTO createAlertProcess(AlertProcessDO alertProcess) {
     BaseResultDTO result = new BaseResultDTO();
		
		try {
			
			//检查是否存在相同的报警
			QueryAlertProcessBO query = new QueryAlertProcessBO();
			//query.setTriggerId(alertTrigger.getTriggerId());
			query.setMachine(alertProcess.getMachine());
			AlertProcessDO oldAlert = alertProcessDao.selectOne(query);
				//如果收到相同状态的提醒，则不进行任何操作
			
			return result.returnSuccess();
		} catch (Exception e) {
			log.error("AlertProcessServiceImpl createAlertProcess error", e);
			return result.returnError("添加失败");
		}
	}

	@Override
	public BaseResultDTO modifyAlertProcess(AlertProcessDO alertProcess) {
	BaseResultDTO result = new BaseResultDTO();
		
		try {
			//只修改设定值得字段，属于选择性修改
			alertProcessDao.updateAlertProcessSelective(alertProcess);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("修改失败");
			log.error("AlertProcessServiceImpl modifyAlertProcess error", e);
		}
		return result;
	}

	@Override
	public BaseResultDTO modifyAlertProcessCompletely(AlertProcessDO alertProcess) {
   BaseResultDTO result = new BaseResultDTO();
		
		try {
			//不断字段是否非空，都进行修改，属于完全修改
			alertProcessDao.updateAlertProcess(alertProcess);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("修改失败");
			log.error("AlertProcessServiceImpl modifyAlertProcessCompletely error", e);
		}
		return result;
	}

	@Override
	public BaseResultDTO removeAlertProcess(AlertProcessDO alertProcess) {
     BaseResultDTO result = new BaseResultDTO();
		
		try {
			alertProcessDao.deleteAlertProcess(alertProcess);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("删除失败");
			log.error("AlertProcessServiceImpl removeAlertProcess error", e);
		}
		return result;
	}




}
