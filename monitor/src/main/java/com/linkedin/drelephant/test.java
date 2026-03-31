package com.linkedin.drelephant;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.conf.Configuration;
import org.hit.monitor.common.CONFIG;
import org.hit.monitor.model.YarnAppResultDO;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.linkedin.drelephant.analysis.AnalyticJob;
import com.linkedin.drelephant.analysis.AnalyticJobGeneratorHadoop2;
import com.linkedin.drelephant.util.Utils;

public class test {

	private static final String FETCH_INTERVAL_KEY = "drelephant.analysis.fetch.interval";
	private static final String RETRY_INTERVAL_KEY = "drelephant.analysis.retry.interval";
	private static final String EXECUTOR_NUM_KEY = "drelephant.analysis.thread.count";

	private static final long FETCH_INTERVAL = 60 * 1000; // Interval between
	// fetches 间隔
	private static final long RETRY_INTERVAL = 60 * 1000; // Interval between
	// retries 间隔
	private static final int EXECUTOR_NUM = 5; // The number of executor threads
	// to analyse the jobs

	private static ThreadPoolExecutor _threadPoolExecutor; // 线程池

	public static void main(String[] args) throws Exception {
		
		
		String  _resourceManagerAddress = CONFIG.YARN_RESOURCE_MANAGER.substring(7, 26);
		
		System.out.println(_resourceManagerAddress);
		
		String jhistoryAddr = CONFIG.MAPREDUCE_HISTORY_SERVER.substring(7, 27);
		
		System.out.println(jhistoryAddr);

		
	    //InputStream in = test.getClass().getClassLoader().getResourceAsStream("D:\\GeneralConf.xml");
		
		
		String string = "/E:/chengxu/eclipse-all/apache-tomcat-7.0.77/wtpwebapps/HitClusterOP/WEB-INF/classes/";
		
		String string2 = string.substring(0, string.length()-17);
		
		System.out.println(string2);
		
		
		//String yarn = CONFIG.RESOURCE_MANAGER;
		String yarn = "yarn";
		
		System.out.println(yarn);
		
		Configuration configuration = ElephantContext.instance().getGeneralConf();

		int _executorNum = Utils.getNonNegativeInt(configuration, EXECUTOR_NUM_KEY, EXECUTOR_NUM);
		System.out.println(_executorNum);
		long _fetchInterval = Utils.getNonNegativeLong(configuration, FETCH_INTERVAL_KEY, FETCH_INTERVAL);
		long _retryInterval = Utils.getNonNegativeLong(configuration, RETRY_INTERVAL_KEY, RETRY_INTERVAL);

		System.out.println(_fetchInterval);

		AnalyticJobGeneratorHadoop2 _analyticJobGenerator;

		// if (HadoopSystemContext.isHadoop2Env()) {
		{
			_analyticJobGenerator = new AnalyticJobGeneratorHadoop2(); // 构造函数
		}
		try {
			_analyticJobGenerator.configure(ElephantContext.instance().getGeneralConf());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		List<AnalyticJob> todos = null;
		try {

			todos = _analyticJobGenerator.fetchAnalyticJobs(); // 获取JOB的过程
			System.out.println("kg->"+todos.size());

		} catch (Exception e) {
			// Wait for a while before retry
			// continue;
			System.out.println("失败");
		}

		ThreadFactory factory = new ThreadFactoryBuilder().setNameFormat("dr-el-executor-thread-%d").build();
		_threadPoolExecutor = new ThreadPoolExecutor(_executorNum, _executorNum, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), factory);

		// 每一个JOB提交一个线程去分析
		//for (AnalyticJob analyticJob : todos) {
			//_threadPoolExecutor.submit(new ExecutorJob(analyticJob));
	   //	}
		YarnAppResultDO resultDO =  todos.get(0).getAnalysis();
		
		System.out.println(resultDO.getName());
//		
	}

	
//	private class ExecutorJob implements Runnable {
//
//		private AnalyticJob _analyticJob;
//
//		ExecutorJob(AnalyticJob analyticJob) {
//			_analyticJob = analyticJob;
//		}
//
//		@Override
//		public void run() {
//			try {
//				String analysisName = String.format("%s %s", _analyticJob.getAppType().getName(),
//						_analyticJob.getAppId());
//				long analysisStartTimeMillis = System.currentTimeMillis();
//
//				// AppResult result = _analyticJob.getAnalysis(); //
//				// application级别的分析结果
//
//				// result.save(); // 保存数据库
//
//			} catch (Exception e) {
//
//			}
//		}
//	}
}
