package org.hit.monitor.model;

/**
 * @ClassName: MetricsDataDO
 */
public class MetricsDataDO {
	
	private Long id;//  ( 主键 )
	private Double sum;//
	private Integer num;//
	private Long processTime;//
	
	private Long itemId; //关联的监控项，采用zabbix方案后引入
	
	/** 获取  ( 主键 )  */
	public Long getId() {
		return id;
	}
	
	/** 设定  ( 主键 )  */
	public void setId(Long id) {
		this.id = id;
	}
	
	/** 获取    */
	public Double getSum() {
		return sum;
	}
	
	/** 设定    */
	public void setSum(Double sum) {
		this.sum = sum;
	}
	
	/** 获取    */
	public Integer getNum() {
		return num;
	}
	
	/** 设定    */
	public void setNum(Integer num) {
		this.num = num;
	}
	
	/** 获取    */
	public Long getProcessTime() {
		return processTime;
	}
	
	/** 设定    */
	public void setProcessTime(Long processTime) {
		this.processTime = processTime;
	}
	
	public Long getItemId() {
		return itemId;
	}
	
	public void setItemId(Long itemId) {
		this.itemId = itemId;
	}
}