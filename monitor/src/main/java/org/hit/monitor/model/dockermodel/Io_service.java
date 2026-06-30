package org.hit.monitor.model.dockermodel;

public class Io_service {
	private String device;
	private int major;
	private int minor;
	private DiskioStats stats;
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	public int getMajor() {
		return major;
	}
	public void setMajor(int major) {
		this.major = major;
	}
	public int getMinor() {
		return minor;
	}
	public void setMinor(int minor) {
		this.minor = minor;
	}
	public DiskioStats getStats() {
		return stats;
	}
	public void setStats(DiskioStats stats) {
		this.stats = stats;
	}
	@Override
	public String toString() {
		return "Io_service [device=" + device + ", major=" + major + ", minor=" + minor + ", stats=" + stats + "]";
	}
	
	
}
