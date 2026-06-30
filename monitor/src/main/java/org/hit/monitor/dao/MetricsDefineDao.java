package org.hit.monitor.dao;

import java.util.List;

import org.hit.monitor.bo.QueryMetricsDefineBO;
import org.hit.monitor.model.MetricsDefineDO;

public interface MetricsDefineDao {

	/** 根据主键查询 **/
	public MetricsDefineDO selectMetricsDefineById(Long metricsId);
	
	/** 根据主键查询是否存在 **/
	public int selectMetricsDefineCountById(Long metricsId);

	/** 查询一个，必须传入能唯一确定一个的参数 **/
	public MetricsDefineDO selectOne(QueryMetricsDefineBO query);
	
	/** 查询列表，一次最多查出1000条 **/
	public List<MetricsDefineDO> selectMetricsDefineList(QueryMetricsDefineBO query);
	
	/** 分页查找 **/
	public List<MetricsDefineDO> selectMetricsDefinePage(QueryMetricsDefineBO query);
	
	/** 分页计数 **/
	public int selectMetricsDefineCount(QueryMetricsDefineBO query);

	/** 添加 **/
	public int insertMetricsDefine(MetricsDefineDO metricsDefine);

	/** 完全修改 **/
	public int updateMetricsDefine(MetricsDefineDO metricsDefine);

	/** 选择性修改 **/
	public int updateMetricsDefineSelective(MetricsDefineDO metricsDefine);

	/** 删除 **/
	public int deleteMetricsDefine(MetricsDefineDO metricsDefine);
}