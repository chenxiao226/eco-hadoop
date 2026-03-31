package org.hit.monitor.host;

import org.hit.monitor.model.HostDO;
import org.hit.monitor.service.HostsService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:application-context.xml" })
public class HostServiceTest {
	
	@Autowired
	private HostsService hostsService;
	
	@Before
	public void setUp() throws Exception {
		System.err.println("----------------开始测试-------------------");
	}
	
	@After
	public void setDown() throws Exception {
		System.err.println("----------------结束测试-------------------");
	}
	
	@Test
	public void querySingleTest() {
		 List<HostDO> hostsList = null;

		 Long beginTime = System.currentTimeMillis();
		 hostsList = hostsService.queryHostsList();
		 Long endTime = System.currentTimeMillis();
		 System.out.println("初始查询用时："+ (endTime - beginTime) +"ms");

		 beginTime = System.currentTimeMillis();
		 hostsList = hostsService.queryHostsList();
		 endTime = System.currentTimeMillis();
		 System.out.println("缓存查询用时："+ (endTime - beginTime) +"ms");

		 for(HostDO host: hostsList){
		 	System.out.println(host.getName());
		 }
	}
}
