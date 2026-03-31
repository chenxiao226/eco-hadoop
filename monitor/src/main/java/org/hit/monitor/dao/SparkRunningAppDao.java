package org.hit.monitor.dao;

import org.apache.ibatis.annotations.Param;

import java.util.List;


public interface SparkRunningAppDao {
	
	/** 查询列表，一次最多查出1000条 **/
	public List<String> selectSparkRunningAppList();
	
	/** 添加 **/
	public int insertSparkRunningApp(@Param("appIds") List<String> appIds);
	
	/** 删除 **/
	public int deleteSparkRunningApp(@Param("appIds") List<String> appId);
}