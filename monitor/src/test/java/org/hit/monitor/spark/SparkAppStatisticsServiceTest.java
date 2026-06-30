package org.hit.monitor.spark;

import org.hit.monitor.service.SparkAppStatisticsService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:application-context.xml"})
public class SparkAppStatisticsServiceTest {
	
	@Autowired
	private SparkAppStatisticsService sparkAppStatisticsService;
	
	@Before
	public void setUp() throws Exception {
		System.err.println("----------------开始测试-------------------");
	}
	
	@After
	public void setDown() throws Exception {
		System.err.println("----------------结束测试-------------------");
	}
	
	@Test
	public void queryTest() {
		// Long beginTime = System.currentTimeMillis();
		//
		// ResultDTO<SparkAppStatisticsDO> sparkAppStatisticsDOResultDTO = sparkAppStatisticsService.fetchLatestAppStatistics();
		// SparkAppStatisticsDO module = sparkAppStatisticsDOResultDTO.getModule();
		// System.out.println(module.toString());
		//
		// Long endTime = System.currentTimeMillis();
		// System.out.println("初始查询用时：" + (endTime - beginTime) + "ms");
	}
}
