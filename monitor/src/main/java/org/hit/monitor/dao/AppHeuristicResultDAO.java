package org.hit.monitor.dao;

import java.util.List;

import org.hit.monitor.model.YarnAppHeuristicResultDO;

public interface AppHeuristicResultDAO {
	int deleteByPrimaryKey(Integer id);

	int insert(YarnAppHeuristicResultDO record);

	int insertSelective(YarnAppHeuristicResultDO record);

	YarnAppHeuristicResultDO selectByPrimaryKey(Integer id);

	int updateByPrimaryKeySelective(YarnAppHeuristicResultDO record);

	int updateByPrimaryKey(YarnAppHeuristicResultDO record);
	
	//批量插入
	void insertBatch(List<YarnAppHeuristicResultDO> attachmentTables);
	
	//根据applicationID获得List
	List<YarnAppHeuristicResultDO> selectListByAppId(String appId);
	
}