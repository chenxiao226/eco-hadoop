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
import org.hit.monitor.model.TaskSubmitDO;
import org.hit.monitor.service.AlertLogService;
import org.hit.monitor.service.AlertProcessService;
import org.hit.monitor.service.AlertTriggerService;
import org.hit.monitor.service.TaskSubmitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Controller
@RequestMapping("/task")
public class TaskSubmitController extends BaseController {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	TaskSubmitService taskSubmitService;


	/**
	 * 提交任务表单
	 */
	@ResponseBody
	@RequestMapping("/submit")
	public String addTaskSubmit(HttpServletRequest request) {

		try {

			String host = request.getParameter("machine");
			String taskDes = request.getParameter("Task description");
			String triggerDes = request.getParameter("Trigger description");
			String severity = request.getParameter("Trigger severity");
			String triggerName = request.getParameter("Trigger name");

			if (!StringUtils.isNoneBlank(host, taskDes,triggerDes, severity)) {
				return responseControllerResultError("222参数错误");
			}

			TaskSubmitDO taskSubmitDO = new TaskSubmitDO();
			taskSubmitDO.setHost(host);
			taskSubmitDO.setTaskDes(taskDes);
			taskSubmitDO.setTriggerDes(triggerDes);
			taskSubmitDO.setSeverity(severity);
			taskSubmitDO.setStartTime(System.currentTimeMillis());
			taskSubmitDO.setTriggerName(triggerName);

			
			BaseResultDTO createRes = taskSubmitService.createTaskSubmit(taskSubmitDO);
//			System.out.println(222);
			if (createRes.isSuccess()) {
				return responseControllerResultSuccess(null);
			} else {
				return responseControllerResultError(createRes.getErrorDetail());
			}
		} catch (Exception e) {
			log.error("提交任务表单异常", e);
			return responseControllerResultSuccess("提交任务表单异常");
		}
	}

}
