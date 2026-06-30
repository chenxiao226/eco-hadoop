package org.hit.monitor.service.impl;

import org.apache.log4j.Logger;
import org.hit.monitor.bo.QuerySparkAppStatisticsBO;
import org.hit.monitor.common.BaseResultDTO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.dao.SparkAppStatisticsDao;
import org.hit.monitor.dao.SparkRunningAppDao;
import org.hit.monitor.model.SparkAppStatisticsDO;
import org.hit.monitor.service.MetricsService;
import org.hit.monitor.service.SparkAppStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SparkAppStatisticsServiceImpl implements SparkAppStatisticsService {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private final SparkAppStatisticsDao sparkAppStatisticsDao;
	private final SparkRunningAppDao sparkRunningAppDao;
	private final MetricsService metricsService;
	
	private boolean initFetchDB = false;
	private final Object initFetchDBLock = new Object();
	private ConcurrentHashMap<String, String> runningAppSet = new ConcurrentHashMap<>();
	
	@Autowired
	public SparkAppStatisticsServiceImpl(SparkAppStatisticsDao sparkAppStatisticsDao, MetricsService metricsService, SparkRunningAppDao sparkRunningAppDao) {
		this.sparkAppStatisticsDao = sparkAppStatisticsDao;
		this.metricsService = metricsService;
		this.sparkRunningAppDao = sparkRunningAppDao;
	}
	
	// @Override
	// public ResultDTO<SparkAppStatisticsDO> fetchLatestAppStatistics() {
	//
	// 	ResultDTO<SparkAppStatisticsDO> result = new ResultDTO<SparkAppStatisticsDO>();
	// 	try {
	// 		String sparkUri = CONFIG.SPARK_HISTORY_SERVER + "/applications?status=running";
	//
	// 		ResultDTO<String> sparkResult = metricsService.fetchMetricsJSONByAPI(sparkUri);
	// 		JSONArray appArray = JSON.parseArray(sparkResult.getModule());
	//
	// 		SparkAppStatisticsDO data = new SparkAppStatisticsDO();
	// 		data.setAppCompleted(fetchFinishedAppIncremental().getModule());
	// 		data.setAppRunning(appArray.size());
	//
	// 		return result.returnSuccess(data);
	// 	} catch (Exception e) {
	// 		log.error("SparkAppStatisticsServiceImpl fetchLatestAppStatistics error", e);
	// 		return result.returnError("查询Spark App信息失败");
	// 	}
	// }
	//
	// /**
	//  * 增量查询Spark已完成App
	//  */
	// public ResultDTO<Integer> fetchFinishedAppIncremental() {
	//
	// 	if (!initFetchDB) {
	// 		synchronized (initFetchDBLock) {
	// 			if (!initFetchDB) {
	// 				List<String> sparkRunningApps = sparkRunningAppDao.selectSparkRunningAppList();
	// 				for (String appId : sparkRunningApps)
	// 					runningAppSet.put(appId, appId);
	// 			}
	// 		}
	// 	}
	//
	// 	ResultDTO<Integer> result = new ResultDTO<Integer>();
	// 	try {
	// 		SparkAppStatisticsDO lastFetchRes = queryOne(new QuerySparkAppStatisticsBO());
	// 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'GMT'");
	//
	// 		String sparkUri = CONFIG.SPARK_HISTORY_SERVER + "/applications";
	// 		if (lastFetchRes != null) {
	// 			Date lastTime = new Date(lastFetchRes.getLastFetchTime() + 1); //一定要加1，防止之前最后一条数据被重复记入
	// 			String timeQueryParam = sdf.format(lastTime);
	// 			sparkUri = sparkUri + "?minDate=" + timeQueryParam;
	// 		}
	//
	// 		ResultDTO<String> sparkResult = metricsService.fetchMetricsJSONByAPI(sparkUri);
	// 		JSONArray appArray = JSON.parseArray(sparkResult.getModule());
	// 		int finishCount = 0;
	// 		Long newestTime = lastFetchRes == null ? 0 : lastFetchRes.getLastFetchTime();
	//
	// 		List<String> runningAppToPersis = new ArrayList<>();
	// 		Set<String> appConfirmStillRunning = new HashSet<>();
	// 		Set<String> runningAppConfirmFinished = new HashSet<>();
	//
	// 		for (int i = 0; i < appArray.size(); i++) {
	// 			JSONObject app = appArray.getJSONObject(i);
	// 			JSONArray attempts = app.getJSONArray("attempts");
	// 			JSONObject firstAttempt = attempts.getJSONObject(0);
	// 			JSONObject lastAttempt = attempts.getJSONObject(attempts.size() - 1);
	// 			boolean finished = lastAttempt.getBoolean("completed");
	// 			String appId = app.getString("id");
	// 			if (finished) {
	// 				finishCount++;
	// 				long startTime = firstAttempt.getLong("startTimeEpoch");
	// 				if (startTime > newestTime) {
	// 					newestTime = startTime;
	// 				}
	// 				if (runningAppSet.contains(appId)) {
	// 					runningAppSet.remove(appId);
	// 					runningAppConfirmFinished.add(appId);
	// 				}
	// 			} else {
	// 				if (!runningAppSet.contains(appId)) {
	// 					runningAppSet.put(appId, appId);
	// 					runningAppToPersis.add(appId);
	// 				}
	// 				appConfirmStillRunning.add(appId);
	// 			}
	// 		}
	//
	//
	//
	//
	// 		if (lastFetchRes != null) {
	// 			lastFetchRes.setAppCompleted(lastFetchRes.getAppCompleted() + finishCount);
	// 			lastFetchRes.setLastFetchTime(newestTime);
	// 			sparkAppStatisticsDao.updateSparkAppStatistics(lastFetchRes); //乐观锁更新
	// 		} else {
	// 			// 第一次进行数据写入，创建记录，设定id为1，版本号为1
	// 			lastFetchRes = new SparkAppStatisticsDO();
	// 			lastFetchRes.setAppCompleted(finishCount);
	// 			lastFetchRes.setLastFetchTime(newestTime);
	// 			lastFetchRes.setVersion(1);
	// 			lastFetchRes.setId(1);
	// 			sparkAppStatisticsDao.insertSparkAppStatistics(lastFetchRes);
	// 		}
	//
	//
	// 		//查询每一个之前记录为running的app是否已经完成(此处是由于Spark API限制导致的)
	// 		for (String id : runningAppSet.keySet()) {
	// 			String queryAppStateUri = CONFIG.SPARK_HISTORY_SERVER + "/applications?" + id;
	// 			JSONArray ja = JSON.parseArray(metricsService.fetchMetricsJSONByAPI(sparkUri).getModule());
	//
	// 			if (ja.size() > 0) {
	// 				JSONObject app = ja.getJSONObject(0);
	// 				JSONArray attempts = app.getJSONArray("attempts");
	// 				JSONObject lastAttempt = attempts.getJSONObject(attempts.size() - 1);
	// 				boolean finished = lastAttempt.getBoolean("completed");
	// 				if (finished) {
	// 					finishCount++;
	// 					runningAppSet.remove(id);
	// 					runningAppConfirmFinished.add(id);
	// 				}
	// 			}
	// 		}
	//
	//
	// 		return result.returnSuccess(lastFetchRes.getAppCompleted());
	// 	} catch (Exception e) {
	// 		log.error("SparkAppStatisticsServiceImpl fetchLatestAppStatistics error", e);
	// 		return result.returnError("增量查询Spark已完成App失败");
	// 	}
	// }
	
	private ResultDTO<SparkAppStatisticsDO> querySparkAppStatisticsById(Long id) {
		
		ResultDTO<SparkAppStatisticsDO> result = new ResultDTO<SparkAppStatisticsDO>();
		
		try {
			SparkAppStatisticsDO sparkAppStatistics = sparkAppStatisticsDao.selectSparkAppStatisticsById(id);
			result.setModule(sparkAppStatistics);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("根据主键查询失败");
			log.error("SparkAppStatisticsServiceImpl querySparkAppStatisticsById error", e);
		}
		return result;
	}
	
	private SparkAppStatisticsDO queryOne(QuerySparkAppStatisticsBO query) {
		try {
			return sparkAppStatisticsDao.selectOne(query);
		} catch (Exception e) {
			log.error("SparkAppStatisticsServiceImpl querySparkAppStatisticsById error", e);
			return null;
		}
	}
	
	private BatchResultDTO<SparkAppStatisticsDO> querySparkAppStatisticsList(QuerySparkAppStatisticsBO query) {
		
		BatchResultDTO<SparkAppStatisticsDO> result = new BatchResultDTO<SparkAppStatisticsDO>();
		
		try {
			List<SparkAppStatisticsDO> list = sparkAppStatisticsDao.selectSparkAppStatisticsList(query);
			result.setModule(list);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("查询列表失败");
			log.error("SparkAppStatisticsServiceImpl querySparkAppStatisticsList error", e);
		}
		return result;
	}
	
	private BatchResultDTO<SparkAppStatisticsDO> querySparkAppStatisticsPage(QuerySparkAppStatisticsBO query) {
		
		BatchResultDTO<SparkAppStatisticsDO> result = new BatchResultDTO<SparkAppStatisticsDO>();
		
		try {
			int count = sparkAppStatisticsDao.selectSparkAppStatisticsCount(query);
			query.setRecord(count);
			
			// 没数据
			if (count < 1) {
				result.setSuccess(true);
				return result;
			}
			// 查询页面超过最大页码
			if (query.getPageNo() > query.getTotalPages()) {
				result.setSuccess(true);
				return result;
			}
			
			List<SparkAppStatisticsDO> list = sparkAppStatisticsDao.selectSparkAppStatisticsList(query);
			result.setModule(list);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("分页查询失败");
			log.error("SparkAppStatisticsServiceImpl querySparkAppStatisticsPage error", e);
		}
		return result;
	}
	
	private BaseResultDTO createSparkAppStatistics(SparkAppStatisticsDO sparkAppStatistics) {
		
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			sparkAppStatisticsDao.insertSparkAppStatistics(sparkAppStatistics);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("添加失败");
			log.error("SparkAppStatisticsServiceImpl createSparkAppStatistics error", e);
		}
		return result;
	}
	
	private BaseResultDTO modifySparkAppStatistics(SparkAppStatisticsDO sparkAppStatistics) {
		
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			//只修改设定值得字段，属于选择性修改
			sparkAppStatisticsDao.updateSparkAppStatisticsSelective(sparkAppStatistics);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("修改失败");
			log.error("SparkAppStatisticsServiceImpl modifySparkAppStatistics error", e);
		}
		return result;
	}
	
	private BaseResultDTO modifySparkAppStatisticsCompletely(SparkAppStatisticsDO sparkAppStatistics) {
		
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			//不断字段是否非空，都进行修改，属于完全修改
			sparkAppStatisticsDao.updateSparkAppStatistics(sparkAppStatistics);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("修改失败");
			log.error("SparkAppStatisticsServiceImpl modifySparkAppStatisticsCompletely error", e);
		}
		return result;
	}
}