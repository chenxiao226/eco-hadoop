package org.hit.monitor.model.dockermodel;

public class StatsMemory {
	private String usage;
	private String max_usage;
	private String cache;
	private String rss;
	private String swap;
	private String working_set;
	private String failcnt;
	private Container_data container_data;
	private Hierarchical_data hierarchical_data;
	public String getUsage() {
		return usage;
	}
	public void setUsage(String usage) {
		this.usage = usage;
	}
	public String getMax_usage() {
		return max_usage;
	}
	public void setMax_usage(String max_usage) {
		this.max_usage = max_usage;
	}
	public String getCache() {
		return cache;
	}
	public void setCache(String cache) {
		this.cache = cache;
	}
	public String getRss() {
		return rss;
	}
	public void setRss(String rss) {
		this.rss = rss;
	}
	public String getSwap() {
		return swap;
	}
	public void setSwap(String swap) {
		this.swap = swap;
	}
	public String getWorking_set() {
		return working_set;
	}
	public void setWorking_set(String working_set) {
		this.working_set = working_set;
	}
	public String getFailcnt() {
		return failcnt;
	}
	public void setFailcnt(String failcnt) {
		this.failcnt = failcnt;
	}
	public Container_data getContainer_data() {
		return container_data;
	}
	public void setContainer_data(Container_data container_data) {
		this.container_data = container_data;
	}
	public Hierarchical_data getHierarchical_data() {
		return hierarchical_data;
	}
	public void setHierarchical_data(Hierarchical_data hierarchical_data) {
		this.hierarchical_data = hierarchical_data;
	}
	@Override
	public String toString() {
		return "StatsMemory [usage=" + usage + ", max_usage=" + max_usage + ", cache=" + cache + ", rss=" + rss
				+ ", swap=" + swap + ", working_set=" + working_set + ", failcnt=" + failcnt + ", container_data="
				+ container_data + ", hierarchical_data=" + hierarchical_data + "]";
	}
	
}
