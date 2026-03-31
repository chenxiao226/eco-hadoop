package org.hit.monitor.timetask;

import org.hit.monitor.bo.QueryMetricsBO;
import org.hit.monitor.bo.QueryMetricsDefineBO;
import org.hit.monitor.common.BaseResultDTO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.model.MetricsDefineDO;
import org.hit.monitor.service.MetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 实时数据定时清理
 */
public class MetricsDataCleaner {

	private Logger log = LoggerFactory.getLogger(getClass());

	private static final long DAY = 60 * 60 * 24; // 一天的秒数

	@Autowired
	private MetricsService metricsService;

	/**
	 * 清除过期指标数据
	 */
	public void cleanMetricsData() {
		log.info("开始执行过期指标数据清理");
		try {
			BatchResultDTO<MetricsDefineDO> metricsDefineList = metricsService.queryMetricsDefineList(new QueryMetricsDefineBO());
			if (metricsDefineList.isFailed()) {
				log.error(metricsDefineList.getErrorDetail());
				return;
			}
			int successCount = 0;
			int failureCount = 0;
			if (metricsDefineList.isExist()) {
				
				// 定义要删除数据的时间区间，从1970至一个礼拜前
				long beginTime = 0L;
				//long endTime = System.currentTimeMillis() / 1000 - DAY * 7;
				long endTime = 1L;
				BaseResultDTO removeRes = null;
				
				for (MetricsDefineDO md : metricsDefineList.getModule()) {
					
					QueryMetricsBO filter = new QueryMetricsBO();
					filter.setMetricsName(md.getName());
					filter.setStart(beginTime);
					filter.setEnd(endTime);
					removeRes = metricsService.removeMetricsData(filter);
					
					if (removeRes.isFailed()) {
						log.error("指标【" + md.getName() + "】删除失败。" + removeRes.getErrorDetail());
						failureCount++;
					} else {
						successCount++;
					}
				}
			}
			log.info("过期指标数据清理完成，成功清理 " + successCount + " 个，失败 " + failureCount + " 个");
		} catch (Exception e) {
			log.error("定时清理实时数据发生错误", e);
		}
	}
}
