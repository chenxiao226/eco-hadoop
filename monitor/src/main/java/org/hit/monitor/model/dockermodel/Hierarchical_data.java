package org.hit.monitor.model.dockermodel;

public class Hierarchical_data {
	private int pgfault;
	private int pgmajfault;
	public int getPgfault() {
		return pgfault;
	}
	public void setPgfault(int pgfault) {
		this.pgfault = pgfault;
	}
	public int getPgmajfault() {
		return pgmajfault;
	}
	public void setPgmajfault(int pgmajfault) {
		this.pgmajfault = pgmajfault;
	}
	@Override
	public String toString() {
		return "Hierarchical_data [pgfault=" + pgfault + ", pgmajfault=" + pgmajfault + "]";
	}
	
	
}
