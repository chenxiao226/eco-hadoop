package org.hit.monitor.controller;


import java.util.ArrayList;

import java.util.List;


import javax.servlet.http.HttpServletRequest;


import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.model.dockermodel.Docker;
import org.hit.monitor.model.dockermodel.DockerdataList;
import org.hit.monitor.model.dockermodel.Stats;
import org.hit.monitor.service.DockerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/docker")
public class DockerController extends BaseController {
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	DockerService dockerservice;
	ResultDTO<String> result ;
	List<Stats> stats = new ArrayList<Stats>();
	Docker docker;
	
	public void test(){
		result =dockerservice.fetchDockerMetricsJSONByAPI("");
	}
		
	
	//@Scheduled(fixedRate = 1000 * 3)//定时任务 每隔3秒取一次数据
	public void UpdateData(){
		
		
		System.out.println(docker);
		
	}
	@ResponseBody
	@RequestMapping("/metrics")
	public String fetchMetricsByName() {

	
//		ResultDTO<String> result = dockerservice.fetchDockerMetricsJSONByAPI("");
		System.out.println(result.getModule());
		if(result.isSuccess()){
			return responseControllerResultSuccess(result);
		}else{
			return responseControllerResultError("error");
		}
		
	}

	@ResponseBody
	@RequestMapping("/total")	
	public String fetchDockerdata(HttpServletRequest request)  {
//		ResultDTO<String> result ;
//		result  =dockerservice.fetchDockerMetricsJSONByAPI("");
//		result.setModule(DockerdataList.docker);				
//		docker = dockerservice.allDataTransform();	
//		System.out.println("--------total-----------"+dockerservice.jsonToEntity(result.getModule()).size());
		
		
		return responseControllerResultSuccess(DockerdataList.docker);
		
	}

}
