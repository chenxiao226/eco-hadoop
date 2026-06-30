package com.linkedin.drelephant;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.log4j.Logger;
import org.hit.monitor.model.YarnAppResultDO;
import org.hit.monitor.service.impl.YarnAppResultServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.linkedin.drelephant.analysis.AnalyticJob;
import com.linkedin.drelephant.analysis.AnalyticJobGenerator;
import com.linkedin.drelephant.analysis.AnalyticJobGeneratorHadoop2;
import com.linkedin.drelephant.util.Utils;

@Component
public class ElephantRunner implements Runnable {

	@Autowired
	private YarnAppResultServiceImpl yarnAppResultServiceImpl;
	
	private static final Logger logger = Logger.getLogger(ElephantRunner.class);

	private static final long FETCH_INTERVAL = 60 * 1000; // Interval between
															// fetches 间隔
	private static final long RETRY_INTERVAL = 60 * 1000; // Interval between
															// retries 间隔
	private static final int EXECUTOR_NUM = 5; // The number of executor threads
												// to analyse the jobs

	private static final String FETCH_INTERVAL_KEY = "drelephant.analysis.fetch.interval";
	private static final String RETRY_INTERVAL_KEY = "drelephant.analysis.retry.interval";
	private static final String EXECUTOR_NUM_KEY = "drelephant.analysis.thread.count";

	private AtomicBoolean _running = new AtomicBoolean(true); // 保证原子性操作，在在这个Boolean值的变化的时候不允许在之间插入，保持操作的原子性
																// 方法

	private long lastRun;
	private long _fetchInterval;
	private long _retryInterval;
	private int _executorNum;

	//private HadoopSecurity _hadoopSecurity; // 实现HDFS的登录模块
	private ThreadPoolExecutor _threadPoolExecutor; // 线程池
	private AnalyticJobGenerator _analyticJobGenerator;// 获取分析Job的产生器。任务队列来完成Hadoop/Spark任务的收集、分析过程

	private void loadGeneralConfiguration() { // 从GeneralConf.xml中赋值

		Configuration configuration = ElephantContext.instance().getGeneralConf();

		_executorNum = Utils.getNonNegativeInt(configuration, EXECUTOR_NUM_KEY, EXECUTOR_NUM);
		_fetchInterval = Utils.getNonNegativeLong(configuration, FETCH_INTERVAL_KEY, FETCH_INTERVAL);
		_retryInterval = Utils.getNonNegativeLong(configuration, RETRY_INTERVAL_KEY, RETRY_INTERVAL);
	}

	private void loadAnalyticJobGenerator() {
		// if (HadoopSystemContext.isHadoop2Env()) //此处不用判断是否是hadoop 2.x版本
		{
			_analyticJobGenerator = new AnalyticJobGeneratorHadoop2(); // 构造函数
		}

		try {
			_analyticJobGenerator.configure(ElephantContext.instance().getGeneralConf());
		} catch (Exception e) {
			logger.error("Error occurred when configuring the analysis provider.", e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		logger.info("History service has started...");

		loadGeneralConfiguration(); // 给三个值赋值

		loadAnalyticJobGenerator(); // 载入分析Job的产生器

		ElephantContext.init(); // 比较重要的

		logger.info("executor num is " + _executorNum);
		if (_executorNum < 1) {
			throw new RuntimeException("Must have at least 1 worker thread.");
		}

		ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("dr-el-executor-thread-%d").build();
		_threadPoolExecutor = new ThreadPoolExecutor(_executorNum, _executorNum, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), factory);

		while (!Thread.currentThread().isInterrupted()) {

			lastRun = System.currentTimeMillis(); // 最近执行的时间

			logger.info("Fetching analytic job list...");

			List<AnalyticJob> todos;
			try {

				todos = _analyticJobGenerator.fetchAnalyticJobs(); // 获取JOB的过程

			} catch (Exception e) {
				logger.error("Error fetching job list. Try again later...", e);
				// Wait for a while before retry
				waitInterval(_retryInterval);
				continue;
			}
			// 每一个JOB提交一个线程去分析
			for (AnalyticJob analyticJob : todos) {
				_threadPoolExecutor.submit(new ExecutorJob(analyticJob));
			}

			int queueSize = _threadPoolExecutor.getQueue().size();
			logger.info("Job queue size is " + queueSize);
			// Wait for a while before next fetch
			waitInterval(_fetchInterval);
		}
		logger.info("Main thread is terminated.");
	}

	private class ExecutorJob implements Runnable {

		private AnalyticJob _analyticJob;

		ExecutorJob(AnalyticJob analyticJob) {
			_analyticJob = analyticJob;
		}

		@Override
		public void run() {
			try {
				String analysisName = String.format("%s %s", _analyticJob.getAppType().getName(),
						_analyticJob.getAppId());
				long analysisStartTimeMillis = System.currentTimeMillis();
				
				YarnAppResultDO resultDO = _analyticJob.getAnalysis();
				
				yarnAppResultServiceImpl.insertAppResultDetails(resultDO);
				
				long processingTime = System.currentTimeMillis() - analysisStartTimeMillis;
				logger.info(String.format("Analysis of %s took %sms", analysisName, processingTime));

			} catch (InterruptedException e) {

				logger.info("Thread interrupted");
				logger.info(e.getMessage());
				logger.info(ExceptionUtils.getStackTrace(e));
			} catch (Exception e) {
				// TODO: handle exception
				logger.error(e.getMessage());
				logger.error(ExceptionUtils.getStackTrace(e));

				if (_analyticJob != null && _analyticJob.retry()) {
					logger.error("Add analytic job id [" + _analyticJob.getAppId() + "] into the retry list.");
					_analyticJobGenerator.addIntoRetries(_analyticJob);
				} else {
					if (_analyticJob != null) {
						logger.error("Drop the analytic job. Reason: reached the max retries for application id = ["
								+ _analyticJob.getAppId() + "].");
					}
				}
			}
		}
	}

	private void waitInterval(long interval) {
		// Wait for long enough
		long nextRun = lastRun + interval;
		long waitTime = nextRun - System.currentTimeMillis();

		if (waitTime <= 0) {
			return;
		}
		try {
			Thread.sleep(waitTime);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	public void kill() {
		_running.set(false);
		if (_threadPoolExecutor != null) {
			_threadPoolExecutor.shutdownNow();
		}
	}
}
