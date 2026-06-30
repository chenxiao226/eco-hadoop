package org.hit.monitor.dao;

import java.util.List;

import org.hit.monitor.model.MysqlMetrics;
import org.hit.monitor.model.RDBProcess;



public interface RDBMetricsDao {
	/*获取用户列表*/
	public List<String> selectUserList();
	
	/*获取缓冲池总页数*/
	public MysqlMetrics showBufferPoolPagesTotal();
	
	/*获取进程情况*/
	public List<RDBProcess> showProcessList();
	
	/*获取时间*/
	public MysqlMetrics showTime();
	
	/*获取查询量*/
	public MysqlMetrics showQuestions();
	
	/*获取 innodb_buffer_pool_reads*/
	public MysqlMetrics showInnodbBufferPoolReads();
	
	/*获取Innodb_buffer_pool_read_requests*/
	public MysqlMetrics showInnodbBufferPoolReadRequests();
}
