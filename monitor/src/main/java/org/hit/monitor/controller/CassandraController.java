package org.hit.monitor.controller;

import java.io.IOException;

import javax.management.MalformedObjectNameException;

import org.hit.monitor.service.CassandraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/cassandra")
public class CassandraController extends BaseController {
	
	@Autowired
	CassandraService cassandraService;
	
	@RequestMapping("/Tabe_CQL")
	@ResponseBody
	public String fetchTabe_CQL_ExceptionsMetrics() throws MalformedObjectNameException, IOException{
			
		return responseControllerResultSuccess(cassandraService.fetchTableCQLMetrics());
	}
	
	@RequestMapping("/Thread")
	@ResponseBody
	public  String fetchThreadMetrics() throws MalformedObjectNameException, IOException{
		
		return responseControllerResultSuccess(cassandraService.fetchThreadPoolsMetrics());
	}
	
	@RequestMapping("/CacheHits")
	@ResponseBody
	public String fetchCacheHitsMetrics() throws MalformedObjectNameException, IOException{
		return responseControllerResultSuccess(cassandraService.fetchCacheRatio());
	}
	@RequestMapping("/test")
	@ResponseBody
	public String test(){
		return responseControllerResultSuccess(cassandraService.test());
	}
}
