package org.hit.monitor.controller;

import org.apache.commons.lang3.StringUtils;
import org.hit.monitor.bo.QueryAlertLogBO;
import org.hit.monitor.bo.QueryAlertProcessBO;
import org.hit.monitor.bo.QueryAlertTriggerBO;
import org.hit.monitor.common.ALERT;
import org.hit.monitor.common.AlertScoketClient;
import org.hit.monitor.common.BaseResultDTO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.model.AlertLogDO;
import org.hit.monitor.model.AlertProcessDO;
import org.hit.monitor.model.AlertTriggerDO;
import org.hit.monitor.service.AlertLogService;
import org.hit.monitor.service.AlertProcessService;
import org.hit.monitor.service.AlertTriggerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 接受故障预警子系统的报警信息
 */
@Controller
@RequestMapping("/alert")
public class AlertController extends BaseController {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	private final AlertTriggerService alertTriggerService;
	
	private final AlertLogService alertLogService;
	
	private final AlertProcessService alertProcessService;
	
	@Autowired
	public AlertController(AlertTriggerService alertTriggerService, AlertLogService alertLogService, AlertProcessService alertProcessService) {
		this.alertTriggerService = alertTriggerService;
		this.alertLogService = alertLogService;
		this.alertProcessService = alertProcessService;
	}
	
