package org.hit.monitor.model.dockermodel;

public class Udp {
	private String Listen;
	private String Dropped;	
	private String RxQueued;
	private String TxQueued;
	public String getListen() {
		return Listen;
	}
	public void setListen(String listen) {
		Listen = listen;
	}
	public String getDropped() {
		return Dropped;
	}
	public void setDropped(String dropped) {
		Dropped = dropped;
	}
	public String getRxQueued() {
		return RxQueued;
	}
	public void setRxQueued(String rxQueued) {
		RxQueued = rxQueued;
	}
	public String getTxQueued() {
		return TxQueued;
	}
	public void setTxQueued(String txQueued) {
		TxQueued = txQueued;
	}
	@Override
	public String toString() {
		return "Udp [Listen=" + Listen + ", Dropped=" + Dropped + ", RxQueued=" + RxQueued + ", TxQueued=" + TxQueued
				+ "]";
	}
	
}
