package org.hit.monitor.model;

/**
 * @ClassName: HistoryUint
 * 
 */
public class HistoryUintDO {

	private Long itemid;//  
	private Integer clock;//  
	private Long value;//  
	private Integer ns;//  
	
	/**获取    */
	public Long getItemid(){
		return itemid;
	}
	
	/**设定    */
	public void setItemid(Long itemid){
		this.itemid = itemid;
	}
	
	/**获取    */
	public Integer getClock(){
		return clock;
	}
	
	/**设定    */
	public void setClock(Integer clock){
		this.clock = clock;
	}
	
	/**获取    */
	public Long getValue(){
		return value;
	}
	
	/**设定    */
	public void setValue(Long value){
		this.value = value;
	}
	
	/**获取    */
	public Integer getNs(){
		return ns;
	}
	
	/**设定    */
	public void setNs(Integer ns){
		this.ns = ns;
	}
	
}