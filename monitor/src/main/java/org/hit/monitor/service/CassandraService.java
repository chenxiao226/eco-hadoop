package org.hit.monitor.service;

import java.io.IOException;
import java.util.Map;

import javax.management.MalformedObjectNameException;

public interface CassandraService {
	
	public Map<String, Object> fetchTableCQLMetrics() throws MalformedObjectNameException, IOException;
	
	public Map<String, Object> fetchThreadPoolsMetrics() throws MalformedObjectNameException, IOException;
	
	public Map<String, Object> fetchCacheRatio() throws MalformedObjectNameException, IOException;
	
	public Object test();
	

}
