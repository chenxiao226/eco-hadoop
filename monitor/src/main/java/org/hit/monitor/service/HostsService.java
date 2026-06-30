package org.hit.monitor.service;

import org.hit.monitor.bo.QueryHostsBO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.model.HostDO;

import java.util.List;

public interface HostsService {

	/**
	 * 查询列表
	 */
	public List<HostDO> queryHostsList();
	
	/**
	 * 根据主机名查询主机
	 */
	public HostDO queryHostByName(String hostName);
	
	/**
	 * 分页查询
	 */
	public BatchResultDTO<HostDO> queryHostsPage(QueryHostsBO query);
}