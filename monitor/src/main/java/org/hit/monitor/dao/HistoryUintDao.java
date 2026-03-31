package org.hit.monitor.dao;

import org.hit.monitor.bo.QueryHistoryUintBO;
import org.hit.monitor.model.HistoryUintDO;

import java.util.List;

public interface HistoryUintDao {

	/** 根据主键查询 **/
	public HistoryUintDO selectHistoryUintById(Long itemid);
	
	/** 根据主键查询是否存在 **/
	public int selectHistoryUintCountById(Long itemid);

	/** 查询一个，必须传入能唯一确定一个的参数 **/
	public HistoryUintDO selectOne(QueryHistoryUintBO query);
	
	/** 查询列表，一次最多查出1000条 **/
	public List<HistoryUintDO> selectHistoryUintList(QueryHistoryUintBO query);
	
	/** 分页查找 **/
	public List<HistoryUintDO> selectHistoryUintPage(QueryHistoryUintBO query);
	
	/** 分页计数 **/
	public int selectHistoryUintCount(QueryHistoryUintBO query);

	/** 添加 **/
	public int insertHistoryUint(HistoryUintDO historyUint);

	/** 完全修改 **/
	public int updateHistoryUint(HistoryUintDO historyUint);

	/** 选择性修改 **/
	public int updateHistoryUintSelective(HistoryUintDO historyUint);

	/** 删除 **/
	public int deleteHistoryUint(HistoryUintDO historyUint);
}