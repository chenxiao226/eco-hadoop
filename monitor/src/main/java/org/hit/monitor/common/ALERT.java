package org.hit.monitor.common;


public interface ALERT {
	interface TRIGGER{
		int OK = 1;
		int PROBLEM = 0;
	}
	interface PROCESS{
		int STOP = 0;
		int START = 1;
	}
}
