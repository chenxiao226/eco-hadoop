package org.hit.monitor.dao;

import java.util.List;

import org.hit.monitor.model.YarnAppHeuristicResultDetailsDO;

public interface AppHeuristicResultDetailsDAO {
    int deleteByPrimaryKey(YarnAppHeuristicResultDetailsDO key);

    int insert(YarnAppHeuristicResultDetailsDO record);

    int insertSelective(YarnAppHeuristicResultDetailsDO record);

    YarnAppHeuristicResultDetailsDO selectByPrimaryKey(YarnAppHeuristicResultDetailsDO key);

    int updateByPrimaryKeySelective(YarnAppHeuristicResultDetailsDO record);

    int updateByPrimaryKeyWithBLOBs(YarnAppHeuristicResultDetailsDO record);

    int updateByPrimaryKey(YarnAppHeuristicResultDetailsDO record);
    
    List<YarnAppHeuristicResultDetailsDO> selectListByAppHeuristicId(Integer Id);
    
}