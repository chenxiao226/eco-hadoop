package org.hit.monitor.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.hit.monitor.common.AlertScoketClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/alertconf")
public class AlertConfController extends BaseController {

	@ResponseBody
	@RequestMapping("/add/trigger")
	public String addTriggerConf(HttpServletRequest request) {

		String name = request.getParameter("name");
		String expression = request.getParameter("expression");
		String code = request.getParameter("code");
		System.out.println("name = " + name);
		System.out.println("expression = " + expression);
		System.out.println("code = " + code);
		if (!StringUtils.isNoneBlank(name, expression, code)) {
			return responseControllerResultError("参数错误！");
		}

		String send = code + "!!!" + name + "#" + expression;
		System.out.println("发送的消息：" + send);

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
	@RequestMapping("/add/log")
	public String addLogConf(HttpServletRequest request) {
		
		String host = request.getParameter("host");
		String name = request.getParameter("name");
		String expression = request.getParameter("expression");
		String code = request.getParameter("code");
		System.out.println("request==" + request);
		if (!StringUtils.isNoneBlank(host,name, expression, code)) {
			return responseControllerResultError("参数错误！");
		}
		
		String send = code + "!!!"+host+"#" + name + "#" + expression;
		
		try {
			String ret = new AlertScoketClient().sendMessage(send);// 调用AlertScoketClient这个类发送数据
			return responseControllerResultSuccess(ret);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return responseControllerResultError("发送数据失败！");
		}
		
	}

}
