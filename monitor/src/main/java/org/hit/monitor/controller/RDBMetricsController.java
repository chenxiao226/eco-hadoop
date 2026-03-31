package org.hit.monitor.controller;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.hit.monitor.service.impl.RDBMetricsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
	


@Controller
@RequestMapping("/RDB")
public class RDBMetricsController extends BaseController {
	private Logger log = LoggerFactory.getLogger(getClass());
	@Autowired
	private RDBMetricsServiceImpl rDBMetricsService;
	
	
	@RequestMapping("/userNum")
	@ResponseBody
	public String fetchUserNum(){
		return JSON.toJSONString(rDBMetricsService.getUserNum());
	}
	
	@RequestMapping("/Innodb_buffer_pool_pages_total")
	@ResponseBody
	public String fetchInnodb_buffer_pool_pages_total(){
		
		return JSON.toJSONString(rDBMetricsService.fetchBufferPoolPagesTotal());
	}
	
	@RequestMapping("/fetchrRdb_User_Buffer")
	@ResponseBody
	public String fetchrRdb_User_Buffer(){
		try {
			String userNum = String.valueOf(rDBMetricsService.getUserNum());
			String Innodb_buffer_pool_pages_total = rDBMetricsService.fetchBufferPoolPagesTotal();
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("userNum", userNum);
			result.put("Innodb_buffer_pool_pages_total", Innodb_buffer_pool_pages_total);
			return responseControllerResultSuccess(result);
		} catch (Exception e) {
			log.error("获取CPU静态数据出错", e);
			return responseControllerResultError("参数错误");
		}
		
	}
	
	@RequestMapping("/ProcessList")
	@ResponseBody
	public String fetchProcessList(){
		return  responseControllerResultSuccess(rDBMetricsService.fetchProcess());
	}
	
	@RequestMapping("/QPS")
	@ResponseBody
	public String fechQPS(){
		return JSON.toJSONString(rDBMetricsService.fetchQPS());
	}
	
	@RequestMapping("/InnoDB_Buffer")
	@ResponseBody
	public String fetchInnoDB_Buffer(){
		try {
			String Innodb = String.valueOf(rDBMetricsService.fetchBuffer());
			long time = Long.parseLong(rDBMetricsService.fetchTime())+1532941363;
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("Innodb", Innodb);
			result.put("time", time);
			return responseControllerResultSuccess(result);
		} catch (Exception e) {
			log.error("获取CPU静态数据出错", e);
			return responseControllerResultError("参数错误");
		}
		
	}
}
