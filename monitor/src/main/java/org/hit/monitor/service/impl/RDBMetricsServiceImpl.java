package org.hit.monitor.service.impl;

import java.util.List;

import org.hit.monitor.dao.RDBMetricsDao;
import org.hit.monitor.model.RDBProcess;
import org.hit.monitor.service.RDBMetricsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("RDBMetricsService")
public class RDBMetricsServiceImpl implements RDBMetricsService {
	@Autowired
	private RDBMetricsDao rDBMetricsDao;
	
	private long time = 0;
	private long questions =0;
	private long result = 0;
	@Override
	public int getUserNum() {
		// TODO Auto-generated method stub
		return rDBMetricsDao.selectUserList().size();
	}

	@Override
	public String fetchBufferPoolPagesTotal() {
		// TODO Auto-generated method stub
		return rDBMetricsDao.showBufferPoolPagesTotal().getValue();
	}

	@Override
	public List<RDBProcess> fetchProcess() {
		// TODO Auto-generated method stub
		return rDBMetricsDao.showProcessList();
	}

	@Override
	public long fetchQPS() {
		result= (Long.valueOf(rDBMetricsDao.showQuestions().getValue())-questions)/(Long.valueOf(rDBMetricsDao.showTime().getValue())-time);
		time=Long.valueOf(rDBMetricsDao.showTime().getValue());
		questions=Long.valueOf(rDBMetricsDao.showQuestions().getValue());

		return questions/time;
	}

	@Override
	public double fetchBuffer() {
		double reads=Double.parseDouble(rDBMetricsDao.showInnodbBufferPoolReads().getValue()) ;
		double requests=Double.parseDouble(rDBMetricsDao.showInnodbBufferPoolReadRequests().getValue()) ;
		return (1-reads/requests);
	}

	@Override
	public String fetchTime() {
		// TODO Auto-generated method stub
		return rDBMetricsDao.showTime().getValue();
	}
	
	
}
