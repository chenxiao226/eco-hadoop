package org.hit.monitor.dao;

import org.hit.monitor.model.AlertLogDO;
import org.hit.monitor.model.TaskSubmitDO;

//import org.apache.ibatis.annotations.Mapper;
public interface TaskSubmitDao {

	/** 添加 **/
	public int insertTaskSubmit(TaskSubmitDO taskSubmitDO);


}