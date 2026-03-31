package org.hit.monitor.model;

/**
 * @ClassName: HistoryLog
 */
public class HistoryLogDO {
	
	private Long id;//  ( 主键 )
	private Long itemid;//  
	private Integer clock;//  
	private Integer timestamp;//  
	private String source;//  
	private Integer severity;//  
	private String value;//  
	private Integer logeventid;//  
	private Integer ns;//  
	
	/** 获取  ( 主键 )  */
	public Long getId() {
		return id;
	}
	
	/** 设定  ( 主键 )  */
	public void setId(Long id) {
		this.id = id;
	}
	
	/** 获取    */
	public Long getItemid() {
		return itemid;
	}
	
	/** 设定    */
	public void setItemid(Long itemid) {
		this.itemid = itemid;
	}
	
	/** 获取    */
	public Integer getClock() {
		return clock;
	}
	
	/** 设定    */
	public void setClock(Integer clock) {
		this.clock = clock;
	}
	
	/** 获取    */
	public Integer getTimestamp() {
		return timestamp;
	}
	
	/** 设定    */
	public void setTimestamp(Integer timestamp) {
		this.timestamp = timestamp;
	}
	
	/** 获取    */
	public String getSource() {
		return source;
	}
	
	/** 设定    */
	public void setSource(String source) {
		this.source = source;
	}
	
	/** 获取    */
	public Integer getSeverity() {
		return severity;
	}
	
	/** 设定    */
	public void setSeverity(Integer severity) {
		this.severity = severity;
	}
	
	/** 获取    */
	public String getValue() {
		return value;
	}
	
	/** 设定    */
	public void setValue(String value) {
		this.value = value;
	}
	
	/** 获取    */
	public Integer getLogeventid() {
		return logeventid;
	}
	
	/** 设定    */
	public void setLogeventid(Integer logeventid) {
		this.logeventid = logeventid;
	}
	
	/** 获取    */
	public Integer getNs() {
		return ns;
	}
	
	/** 设定    */
	public void setNs(Integer ns) {
		this.ns = ns;
	}
	
}