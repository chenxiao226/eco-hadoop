package org.hit.monitor.dao;

import java.util.List;

import org.hit.monitor.bo.QuerySparkAppStatisticsBO;
import org.hit.monitor.model.SparkAppStatisticsDO;

public interface SparkAppStatisticsDao {

	/** 根据主键查询 **/
	public SparkAppStatisticsDO selectSparkAppStatisticsById(Long id);
	
	/** 根据主键查询是否存在 **/
	public int selectSparkAppStatisticsCountById(Long id);

	/** 查询一个，必须传入能唯一确定一个的参数 **/
	public SparkAppStatisticsDO selectOne(QuerySparkAppStatisticsBO query);
	
	/** 查询列表，一次最多查出1000条 **/
	public List<SparkAppStatisticsDO> selectSparkAppStatisticsList(QuerySparkAppStatisticsBO query);
	
	/** 分页查找 **/
	public List<SparkAppStatisticsDO> selectSparkAppStatisticsPage(QuerySparkAppStatisticsBO query);
	
	/** 分页计数 **/
	public int selectSparkAppStatisticsCount(QuerySparkAppStatisticsBO query);

	/** 添加 **/
	public int insertSparkAppStatistics(SparkAppStatisticsDO sparkAppStatistics);

	/** 完全修改 **/
	public int updateSparkAppStatistics(SparkAppStatisticsDO sparkAppStatistics);

	/** 选择性修改 **/
	public int updateSparkAppStatisticsSelective(SparkAppStatisticsDO sparkAppStatistics);

	/** 删除 **/
	public int deleteSparkAppStatistics(SparkAppStatisticsDO sparkAppStatistics);
}