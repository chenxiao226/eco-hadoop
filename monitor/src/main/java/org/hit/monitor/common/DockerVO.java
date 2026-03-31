package org.hit.monitor.common;

public class DockerVO {
	private long time;
	private long value;
	
	
	public long getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	
}
