package org.hit.monitor.model.dockermodel;

public class StatsFliesystem {
	private String device;
	private String type;
	private String capacity;
	private String usage;
	private String base_usage;
	private String available;
	private String has_inodes;
	private String inodes;
	private String inodes_free;
	private String reads_completed;
	private String reads_merged;
	private String sectors_read;
	private String read_time;
	private String writes_completed;
	private String writes_merged;
	private String sectors_written;
	private String write_time;
	private String io_in_progress;
	private String io_time;
	private String weighted_io_time;
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getCapacity() {
		return capacity;
	}
	public void setCapacity(String capacity) {
		this.capacity = capacity;
	}
	public String getUsage() {
		return usage;
	}
	public void setUsage(String usage) {
		this.usage = usage;
	}
	public String getBase_usage() {
		return base_usage;
	}
	public void setBase_usage(String base_usage) {
		this.base_usage = base_usage;
	}
	public String getAvailable() {
		return available;
	}
	public void setAvailable(String available) {
		this.available = available;
	}
	public String getHas_inodes() {
		return has_inodes;
	}
	public void setHas_inodes(String has_inodes) {
		this.has_inodes = has_inodes;
	}
	public String getInodes() {
		return inodes;
	}
	public void setInodes(String inodes) {
		this.inodes = inodes;
	}
	public String getInodes_free() {
		return inodes_free;
	}
	public void setInodes_free(String inodes_free) {
		this.inodes_free = inodes_free;
	}
	public String getReads_completed() {
		return reads_completed;
	}
	public void setReads_completed(String reads_completed) {
		this.reads_completed = reads_completed;
	}
	public String getReads_merged() {
		return reads_merged;
	}
	public void setReads_merged(String reads_merged) {
		this.reads_merged = reads_merged;
	}
	public String getSectors_read() {
		return sectors_read;
	}
	public void setSectors_read(String sectors_read) {
		this.sectors_read = sectors_read;
	}
	public String getRead_time() {
		return read_time;
	}
	public void setRead_time(String read_time) {
		this.read_time = read_time;
	}
	public String getWrites_completed() {
		return writes_completed;
	}
	public void setWrites_completed(String writes_completed) {
		this.writes_completed = writes_completed;
	}
	public String getWrites_merged() {
		return writes_merged;
	}
	public void setWrites_merged(String writes_merged) {
		this.writes_merged = writes_merged;
	}
	public String getSectors_written() {
		return sectors_written;
	}
	public void setSectors_written(String sectors_written) {
		this.sectors_written = sectors_written;
	}
	public String getWrite_time() {
		return write_time;
	}
	public void setWrite_time(String write_time) {
		this.write_time = write_time;
	}
	public String getIo_in_progress() {
		return io_in_progress;
	}
	public void setIo_in_progress(String io_in_progress) {
		this.io_in_progress = io_in_progress;
	}
	public String getIo_time() {
		return io_time;
	}
	public void setIo_time(String io_time) {
		this.io_time = io_time;
	}
	public String getWeighted_io_time() {
		return weighted_io_time;
	}
	public void setWeighted_io_time(String weighted_io_time) {
		this.weighted_io_time = weighted_io_time;
	}
	@Override
	public String toString() {
		return "StatsFliesystem [device=" + device + ", type=" + type + ", capacity=" + capacity + ", usage=" + usage
				+ ", base_usage=" + base_usage + ", available=" + available + ", has_inodes=" + has_inodes + ", inodes="
				+ inodes + ", inodes_free=" + inodes_free + ", reads_completed=" + reads_completed + ", reads_merged="
				+ reads_merged + ", sectors_read=" + sectors_read + ", read_time=" + read_time + ", writes_completed="
				+ writes_completed + ", writes_merged=" + writes_merged + ", sectors_written=" + sectors_written
				+ ", write_time=" + write_time + ", io_in_progress=" + io_in_progress + ", io_time=" + io_time
				+ ", weighted_io_time=" + weighted_io_time + "]";
	}
	
}
