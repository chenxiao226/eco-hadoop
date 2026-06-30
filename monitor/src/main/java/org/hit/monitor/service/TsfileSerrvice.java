package org.hit.monitor.service;

public interface TsfileSerrvice {
	public String fetchTotalPoints() throws Exception;

	public String fetchREQSuccess() throws Exception;

	public String fetchREQFail() throws Exception;

	public String fetchTotalPointsFAIL() throws Exception;
}
