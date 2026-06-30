package org.hit.monitor.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.hit.monitor.bo.QueryHostsBO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.dao.HostDao;
import org.hit.monitor.model.HostDO;
import org.hit.monitor.service.HostsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class HostsServiceImpl implements HostsService {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	private final HostDao hostsDao;
	
	private volatile List<HostDO> hostList = null;
	
	private final Object hostListLock = new Object();
	
	@Autowired
	public HostsServiceImpl(HostDao hostsDao) {
		this.hostsDao = hostsDao;
	}
	
	@Override
	public List<HostDO> queryHostsList() {
		try {
			if (hostList == null) {
				synchronized (hostListLock) {
					if (hostList == null) {
						QueryHostsBO query = new QueryHostsBO();
						query.setAvailable(1); //此字段用于标记是否是有效的节点
						List<HostDO> list = hostsDao.selectHostsList(query);
						this.hostList = Collections.unmodifiableList(list);
					}
				}
			}
			return this.hostList;
		} catch (Exception e) {
			log.error("HostsServiceImpl queryHostsList error", e);
			return null;
		}
	}
	
	@Override
	public HostDO queryHostByName(String hostName) {
		try {
			if (StringUtils.isEmpty(hostName)) {
				return null;
			}
			QueryHostsBO queryHostsBO = new QueryHostsBO();
			queryHostsBO.setName(hostName);
			return hostsDao.selectOne(queryHostsBO);
		} catch (Exception e) {
			log.error("HostsServiceImpl queryHostByName error", e);
			return null;
		}
	}
	
	@Override
	public BatchResultDTO<HostDO> queryHostsPage(QueryHostsBO query) {
		
		BatchResultDTO<HostDO> result = new BatchResultDTO<HostDO>();
		
		try {
			int count = hostsDao.selectHostsCount(query);
			query.setRecord(count);
			
			// 没数据
			if (count < 1) {
				result.setSuccess(true);
				return result;
			}
			// 查询页面超过最大页码
			if (query.getPageNo() > query.getTotalPages()) {
				result.setSuccess(true);
				return result;
			}
			
			List<HostDO> list = hostsDao.selectHostsList(query);
			result.setModule(list);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("分页查询失败");
			log.error("HostsServiceImpl queryHostsPage error", e);
		}
		return result;
	}
}