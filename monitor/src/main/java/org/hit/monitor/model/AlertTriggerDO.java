package org.hit.monitor.model;

/**
 * @ClassName: AlertTrigger
 * 
 */
public class AlertTriggerDO {

	private Long id;//  ( 主键 )
	private String host;//  
	private Long triggerId;//  
	private String triggerDes;//  
	private String severity;//  
	private Integer status;//  
	private String info;//  
	private Long time;//  
	
	/**获取  ( 主键 )  */
	public Long getId(){
		return id;
	}
	
	/**设定  ( 主键 )  */
	public void setId(Long id){
		this.id = id;
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
	public Long getTriggerId(){
		return triggerId;
	}
	
	/**设定    */
	public void setTriggerId(Long triggerId){
		this.triggerId = triggerId;
	}
	
	/**获取    */
	public String getTriggerDes(){
		return triggerDes;
	}
	
	/**设定    */
	public void setTriggerDes(String triggerDes){
		this.triggerDes = triggerDes;
	}
	
	/**获取    */
	public String getSeverity(){
		return severity;
	}
	
	/**设定    */
	public void setSeverity(String severity){
		this.severity = severity;
	}
	
	/**获取    */
	public Integer getStatus(){
		return status;
	}
	
	/**设定    */
	public void setStatus(Integer status){
		this.status = status;
	}
	
	/**获取    */
	public String getInfo(){
		return info;
	}
	
	/**设定    */
	public void setInfo(String info){
		this.info = info;
	}
	
	/**获取    */
	public Long getTime(){
		return time;
	}
	
	/**设定    */
	public void setTime(Long time){
		this.time = time;
	}
	
}