	/**
	 * 添加规则报警信息
	 *//*
	@ResponseBody
	@RequestMapping("/trigger/add")
	public String addTriggerAlert(HttpServletRequest request) {
		try {
			String host = request.getParameter("machine");
			String triggerDes = request.getParameter("Trigger name");
			String severity = request.getParameter("Trigger severity");

			if (!StringUtils.isNoneBlank(host, triggerDes, severity)) {
				return responseControllerResultError("参数错误");
			}
			
			AlertTriggerDO alertTrigger = new AlertTriggerDO();
			alertTrigger.setHost(host);
			alertTrigger.setTriggerDes(triggerDes);
			alertTrigger.setSeverity(severity);
			alertTrigger.setTime(System.currentTimeMillis());
			

			
			BaseResultDTO createRes = alertTriggerService.createAlertTrigger(alertTrigger);
			if (createRes.isSuccess()) {
				return responseControllerResultSuccess(null);
			} else {
				return responseControllerResultError(createRes.getErrorDetail());
			}
		} catch (Exception e) {
			log.error("添加规则报警信息出错", e);
			return responseControllerResultSuccess("监控服务器异常");
		}
	}
	
	*//**
	 * 查询规则报警信息
	 *//*
	@ResponseBody
	@RequestMapping("/trigger/fetch")
	public String fetchTriggerAlert(HttpServletRequest request) {
		try {
			String start = request.getParameter("start");//时间戳
			String end = request.getParameter("end");//时间戳
			String des = request.getParameter("des");//报警描述（模糊查询）
			String severity = request.getParameter("severity");//严重程度
			String host = request.getParameter("host");//主机
			String statusStr = request.getParameter("status");//报警状态
			Integer limit = getIntegerParameterFromRequest(request, "limit", null);//分页限制
			
			QueryAlertTriggerBO queryAlertTrigger = new QueryAlertTriggerBO();
			
			if (StringUtils.isNotBlank(start)) {
				queryAlertTrigger.setStart(Long.parseLong(start));
			}
			if (StringUtils.isNotBlank(end)) {
				queryAlertTrigger.setEnd(Long.parseLong(end));
			}
			if (StringUtils.isNotBlank(des)) {
				queryAlertTrigger.setTriggerDes(des);
			}
			if (StringUtils.isNotBlank(severity)) {
				queryAlertTrigger.setSeverity(severity);
			}
			if (StringUtils.isNotBlank(host)) {
				queryAlertTrigger.setHost(host);
			}
			if (StringUtils.isNotBlank(statusStr)) {
				queryAlertTrigger.setStatus(Integer.parseInt(statusStr));
			}
			if (limit != null) {
				queryAlertTrigger.setLimit(limit);
			}
			
			BatchResultDTO<AlertTriggerDO> queryRes = alertTriggerService.queryAlertTriggerList(queryAlertTrigger);
			if (queryRes.isSuccess()) {
				return responsePageSuccess(queryRes.getModule(), queryAlertTrigger);
			} else {
				return responsePageError(queryRes.getErrorDetail());
			}
		} catch (Exception e) {
			log.error("查询规则报警信息出错", e);
			return responsePageError("服务器异常");
		}
	}
	
	*//**
	 * 添加日志报警
	 *//*
	@ResponseBody
	@RequestMapping("/log/add")
	public String addLogAlert(HttpServletRequest request) {
		try {
			String occurDate = request.getParameter("occurDate");
			String host = request.getParameter("machine");
			String user = request.getParameter("user");
			String software = request.getParameter("software");
			String component = request.getParameter("component");
			String faultType = request.getParameter("faultType");
			String content = request.getParameter("content");
			
			if (!StringUtils.isNoneBlank(occurDate, host, user, software, component, faultType, content)) {
				return responseControllerResultError("参数错误");
			}
			
			AlertLogDO alertlog = new AlertLogDO();
			alertlog.setOccurTime(Long.valueOf(occurDate) * 1000);
			alertlog.setHost(host);
			alertlog.setUser(user);
			alertlog.setSoftware(software);
			alertlog.setComponent(component);
			alertlog.setFaultType(faultType);
			alertlog.setContent(content);
			
			BaseResultDTO createRes = alertLogService.createAlertLog(alertlog);
			if (createRes.isSuccess()) {
				return responseControllerResultSuccess(null);
			} else {
				return responseControllerResultError(createRes.getErrorDetail());
			}
		} catch (Exception e) {
			log.error("添加日志报警信息出错", e);
			return responseControllerResultSuccess("监控服务器异常");
		}
	}
	
	*//**
	 * 查询日志报警信息
	 *//*
	@ResponseBody
	@RequestMapping("/log/fetch")
	public String fetchLogAlert(HttpServletRequest request) {
		try {
			String start = request.getParameter("start");
			String end = request.getParameter("end");
			String host = request.getParameter("host");
			
			QueryAlertLogBO queryAlertLogBO = new QueryAlertLogBO();
			
			if (StringUtils.isNotBlank(start)) {
				queryAlertLogBO.setStart(Long.parseLong(start));
			}
			if (StringUtils.isNotBlank(end)) {
				queryAlertLogBO.setEnd(Long.parseLong(end));
			}
			if (StringUtils.isNotBlank(host)) {
				queryAlertLogBO.setHost(host);
			}
			
			BatchResultDTO<AlertLogDO> queryRes = alertLogService.queryAlertLogList(queryAlertLogBO);
			
			if (queryRes.isSuccess()) {
				return responsePageSuccess(queryRes.getModule(), queryAlertLogBO);
			} else {
				return responsePageError(queryRes.getErrorDetail());
			}
		} catch (Exception e) {
			log.error("查询日志报警信息出错", e);
			return responsePageError("服务器异常");
		}
	}
	
	*//**
	 * 添加进程报警
	 *//*
	@ResponseBody
	@RequestMapping("/process/add")
	public String addProcessAlert(HttpServletRequest request) {
		*//*Map<String, String[]> paramsMap = request.getParameterMap();
		return responseControllerResultSuccess(JSON.toJSONString(paramsMap));*//*
		try {
			String host = request.getParameter("machine");
			String process = request.getParameter("Process name");
			String pstatus = request.getParameter("Process status");
			
			if (!StringUtils.isNoneBlank(host, process, pstatus)) {
				return responseControllerResultError("参数错误");
			}
			
			AlertProcessDO alertProcess = new AlertProcessDO();
			alertProcess.setMachine(host);
			alertProcess.setProcess(process);
			
			int status = 0;
			if (pstatus.equals("STOP")) {
				status = ALERT.PROCESS.STOP;
			} else if (pstatus.equals("START")) {
				status = ALERT.PROCESS.START;
			} else {
				return responseControllerResultError("报警状态错误");
			}
			alertProcess.setStatus(status);
			
			BaseResultDTO createRes = alertProcessService.createAlertProcess(alertProcess);
			if (createRes.isSuccess()) {
				return responseControllerResultSuccess(null);
			} else {
				return responseControllerResultError(createRes.getErrorDetail());
			}
		} catch (Exception e) {
			log.error("添加规则报警信息出错", e);
			return responseControllerResultSuccess("监控服务器异常");
		}
	}
	
	@ResponseBody
	@RequestMapping("/process/fetch")
	public String fetchProcessAlert(HttpServletRequest request) {
		try {
			
			String machine = request.getParameter("machine");
			String process = request.getParameter("process");
			String pstatus = request.getParameter("status");
			
			QueryAlertProcessBO queryAlertProcess = new QueryAlertProcessBO();
			
			if (StringUtils.isNotBlank(machine)) {
				queryAlertProcess.setMachine(machine);
			}
			if (StringUtils.isNotBlank(process)) {
				queryAlertProcess.setProcess(process);
				
			}
			if (StringUtils.isNotBlank(pstatus)) {
				queryAlertProcess.setStatus(Integer.parseInt(pstatus));
			}
			
			BatchResultDTO<AlertProcessDO> queryRes = alertProcessService
					.queryAlertProcessList(queryAlertProcess);
			if (queryRes.isSuccess()) {
				return responsePageSuccess(queryRes.getModule(), queryAlertProcess);
			} else {
				return responsePageError(queryRes.getErrorDetail());
			}
		} catch (Exception e) {
			log.error("查询进程报警信息出错", e);
			return responsePageError("服务器异常");
		}
	}
	
	@ResponseBody
	@RequestMapping("/Process/transfer")
	public String tansProcessAlert(HttpServletRequest request) {
		
		String processes = request.getParameter("message");
		
		if (!StringUtils.isNoneBlank(processes)) {
			return responseControllerResultError("参数错误！");
		}
		
		String send = "process!!!0&" + processes;
		System.out.println(send);
		
		try {
			String ret = new AlertScoketClient().sendMessage(send);// 调用AlertScoketClient这个类发送数据
			
			return responseControllerResultSuccess(ret);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
			return responseControllerResultError("发送数据失败！");
		}
		
	}
	
	@ResponseBody
	@RequestMapping("/Process/refresh")
	public String refreshProcessAlert(HttpServletRequest request) {
		
		String refmessage = request.getParameter("refmessage");
		
		if (!StringUtils.isNoneBlank(refmessage)) {
			return responseControllerResultError("参数错误！");
		}
		String send = refmessage;
		System.out.println(send);
		
		try {
			String ret = new AlertScoketClient().sendMessage(send);// 调用AlertScoketClient这个类发送数据
			
			return responseControllerResultSuccess(ret);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
			return responseControllerResultError("发送数据失败！");
		}
		
	}*/
}
