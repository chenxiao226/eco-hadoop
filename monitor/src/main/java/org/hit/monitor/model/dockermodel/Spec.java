package org.hit.monitor.model.dockermodel;

public class Spec {
	private String creation_time;
	private Boolean  has_cpu;
	private SpecCpu  cpu;
	private Boolean  has_memory;
	private SpecMemory memory;
	private Boolean has_network;
	private Boolean has_filesystem;
	private Boolean has_diskio;
	private Boolean has_custom_metrics;
	private String image;
	public String getCreation_time() {
		return creation_time;
	}
	public void setCreation_time(String creation_time) {
		this.creation_time = creation_time;
	}
	public Boolean getHas_cpu() {
		return has_cpu;
	}
	public void setHas_cpu(Boolean has_cpu) {
		this.has_cpu = has_cpu;
	}
	public SpecCpu getCpu() {
		return cpu;
	}
	public void setCpu(SpecCpu cpu) {
		this.cpu = cpu;
	}
	public Boolean getHas_memory() {
		return has_memory;
	}
	public void setHas_memory(Boolean has_memory) {
		this.has_memory = has_memory;
	}
	public SpecMemory getMemory() {
		return memory;
	}
	public void setMemory(SpecMemory memory) {
		this.memory = memory;
	}
	public Boolean getHas_network() {
		return has_network;
	}
	public void setHas_network(Boolean has_network) {
		this.has_network = has_network;
	}
	public Boolean getHas_filesystem() {
		return has_filesystem;
	}
	public void setHas_filesystem(Boolean has_filesystem) {
		this.has_filesystem = has_filesystem;
	}
	public Boolean getHas_diskio() {
		return has_diskio;
	}
	public void setHas_diskio(Boolean has_diskio) {
		this.has_diskio = has_diskio;
	}
	public Boolean getHas_custom_metrics() {
		return has_custom_metrics;
	}
	public void setHas_custom_metrics(Boolean has_custom_metrics) {
		this.has_custom_metrics = has_custom_metrics;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	@Override
	public String toString() {
		return "Spec [creation_time=" + creation_time + ", has_cpu=" + has_cpu + ", cpu=" + cpu + ", has_memory="
				+ has_memory + ", memory=" + memory + ", has_network=" + has_network + ", has_filesystem="
				+ has_filesystem + ", has_diskio=" + has_diskio + ", has_custom_metrics=" + has_custom_metrics
				+ ", image=" + image + "]";
	}
	
}
