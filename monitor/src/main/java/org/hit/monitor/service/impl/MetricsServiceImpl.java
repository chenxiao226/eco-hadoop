package org.hit.monitor.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.htrace.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.hit.monitor.bo.QueryMetricsBO;
import org.hit.monitor.bo.QueryMetricsDefineBO;
import org.hit.monitor.common.*;
import org.hit.monitor.dao.MetricsDataDao;
import org.hit.monitor.dao.MetricsDefineDao;
import org.hit.monitor.model.MetricsDataDO;
import org.hit.monitor.model.MetricsDefineDO;
import org.hit.monitor.service.MetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service("metricsServiceViaGmetad")
public class MetricsServiceImpl implements MetricsService {

	private Logger log = Logger.getLogger(this.getClass());

	@Autowired
	private MetricsDataDao metricsDataDao;

	@Autowired
	private MetricsDefineDao metricsDefineDao;

	private final long ONE_HOUR = 60 * 60; // 秒为单位

	@Override
	public ResultDTO<MetricsDefineDO> fetchMetricsDefineByName(String name) {

		ResultDTO<MetricsDefineDO> result = new ResultDTO<MetricsDefineDO>();

		if (StringUtils.isBlank(name)) {
			return result.returnError("参数错误");
		}

		try {
			QueryMetricsDefineBO filter = new QueryMetricsDefineBO();
			filter.setName(name);
			MetricsDefineDO metricsDefine = metricsDefineDao.selectOne(filter);
			return result.returnSuccess(metricsDefine);
		} catch (Exception e) {
			log.error("MetricsServiceImpl fetchMetricsDefineByName error", e);
			return result.returnError("根据名称查询指标定义失败");
		}
	}

	@Override
	public BatchResultDTO<MetricsDefineDO> queryMetricsDefineList(QueryMetricsDefineBO filter) {

		BatchResultDTO<MetricsDefineDO> result = new BatchResultDTO<MetricsDefineDO>();

		if (filter == null) {
			return result.returnError("参数错误");
		}

		try {
			List<MetricsDefineDO> list = metricsDefineDao.selectMetricsDefineList(filter);
			return result.returnSuccess(list);
		} catch (Exception e) {
			log.error("MetricsDefineServiceImpl queryMetricsDefineList error", e);
			return result.returnError("查询列表失败");
		}
	}

	@Override
	public BatchResultDTO<MetricsDefineDO> queryMetricsDefinePage(QueryMetricsDefineBO query) {

		BatchResultDTO<MetricsDefineDO> result = new BatchResultDTO<MetricsDefineDO>();

		if (query == null) {
			return result.returnError("参数错误");
		}

		try {
			int count = metricsDefineDao.selectMetricsDefineCount(query);
			query.setRecord(count);

			// 没数据
			if (count < 1) {
				return result.returnSuccess(null);
			}
			// 查询页面超过最大页码
			if (query.getPageNo() > query.getTotalPages()) {
				return result.returnSuccess(null);
			}

			List<MetricsDefineDO> list = metricsDefineDao.selectMetricsDefineList(query);
			return result.returnSuccess(list);
		} catch (Exception e) {
			log.error("MetricsDefineServiceImpl queryMetricsDefinePage error", e);
			return result.returnError("分页查询失败");
		}
	}

	@Override
	public BatchResultDTO<MetricsDataDO> fetchData(Metrics metrics, QueryMetricsBO filter) {

		System.out.println("serviceImp调用：metrics：" + metrics);
		if (metrics == null) {
			BatchResultDTO<MetricsDataDO> result = new BatchResultDTO<MetricsDataDO>();
			return result.returnError("参数错误");
		}
//		System.out.println("打印一下传过去的表名字参数"+ metrics.metricsName() );
		return fetchData(metrics.metricsName(), filter);
	}

	@Override
	public BatchResultDTO<MetricsDataDO> fetchTopNData(Metrics metrics, QueryMetricsBO filter) {
		return null;
	}

