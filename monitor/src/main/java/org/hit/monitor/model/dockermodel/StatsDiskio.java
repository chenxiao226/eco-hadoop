package org.hit.monitor.model.dockermodel;

import java.util.List;

public class StatsDiskio {
	private List<Io_service> io_service_bytes;
	private List<Io_service> io_serviced;
	public List<Io_service> getIo_service_bytes() {
		return io_service_bytes;
	}
	public void setIo_service_bytes(List<Io_service> io_service_bytes) {
		this.io_service_bytes = io_service_bytes;
	}
	public List<Io_service> getIo_serviced() {
		return io_serviced;
	}
	public void setIo_serviced(List<Io_service> io_serviced) {
		this.io_serviced = io_serviced;
	}
	@Override
	public String toString() {
		return "StatsDiskio [io_service_bytes=" + io_service_bytes + ", io_serviced=" + io_serviced + "]";
	}
	
}
