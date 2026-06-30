package org.hit.monitor.bo;

/**
 * 封装AppResult查询的参数
 * 
 *
 */

public class QueryAppResultBO extends BaseQueryBO {

	private static final long serialVersionUID = -3895303601396402305L;

	private String name;// job的名字
	private String jobType;// job类型
	private Long start; // 起始时间
	private Long end; // 结束时间

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public Long getStart() {
		return start;
	}

	public void setStart(Long start) {
		this.start = start;
	}

	public Long getEnd() {
		return end;
	}

	public void setEnd(Long end) {
		this.end = end;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
