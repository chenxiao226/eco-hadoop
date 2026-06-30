package org.hit.monitor.model;

/**
 * @ClassName: Hosts
 * 
 */
public class HostDO {

	private Long hostid;//  ( 主键 )
	private Long proxyHostid;//  
	private String host;//  
	private Integer status;//  
	private Integer available;//  
	private Integer jmxAvailable;//  
	private String name;//  
	private Integer flags;//  
	
	/**获取  ( 主键 )  */
	public Long getHostid(){
		return hostid;
	}
	
	/**设定  ( 主键 )  */
	public void setHostid(Long hostid){
		this.hostid = hostid;
	}
	
	/**获取    */
	public Long getProxyHostid(){
		return proxyHostid;
	}
	
	/**设定    */
	public void setProxyHostid(Long proxyHostid){
		this.proxyHostid = proxyHostid;
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
	public Integer getStatus(){
		return status;
	}
	
	/**设定    */
	public void setStatus(Integer status){
		this.status = status;
	}
	
	/**获取    */
	public Integer getAvailable(){
		return available;
	}
	
	/**设定    */
	public void setAvailable(Integer available){
		this.available = available;
	}
	
	/**获取    */
	public Integer getJmxAvailable(){
		return jmxAvailable;
	}
	
	/**设定    */
	public void setJmxAvailable(Integer jmxAvailable){
		this.jmxAvailable = jmxAvailable;
	}
	
	/**获取    */
	public String getName(){
		return name;
	}
	
	/**设定    */
	public void setName(String name){
		this.name = name;
	}
	
	/**获取    */
	public Integer getFlags(){
		return flags;
	}
	
	/**设定    */
	public void setFlags(Integer flags){
		this.flags = flags;
	}
	
}