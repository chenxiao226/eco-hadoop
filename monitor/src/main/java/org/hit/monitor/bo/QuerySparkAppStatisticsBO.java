package org.hit.monitor.bo;

/**
 * @ClassName: SparkApp statistics查询BO
 * @date 2017-7-26
 * 
 */
public class QuerySparkAppStatisticsBO extends BaseQueryBO{

	private Integer id;//  ( 主键 )
	private Integer appRunning;//  
	private Integer appCompleted;//  
	private Long lastFetchTime;//  
	
	/**  获取  ( 主键 )  **/
	public Integer getId(){
		return id;
	}
	
	/**  设定  ( 主键 )  **/
	public void setId(Integer id){
		this.id = id;
	}
	
	/**  获取    **/
	public Integer getAppRunning(){
		return appRunning;
	}
	
	/**  设定    **/
	public void setAppRunning(Integer appRunning){
		this.appRunning = appRunning;
	}
	
	/**  获取    **/
	public Integer getAppCompleted(){
		return appCompleted;
	}
	
	/**  设定    **/
	public void setAppCompleted(Integer appCompleted){
		this.appCompleted = appCompleted;
	}
	
	/**  获取    **/
	public Long getLastFetchTime(){
		return lastFetchTime;
	}
	
	/**  设定    **/
	public void setLastFetchTime(Long lastFetchTime){
		this.lastFetchTime = lastFetchTime;
	}
	
}