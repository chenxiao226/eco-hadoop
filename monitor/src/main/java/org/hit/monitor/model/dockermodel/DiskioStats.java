package org.hit.monitor.model.dockermodel;

public class DiskioStats {
	private String Async;
	private String Read;
	private String Sync;
	private String Total;
	private String Write;
	public String getAsync() {
		return Async;
	}
	public void setAsync(String async) {
		Async = async;
	}
	public String getRead() {
		return Read;
	}
	public void setRead(String read) {
		Read = read;
	}
	public String getSync() {
		return Sync;
	}
	public void setSync(String sync) {
		Sync = sync;
	}
	public String getTotal() {
		return Total;
	}
	public void setTotal(String total) {
		Total = total;
	}
	public String getWrite() {
		return Write;
	}
	public void setWrite(String write) {
		Write = write;
	}
	@Override
	public String toString() {
		return "DiskioStats [Async=" + Async + ", Read=" + Read + ", Sync=" + Sync + ", Total=" + Total + ", Write="
				+ Write + "]";
	}
	

}
