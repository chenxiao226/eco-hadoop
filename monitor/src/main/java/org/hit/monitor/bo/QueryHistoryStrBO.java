package org.hit.monitor.bo;

/**
 * @ClassName: HistoryStr查询BO
 * @date 2017-7-20
 * 
 */
public class QueryHistoryStrBO extends BaseQueryBO{

	private Long itemid;//  
	private Integer clock;//  
	private String value;//  
	private Integer ns;//  
	
	/**  获取    **/
	public Long getItemid(){
		return itemid;
	}
	
	/**  设定    **/
	public void setItemid(Long itemid){
		this.itemid = itemid;
	}
	
	/**  获取    **/
	public Integer getClock(){
		return clock;
	}
	
	/**  设定    **/
	public void setClock(Integer clock){
		this.clock = clock;
	}
	
	/**  获取    **/
	public String getValue(){
		return value;
	}
	
	/**  设定    **/
	public void setValue(String value){
		this.value = value;
	}
	
	/**  获取    **/
	public Integer getNs(){
		return ns;
	}
	
	/**  设定    **/
	public void setNs(Integer ns){
		this.ns = ns;
	}
	
}