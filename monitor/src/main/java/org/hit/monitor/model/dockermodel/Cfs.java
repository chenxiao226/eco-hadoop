package org.hit.monitor.model.dockermodel;

public class Cfs {
	private long periods;
	private long throttled_periods;
	private long throttler_time;
	public long getPeriods() {
		return periods;
	}
	public void setPeriods(long periods) {
		this.periods = periods;
	}
	public long getThrottled_periods() {
		return throttled_periods;
	}
	public void setThrottled_periods(long throttled_periods) {
		this.throttled_periods = throttled_periods;
	}
	public long getThrottler_time() {
		return throttler_time;
	}
	public void setThrottler_time(long throttler_time) {
		this.throttler_time = throttler_time;
	}
	
	@Override
	public String toString() {
		return "Cfs [periods=" + periods + ", throttled_periods=" + throttled_periods + ", throttler_time="
				+ throttler_time + "]";
	}
	
}
