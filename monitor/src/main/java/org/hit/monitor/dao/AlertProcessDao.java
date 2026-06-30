package org.hit.monitor.dao;

import org.hit.monitor.bo.QueryAlertProcessBO;
import org.hit.monitor.model.AlertProcessDO;

import java.util.List;

public interface AlertProcessDao {
	/** 根据主键查询 **/
	public AlertProcessDO selectAlertProcessByMP(String machine,String process);

	/** 查询一个，必须传入能唯一确定一个的参数 **/
	public AlertProcessDO selectOne(QueryAlertProcessBO query);
	
	/** 查询列表，一次最多查出1000条 **/
	public List<AlertProcessDO> selectAlertProcessList(QueryAlertProcessBO query);
	
	/** 分页查找 **/
	public List<AlertProcessDO> selectAlertProcessPage(QueryAlertProcessBO query);
	
	/** 分页计数 **/
	public int selectAlertProcessCount(QueryAlertProcessBO query);

	/** 添加 **/
	public int insertAlertProcess(AlertProcessDO alertProcess);

	/** 完全修改 **/
	public int updateAlertProcess(AlertProcessDO alertProcess);

	/** 选择性修改 **/
	public int updateAlertProcessSelective(AlertProcessDO alertProcess);

	/** 删除 **/
	public int deleteAlertProcess(AlertProcessDO alertProcess);
}