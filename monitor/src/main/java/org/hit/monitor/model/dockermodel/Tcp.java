package org.hit.monitor.model.dockermodel;

public class Tcp {
	private String Established;
	private String SynSent;
	private String SynRecv;
	private String FinWait1;
	private String FinWait2;
	private String TimeWait;
	private String Close;
	private String CloseWait;
	private String LastAck;
	private String Listen;
	private String Closing;
	public String getEstablished() {
		return Established;
	}
	public void setEstablished(String established) {
		Established = established;
	}
	public String getSynSent() {
		return SynSent;
	}
	public void setSynSent(String synSent) {
		SynSent = synSent;
	}
	public String getSynRecv() {
		return SynRecv;
	}
	public void setSynRecv(String synRecv) {
		SynRecv = synRecv;
	}
	public String getFinWait1() {
		return FinWait1;
	}
	public void setFinWait1(String finWait1) {
		FinWait1 = finWait1;
	}
	public String getFinWait2() {
		return FinWait2;
	}
	public void setFinWait2(String finWait2) {
		FinWait2 = finWait2;
	}
	public String getTimeWait() {
		return TimeWait;
	}
	public void setTimeWait(String timeWait) {
		TimeWait = timeWait;
	}
	public String getClose() {
		return Close;
	}
	public void setClose(String close) {
		Close = close;
	}
	public String getCloseWait() {
		return CloseWait;
	}
	public void setCloseWait(String closeWait) {
		CloseWait = closeWait;
	}
	public String getLastAck() {
		return LastAck;
	}
	public void setLastAck(String lastAck) {
		LastAck = lastAck;
	}
	public String getListen() {
		return Listen;
	}
	public void setListen(String listen) {
		Listen = listen;
	}
	public String getClosing() {
		return Closing;
	}
	public void setClosing(String closing) {
		Closing = closing;
	}
	@Override
	public String toString() {
		return "Tcp [Established=" + Established + ", SynSent=" + SynSent + ", SynRecv=" + SynRecv + ", FinWait1="
				+ FinWait1 + ", FinWait2=" + FinWait2 + ", TimeWait=" + TimeWait + ", Close=" + Close + ", CloseWait="
				+ CloseWait + ", LastAck=" + LastAck + ", Listen=" + Listen + ", Closing=" + Closing + "]";
	}
	
}
