package org.hit.monitor.model.dockermodel;

import java.util.ArrayList;
import java.util.List;

public class Usage {
	private long total;
	private List<String> per_cpu_usage;
	private long user;
	private long system;
	
	
	public Usage() {
		per_cpu_usage = new ArrayList<String>();
	}
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public List<String> getPer_cpu_usage() {
		return per_cpu_usage;
	}
	public void setPer_cpu_usage(List<String> per_cpu_usage) {
		this.per_cpu_usage = per_cpu_usage;
	}
	public long getUser() {
		return user;
	}
	public void setUser(long user) {
		this.user = user;
	}
	public long getSystem() {
		return system;
	}
	public void setSystem(long system) {
		this.system = system;
	}
	@Override
	public String toString() {
		return "Usage [total=" + total + ", per_cpu_usage=" + per_cpu_usage + ", user=" + user + ", system=" + system
				+ "]";
	}
	
	
	
	
}
