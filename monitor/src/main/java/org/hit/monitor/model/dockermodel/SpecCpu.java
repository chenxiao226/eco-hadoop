package org.hit.monitor.model.dockermodel;

public class SpecCpu {
	private Integer limit;
	private Integer max_limit;
	private String mask;
	private Integer period;
	public Integer getLimit() {
		return limit;
	}
	public void setLimit(Integer limit) {
		this.limit = limit;
	}
	public Integer getMax_limit() {
		return max_limit;
	}
	public void setMax_limit(Integer max_limit) {
		this.max_limit = max_limit;
	}
	public String getMask() {
		return mask;
	}
	public void setMask(String mask) {
		this.mask = mask;
	}
	public Integer getPeriod() {
		return period;
	}
	public void setPeriod(Integer period) {
		this.period = period;
	}
	@Override
	public String toString() {
		return "SpecCpu [limit=" + limit + ", max_limit=" + max_limit + ", mask=" + mask + ", period=" + period + "]";
	}
	
}
