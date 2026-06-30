package org.hit.monitor.bo;

import java.util.List;

/**
 * 封装指标查询的参数
 *
 */
public class QueryMetricsBO extends BaseQueryBO {
	
	private static final long serialVersionUID = 3411174176395961214L;
	
	private Long start;                    // 起始时间，从1970年1月1日到现在经过的秒数
	private Long end;                    // 结束时间，从1970年1月1日到现在经过的秒数
	private String metricsName;            // 指标名称，旧版本实现中同时也是所在的表名，由Service层在查询时写入
	private Integer limit;                // 最多查出的数量
	private boolean desc = false;        // 是否按照时间降序排列，默认是升序
	
	//以下为适应Zabiix所添加的业务字段
	private Long monitorItemId;
	private List<Long> monitorItemIds;
	private Long hostId;                // 表明是否是一个单节点查询，为空表明是集合数据聚合查询
	private String table;
	
	/** 起始时间，从1970年1月1日到现在经过的秒数 */
	public Long getStart() {
		return start;
	}
	
	/** 起始时间，从1970年1月1日到现在经过的秒数 */
	public void setStart(Long start) {
		this.start = start;
	}
	
	/** 结束时间，从1970年1月1日到现在经过的秒数 */
	public Long getEnd() {
		return end;
	}
	
	/** 结束时间，从1970年1月1日到现在经过的秒数 */
	public void setEnd(Long end) {
		this.end = end;
	}
	
	/** 指标名称 */
	public String getMetricsName() {
		return metricsName;
	}
	
	/** 指标名称 */
	public void setMetricsName(String metricsName) {
		this.metricsName = metricsName;
	}
	
	/** 最多查出的数量 */
	public Integer getLimit() {
		return limit;
	}
	
	/** 最多查出的数量 */
	public void setLimit(Integer limit) {
		this.limit = limit;
	}
	
	/** 是否按照时间降序排列，默认是升序 */
	public boolean isDesc() {
		return desc;
	}
	
	/** 是否按照时间降序排列，默认是升序 */
	public void setDesc(boolean desc) {
		this.desc = desc;
	}
	
	public List<Long> getMonitorItemIds() {
		return monitorItemIds;
	}
	
	public void setMonitorItemIds(List<Long> monitorItemIds) {
		this.monitorItemIds = monitorItemIds;
	}
	
	public Long getHostId() {
		return hostId;
	}
	
	public void setHostId(Long hostId) {
		this.hostId = hostId;
	}
	
	public Long getMonitorItemId() {
		return monitorItemId;
	}
	
	public void setMonitorItemId(Long monitorItemId) {
		this.monitorItemId = monitorItemId;
	}
	
	public String getTable() {
		return table;
	}
	
	public void setTable(String table) {
		this.table = table;
	}
}
