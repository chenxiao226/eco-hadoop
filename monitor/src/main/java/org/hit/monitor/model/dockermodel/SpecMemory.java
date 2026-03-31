package org.hit.monitor.model.dockermodel;

public class SpecMemory {
	private String limit;
	private String reservation;
	private String swap_limit;
	public String getLimit() {
		return limit;
	}
	public void setLimit(String limit) {
		this.limit = limit;
	}
	public String getReservation() {
		return reservation;
	}
	public void setReservation(String reservation) {
		this.reservation = reservation;
	}
	public String getSwap_limit() {
		return swap_limit;
	}
	public void setSwap_limit(String swap_limit) {
		this.swap_limit = swap_limit;
	}
	@Override
	public String toString() {
		return "SpecMemory [limit=" + limit + ", reservation=" + reservation + ", swap_limit=" + swap_limit + "]";
	}
	

}
