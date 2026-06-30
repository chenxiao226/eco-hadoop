package org.hit.monitor.model;

/**
 * @ClassName: SparkApp statistics
 */
public class SparkAppStatisticsDO {
	
	private Integer id;//  ( 主键 )
	private Integer appRunning;//  
	private Integer appCompleted;//  
	private Long lastFetchTime;//  
	private Integer version;//
	
	/** 获取  ( 主键 )  */
	public Integer getId() {
		return id;
	}
	
	/** 设定  ( 主键 )  */
	public void setId(Integer id) {
		this.id = id;
	}
	
	/** 获取    */
	public Integer getAppRunning() {
		return appRunning;
	}
	
	/** 设定    */
	public void setAppRunning(Integer appRunning) {
		this.appRunning = appRunning;
	}
	
	/** 获取    */
	public Integer getAppCompleted() {
		return appCompleted;
	}
	
	/** 设定    */
	public void setAppCompleted(Integer appCompleted) {
		this.appCompleted = appCompleted;
	}
	
	/** 获取    */
	public Long getLastFetchTime() {
		return lastFetchTime;
	}
	
	/** 设定    */
	public void setLastFetchTime(Long lastFetchTime) {
		this.lastFetchTime = lastFetchTime;
	}
	
	public Integer getVersion() {
		return version;
	}
	
	public void setVersion(Integer version) {
		this.version = version;
	}
	
	@Override
	public String toString() {
		return "SparkAppStatisticsDO{" +
				"id=" + id +
				", appRunning=" + appRunning +
				", appCompleted=" + appCompleted +
				", lastFetchTime=" + lastFetchTime +
				", version=" + version +
				'}';
	}
}