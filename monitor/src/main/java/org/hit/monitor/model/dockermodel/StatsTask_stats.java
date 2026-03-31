package org.hit.monitor.model.dockermodel;

public class StatsTask_stats {
	private String nr_sleeping;
	private String nr_running;
	private String nr_stopped;
	private String nr_uninterruptible;
	private String nr_io_wait;
	public String getNr_sleeping() {
		return nr_sleeping;
	}
	public void setNr_sleeping(String nr_sleeping) {
		this.nr_sleeping = nr_sleeping;
	}
	public String getNr_running() {
		return nr_running;
	}
	public void setNr_running(String nr_running) {
		this.nr_running = nr_running;
	}
	public String getNr_stopped() {
		return nr_stopped;
	}
	public void setNr_stopped(String nr_stopped) {
		this.nr_stopped = nr_stopped;
	}
	public String getNr_uninterruptible() {
		return nr_uninterruptible;
	}
	public void setNr_uninterruptible(String nr_uninterruptible) {
		this.nr_uninterruptible = nr_uninterruptible;
	}
	public String getNr_io_wait() {
		return nr_io_wait;
	}
	public void setNr_io_wait(String nr_io_wait) {
		this.nr_io_wait = nr_io_wait;
	}
	@Override
	public String toString() {
		return "StatsTask_stats [nr_sleeping=" + nr_sleeping + ", nr_running=" + nr_running + ", nr_stopped="
				+ nr_stopped + ", nr_uninterruptible=" + nr_uninterruptible + ", nr_io_wait=" + nr_io_wait + "]";
	}
	
}
