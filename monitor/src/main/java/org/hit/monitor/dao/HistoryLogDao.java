package org.hit.monitor.dao;

import org.hit.monitor.bo.QueryHistoryLogBO;
import org.hit.monitor.model.HistoryLogDO;

import java.util.List;

public interface HistoryLogDao {

	/** 根据主键查询 **/
	public HistoryLogDO selectHistoryLogById(Long id);
	
	/** 根据主键查询是否存在 **/
	public int selectHistoryLogCountById(Long id);

	/** 查询一个，必须传入能唯一确定一个的参数 **/
	public HistoryLogDO selectOne(QueryHistoryLogBO query);
	
	/** 查询列表，一次最多查出1000条 **/
	public List<HistoryLogDO> selectHistoryLogList(QueryHistoryLogBO query);
	
	/** 分页查找 **/
	public List<HistoryLogDO> selectHistoryLogPage(QueryHistoryLogBO query);
	
	/** 分页计数 **/
	public int selectHistoryLogCount(QueryHistoryLogBO query);

	/** 添加 **/
	public int insertHistoryLog(HistoryLogDO historyLog);

	/** 完全修改 **/
	public int updateHistoryLog(HistoryLogDO historyLog);

	/** 选择性修改 **/
	public int updateHistoryLogSelective(HistoryLogDO historyLog);

	/** 删除 **/
	public int deleteHistoryLog(HistoryLogDO historyLog);
}