package org.hit.monitor.controller;

import java.io.IOException;

import javax.management.MalformedObjectNameException;

import org.hit.monitor.service.Neo4jService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/Neo4j")
public class Neo4jController extends BaseController {
	@Autowired
	Neo4jService neo4jService;
	
	@RequestMapping("/kernel")
	@ResponseBody
	public String fetchKernel() throws MalformedObjectNameException, IOException{
		
		
		 return responseControllerResultSuccess(neo4jService.fetchKernelMetrics());
	}
	
	@RequestMapping("/pagecache")
	@ResponseBody
	public String fetchPagecache() throws MalformedObjectNameException, IOException{
		
		 return responseControllerResultSuccess(neo4jService.fetchPageCacheMetrics());
	}
	
	
	@RequestMapping("/primitive")
	@ResponseBody
	public String fetchPrimitive() throws MalformedObjectNameException, IOException{
		
		return responseControllerResultSuccess(neo4jService.fetchPrimitiveCount());
	}
}
