package org.hit.monitor.model;

/**
 * @ClassName: SparkRunningApp
 * 
 */
public class SparkRunningAppDO {

	private String appId;//  ( 主键 )
	
	/**获取  ( 主键 )  */
	public String getAppId(){
		return appId;
	}
	
	/**设定  ( 主键 )  */
	public void setAppId(String appId){
		this.appId = appId;
	}
	
}