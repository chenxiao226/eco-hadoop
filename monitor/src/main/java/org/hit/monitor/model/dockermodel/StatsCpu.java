package org.hit.monitor.model.dockermodel;



public class StatsCpu {
	private Usage usage;
	private Cfs cfs;
	private long load_average;
	public Usage getUsage() {
		return usage;
	}
	public void setUsage(Usage usage) {
		this.usage = usage;
	}
	public Cfs getCfs() {
		return cfs;
	}
	public void setCfs(Cfs cfs) {
		this.cfs = cfs;
	}
	public long getLoad_average() {
		return load_average;
	}
	public void setLoad_average(long load_average) {
		this.load_average = load_average;
	}
	@Override
	public String toString() {
		return "StatsCpu [usage=" + usage + ", cfs=" + cfs + ", load_average=" + load_average + "]";
	}
	
	
}
