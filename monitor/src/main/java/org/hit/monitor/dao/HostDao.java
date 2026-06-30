package org.hit.monitor.dao;

import org.hit.monitor.bo.QueryHostsBO;
import org.hit.monitor.model.HostDO;

import java.util.List;

public interface HostDao {

	/** 根据主键查询 **/
	public HostDO selectHostsById(Long hostid);
	
	/** 根据主键查询是否存在 **/
	public int selectHostsCountById(Long hostid);

	/** 查询一个，必须传入能唯一确定一个的参数 **/
	public HostDO selectOne(QueryHostsBO query);
	
	/** 查询列表，一次最多查出1000条 **/
	public List<HostDO> selectHostsList(QueryHostsBO query);
	
	/** 分页查找 **/
	public List<HostDO> selectHostsPage(QueryHostsBO query);
	
	/** 分页计数 **/
	public int selectHostsCount(QueryHostsBO query);
}