package org.hit.monitor.metrics;

import org.hit.monitor.service.MetricsService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:application-context.xml"})
public class MetricsServiceTest {
	
	@Autowired
	private MetricsService metricsService;
	
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
		// QueryMetricsBO queryMetricsBO = new QueryMetricsBO();
		// queryMetricsBO.setStart(0L);
		//
		// BatchResultDTO<MetricsDataDO> metricsDataDOBatchResultDTO = null;
		//
		// Long beginTime = System.currentTimeMillis();
		// metricsDataDOBatchResultDTO = metricsService.fetchData("zabbix[process,alerter,avg,busy]", queryMetricsBO);
		// Long endTime = System.currentTimeMillis();
		// System.out.println("初始查询用时：" + (endTime - beginTime) + "ms");
		//
		// beginTime = System.currentTimeMillis();
		// metricsDataDOBatchResultDTO = metricsService.fetchData("zabbix[process,alerter,avg,busy]", queryMetricsBO);
		// endTime = System.currentTimeMillis();
		// System.out.println("第二次查询用时：" + (endTime - beginTime) + "ms");
		//
		// beginTime = System.currentTimeMillis();
		// for (int i = 0; i < 20; i++) {
		// 	metricsDataDOBatchResultDTO = metricsService.fetchData("zabbix[process,alerter,avg,busy]", queryMetricsBO);
		// }
		// endTime = System.currentTimeMillis();
		// System.out.println("后续20次查询平均用时：" + (endTime - beginTime) / 20.0 + "ms");
		
		// System.out.println("sum\t\tnum\t\ttime");
		// for (MetricsDataDO data : metricsDataDOBatchResultDTO.getModule()) {
		// 	System.out.println(data.getSum() + "\t\t" + data.getNum() + "\t\t" + data.getProcessTime());
		// }
	}
}