package org.hit.monitor.model;

import org.apache.htrace.fasterxml.jackson.annotation.JsonTypeInfo.Id;

public class RDBProcess {
	private Long Id;
	private String User;
	private String Host;
	private	String db;
	private	String Command;
	private	String Time;
	private	String State;
	private	String Info;
	public Long getId() {
		return Id;
	}
	public void setId(Long id) {
		Id = id;
	}
	public String getUser() {
		return User;
	}
	public void setUser(String user) {
		User = user;
	}
	public String getHost() {
		return Host;
	}
	public void setHost(String host) {
		Host = host;
	}
	public String getDb() {
		return db;
	}
	public void setDb(String db) {
		this.db = db;
	}
	public String getCommand() {
		return Command;
	}
	public void setCommand(String command) {
		Command = command;
	}
	public String getTime() {
		return Time;
	}
	public void setTime(String time) {
		Time = time;
	}
	public String getState() {
		return State;
	}
	public void setState(String state) {
		State = state;
	}
	public String getInfo() {
		return Info;
	}
	public void setInfo(String info) {
		Info = info;
	}
	@Override
	public String toString() {
		return "RDBProcess [Id=" + Id + ", User=" + User + ", Host=" + Host + ", db=" + db + ", Command=" + Command
				+ ", Time=" + Time + ", State=" + State + ", Info=" + Info + "]";
	}
	
}
