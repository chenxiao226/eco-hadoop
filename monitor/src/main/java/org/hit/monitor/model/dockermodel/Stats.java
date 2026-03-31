package org.hit.monitor.model.dockermodel;

import java.util.ArrayList;
import java.util.List;

public class Stats {
	private String timestamp;
	private StatsCpu cpu;
	private StatsDiskio diskio;
	private StatsMemory memory;
	private StatsNetwork network;
	private List<StatsFliesystem> filesystem;
	private StatsTask_stats task_stats;
	
	
	public Stats() {
		filesystem = new ArrayList<StatsFliesystem>();
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public StatsCpu getCpu() {
		return cpu;
	}
	public void setCpu(StatsCpu cpu) {
		this.cpu = cpu;
	}
	public StatsDiskio getDiskio() {
		return diskio;
	}
	public void setDiskio(StatsDiskio diskio) {
		this.diskio = diskio;
	}
	public StatsMemory getMemory() {
		return memory;
	}
	public void setMemory(StatsMemory memory) {
		this.memory = memory;
	}
	public StatsNetwork getNetwork() {
		return network;
	}
	public void setNetwork(StatsNetwork network) {
		this.network = network;
	}
	public List<StatsFliesystem> getFilesystem() {
		return filesystem;
	}
	public void setFilesystem(List<StatsFliesystem> filesystem) {
		this.filesystem = filesystem;
	}
	public StatsTask_stats getTask_stats() {
		return task_stats;
	}
	public void setTask_stats(StatsTask_stats task_stats) {
		this.task_stats = task_stats;
	}
	@Override
	public String toString() {
		return "Stats [timestamp=" + timestamp + ", cpu=" + cpu + ", diskio=" + diskio + ", memory=" + memory
				+ ", network=" + network + ", filesystem=" + filesystem + ", task_stats=" + task_stats + "]";
	}
	
	
}
