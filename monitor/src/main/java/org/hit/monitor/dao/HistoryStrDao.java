package org.hit.monitor.dao;

import org.hit.monitor.bo.QueryHistoryStrBO;
import org.hit.monitor.model.HistoryStrDO;

import java.util.List;

public interface HistoryStrDao {

	/** 根据主键查询 **/
	public HistoryStrDO selectHistoryStrById(Long itemid);
	
	/** 根据主键查询是否存在 **/
	public int selectHistoryStrCountById(Long itemid);

	/** 查询一个，必须传入能唯一确定一个的参数 **/
	public HistoryStrDO selectOne(QueryHistoryStrBO query);
	
	/** 查询列表，一次最多查出1000条 **/
	public List<HistoryStrDO> selectHistoryStrList(QueryHistoryStrBO query);
	
	/** 分页查找 **/
	public List<HistoryStrDO> selectHistoryStrPage(QueryHistoryStrBO query);
	
	/** 分页计数 **/
	public int selectHistoryStrCount(QueryHistoryStrBO query);

	/** 添加 **/
	public int insertHistoryStr(HistoryStrDO historyStr);

	/** 完全修改 **/
	public int updateHistoryStr(HistoryStrDO historyStr);

	/** 选择性修改 **/
	public int updateHistoryStrSelective(HistoryStrDO historyStr);

	/** 删除 **/
	public int deleteHistoryStr(HistoryStrDO historyStr);
}