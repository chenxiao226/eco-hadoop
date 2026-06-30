package org.hit.monitor.model.dockermodel;

public class StatsNetwork {
	private String name;
	private String rx_bytes;
	private String rx_packets;
	private String rx_errors;
	private String rx_dropped;
	private String tx_bytes;
	private String tx_packets;
	private String tx_errors;
	private String tx_dropped;
	private Tcp tcp;
	private Tcp tcp6;
	private Udp udp;
	private Udp udp6;
	
	public Tcp getTcp() {
		return tcp;
	}
	public void setTcp(Tcp tcp) {
		this.tcp = tcp;
	}
	public Tcp getTcp6() {
		return tcp6;
	}
	public void setTcp6(Tcp tcp6) {
		this.tcp6 = tcp6;
	}
	public Udp getUdp() {
		return udp;
	}
	public void setUdp(Udp udp) {
		this.udp = udp;
	}
	public Udp getUdp6() {
		return udp6;
	}
	public void setUdp6(Udp udp6) {
		this.udp6 = udp6;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getRx_bytes() {
		return rx_bytes;
	}
	public void setRx_bytes(String rx_bytes) {
		this.rx_bytes = rx_bytes;
	}
	public String getRx_packets() {
		return rx_packets;
	}
	public void setRx_packets(String rx_packets) {
		this.rx_packets = rx_packets;
	}
	public String getRx_errors() {
		return rx_errors;
	}
	public void setRx_errors(String rx_errors) {
		this.rx_errors = rx_errors;
	}
	public String getRx_dropped() {
		return rx_dropped;
	}
	public void setRx_dropped(String rx_dropped) {
		this.rx_dropped = rx_dropped;
	}
	public String getTx_bytes() {
		return tx_bytes;
	}
	public void setTx_bytes(String tx_bytes) {
		this.tx_bytes = tx_bytes;
	}
	public String getTx_packets() {
		return tx_packets;
	}
	public void setTx_packets(String tx_packets) {
		this.tx_packets = tx_packets;
	}
	public String getTx_errors() {
		return tx_errors;
	}
	public void setTx_errors(String tx_errors) {
		this.tx_errors = tx_errors;
	}
	public String getTx_dropped() {
		return tx_dropped;
	}
	public void setTx_dropped(String tx_dropped) {
		this.tx_dropped = tx_dropped;
	}
	@Override
	public String toString() {
		return "StatsNetwork [name=" + name + ", rx_bytes=" + rx_bytes + ", rx_packets=" + rx_packets + ", rx_errors="
				+ rx_errors + ", rx_dropped=" + rx_dropped + ", tx_bytes=" + tx_bytes + ", tx_packets=" + tx_packets
				+ ", tx_errors=" + tx_errors + ", tx_dropped=" + tx_dropped + ", tcp=" + tcp + ", tcp6=" + tcp6
				+ ", udp=" + udp + ", udp6=" + udp6 + "]";
	}
	
}
