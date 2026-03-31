package org.hit.monitor.service;

import org.hit.monitor.bo.QueryMetricsBO;
import org.hit.monitor.bo.QueryMetricsDefineBO;
import org.hit.monitor.common.BaseResultDTO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.Metrics;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.model.MetricsDataDO;
import org.hit.monitor.model.MetricsDefineDO;

import java.util.Map;

public interface MetricsService {

	/**
	 * 根据名称查询Metric的定义
	 *
	 * @deprecated 此为采用Zabbix获取实时数据之前的方案
	 * @param name 指标名称
	 */
	public ResultDTO<MetricsDefineDO> fetchMetricsDefineByName(String name);

	/**
	 * 查询所有Metric的定义
	 *
	 * @deprecated 此为采用Zabbix获取实时数据之前的方案
	 */
	public BatchResultDTO<MetricsDefineDO> queryMetricsDefineList(QueryMetricsDefineBO filter);

	/**
	 * 分页查询Metric的定义
	 *
	 * @deprecated 此为采用Zabbix获取实时数据之前的方案
	 */
	public BatchResultDTO<MetricsDefineDO> queryMetricsDefinePage(QueryMetricsDefineBO query);

	/**
	 * 获取Metrics数据
	 * 
	 * @param filter 数据筛选的参数，包含指标名称、起始时间、结束时间。<br />
	 *        <b>指标名称必填，如果起始时间、结束时间和限制条数都没有指定，则默认查询最近1小时的数据</b>
	 * @return 查询结果
	 */
	public BatchResultDTO<MetricsDataDO> fetchData(Metrics metrics, QueryMetricsBO filter);
	
	/**
	 * 获取Metrics数据
	 *
	 * @param filter 数据筛选的参数，包含指标名称、起始时间、结束时间。<br />
	 *        <b>指标名称必填，如果起始时间、结束时间和限制条数都没有指定，则默认查询最近1小时的数据</b>
	 * @return 查询结果
	 */
	public BatchResultDTO<MetricsDataDO> fetchTopNData(Metrics metrics, QueryMetricsBO filter);

	/**
	 * 分页获取Metrics数据
	 * 
	 * @param filter 数据筛选的参数，包含指标名称、起始时间、结束时间。<b>指标名称必填</b>
	 * @return 查询结果
	 */
	public BatchResultDTO<MetricsDataDO> fetchDataPage(Metrics metrics, QueryMetricsBO filter);

	/**
	 * 删除过期的Metrics数据
	 * 
	 * @param filter 数据筛选的参数，包含表名、起始时间、结束时间。<b>全为必填项</b>
	 */
	public BaseResultDTO removeMetricsData(QueryMetricsBO filter);

	/**
	 * 通过Hadoop、Yarn等提供的API获得JSON数据
	 *
	 * @param uri API地址
	 * @return JSON字符串
	 */
	public ResultDTO<String> fetchMetricsJSONByAPI(String uri);

}
