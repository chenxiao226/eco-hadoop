package org.hit.monitor.model;

/**
 * @ClassName: AlertLog
 * @date 2017-8-1
 * 
 */
public class AlertLogDO {

	private Long id;//  ( 主键 )
	private Long occurTime;//  
	private String host;//  
	private String user;//  
	private String software;//  
	private String component;//  
	private String content;//  
	private String faultType;//  
	
	/**获取  ( 主键 )  */
	public Long getId(){
		return id;
	}
	
	/**设定  ( 主键 )  */
	public void setId(Long id){
		this.id = id;
	}
	
	/**获取    */
	public Long getOccurTime(){
		return occurTime;
	}
	
	/**设定    */
	public void setOccurTime(Long occurTime){
		this.occurTime = occurTime;
	}
	
	/**获取    */
	public String getHost(){
		return host;
	}
	
	/**设定    */
	public void setHost(String host){
		this.host = host;
	}
	
	/**获取    */
	public String getUser(){
		return user;
	}
	
	/**设定    */
	public void setUser(String user){
		this.user = user;
	}
	
	/**获取    */
	public String getSoftware(){
		return software;
	}
	
	/**设定    */
	public void setSoftware(String software){
		this.software = software;
	}
	
	/**获取    */
	public String getComponent(){
		return component;
	}
	
	/**设定    */
	public void setComponent(String component){
		this.component = component;
	}
	
	/**获取    */
	public String getContent(){
		return content;
	}
	
	/**设定    */
	public void setContent(String content){
		this.content = content;
	}
	
	/**获取    */
	public String getFaultType(){
		return faultType;
	}
	
	/**设定    */
	public void setFaultType(String faultType){
		this.faultType = faultType;
	}
	
}