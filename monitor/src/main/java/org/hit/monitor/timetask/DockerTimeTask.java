package org.hit.monitor.timetask;

import java.text.ParseException;

import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.model.dockermodel.Docker;
import org.hit.monitor.model.dockermodel.DockerdataList;
import org.hit.monitor.service.DockerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Component
public class DockerTimeTask {
	
	@Autowired
	DockerService dockerservice;
	ResultDTO<String> result;
	
	 // @Scheduled(fixedDelay = 3000)  // 暂时禁用，需配置Docker环境后启用
	public void updateDockerList() throws Exception{
		 
		System.out.println("--------重新获取docker数据-----------");			
		result =dockerservice.fetchDockerMetricsJSONByAPI("");
		//System.out.println(result.getModule());
		DockerdataList.docker = dockerservice.jsonToEntity(result.getModule());
		for(int i=0;i<DockerdataList.docker.size();i++){
			dockerservice.allDataTransform(DockerdataList.docker.get(i));
//			System.out.println(DockerdataList.docker.get(i).getStats().get(0).getTimestamp());
		}
//		System.out.println(DockerdataList.docker.get(0).getName());
		//System.out.println(DockerdataList.docker.get(1).getName());
			
			
//		DockerdataList.docker.set(0, ((Docker) dockerservice.jsonToEntity(result.getModule())).getDocker().get(0));
//		DockerdataList.docker = dockerservice.allDataTransform(DockerdataList.docker.get(0));	
//		System.out.println(DockerdataList.docker.getStats().get(DockerdataList.docker.getStats().size()-1).getTimestamp());
	}
	
}