	private BatchResultDTO<MetricsDataDO> fetchData(String metricsName, QueryMetricsBO filter) {
//		System.out.println("打印一下接到的表名字参数"+ metricsName );
		BatchResultDTO<MetricsDataDO> result = new BatchResultDTO<MetricsDataDO>();

		// 参数校验
		if (metricsName == null || filter == null) {
			return result.returnError("参数错误");
		}

		if (filter == null)
			filter = new QueryMetricsBO();

		try {
			// 设定要查询的表，重要
			filter.setMetricsName(metricsName);

			// 如果起始时间、结束时间、查询条数限制都没有指定，则默认查询最近1小时的数据
			if (filter.getStart() == null && filter.getEnd() == null && filter.getLimit() == null) {
				long now = System.currentTimeMillis() / 1000;
				filter.setStart(now - ONE_HOUR);
				filter.setEnd(now);
			}
			List<MetricsDataDO> list = metricsDataDao.selectMetricsList(filter);
			return result.returnSuccess(list);
		} catch (Exception e) {
			log.error("MetricsServiceImpl fetchData error", e);
			return result.returnError("查询列表失败");
		}
	}

	@Override
	public BatchResultDTO<MetricsDataDO> fetchDataPage(Metrics metrics, QueryMetricsBO filter) {

		BatchResultDTO<MetricsDataDO> result = new BatchResultDTO<MetricsDataDO>();

		// 参数校验
		if (metrics == null) {
			return result.returnError("参数错误");
		}

		if (filter == null)
			filter = new QueryMetricsBO();

		try {
			// 设定要查询的表，重要
			filter.setMetricsName(metrics.metricsName());

			int count = metricsDataDao.selectMetricsCount(filter);
			filter.setRecord(count);

			// 没数据
			if (count < 1) {
				result.setSuccess(true);
				return result;
			}
			// 查询页面超过最大页码
			if (filter.getPageNo() > filter.getTotalPages()) {
				result.setSuccess(true);
				return result;
			}

			List<MetricsDataDO> list = metricsDataDao.selectMetricsPage(filter);
			return result.returnSuccess(list);
		} catch (Exception e) {
			log.error("MetricsServiceImpl fetchDataPage error", e);
			return result.returnError("分页查询失败");
		}
	}

	@Override
	public BaseResultDTO removeMetricsData(QueryMetricsBO filter) {

		BaseResultDTO result = new BaseResultDTO();
		try {
			if (filter.getMetricsName() == null || filter.getStart() == null || filter.getEnd() == null) {
				result.returnError("参数错误");
			}
			metricsDataDao.deleteMetricsData(filter);
			return result.returnSuccess();
		} catch (Exception e) {
			log.error("MetricsServiceImpl removeMetricsData error", e);
			return result.returnError("删除指标数据失败");
		}
	}

	@Override
	public ResultDTO<String> fetchMetricsJSONByAPI(String uri) {

//		测试
//		System.out.println("fetchMetricsJSONByAPI调用");
		ResultDTO<String> resultDTO = new ResultDTO<String>();
		try {
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(uri);

//			// 打印请求信息
//			System.out.println("fetchMetricsJSONByAPI请求 URL: " + httpGet.getURI());
//			System.out.println("fetchMetricsJSONByAPI请求 Headers: " + Arrays.toString(httpGet.getAllHeaders()));

			HttpGet get = new HttpGet(uri);
			get.setHeader("Accept-Charset", "utf-8");
			get.setHeader("Accept", "application/json");
			get.setHeader("Cache-Control", "no-cache");
			HttpResponse res = httpClient.execute(get);
//			//打印响应
//			System.out.println("fetchMetricsJSONByAPI调用HTTP 状态码: " + res.getStatusLine().getStatusCode());

			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(res.getEntity());// 返回json格式：
//				System.out.println(result);
//				System.out.println( "fetchMetricsJSONByAPI返回json格式" + result);
				return resultDTO.returnSuccess(result);
			} else {
				return resultDTO.returnError("获取API数据出错");
			}
		} catch (Exception e) {
			log.error("获取集群指标异常", e);
			return resultDTO.returnError("获取API数据异常");
		}
	}

}