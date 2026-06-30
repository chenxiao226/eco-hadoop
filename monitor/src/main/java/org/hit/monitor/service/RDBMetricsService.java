package org.hit.monitor.service;

import java.util.List;

import org.hit.monitor.model.RDBProcess;



public interface RDBMetricsService {
	public int getUserNum();
	
	public String  fetchBufferPoolPagesTotal();
	
	public List<RDBProcess> fetchProcess();
	
	public long fetchQPS();
	
	public double  fetchBuffer();
	
	public String fetchTime();
}
