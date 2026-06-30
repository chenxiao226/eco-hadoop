package org.hit.monitor.service;


import java.io.IOException;
import java.util.Map;

import javax.management.MalformedObjectNameException;

public interface Neo4jService {
	
	public Map<String,Object> fetchKernelMetrics() throws MalformedObjectNameException, IOException;
	public Map<String,Object> fetchPageCacheMetrics() throws MalformedObjectNameException, IOException;
	public Map<String,Object> fetchPrimitiveCount() throws MalformedObjectNameException, IOException;
}
