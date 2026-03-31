package org.hit.monitor.common;

public class TimeSeriesDTO {

	protected String time; // 普通时间
	protected Double value;
	protected Long processTime; // unix时间戳
	
	public TimeSeriesDTO(String time, Double value, Long processTime){
		this.time = time;
		this.value = value;
		this.processTime = processTime;
	}
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public Double getValue() {
		return value;
	}
	public void setValue(Double value) {
		this.value = value;
	}

	public Long getProcessTime() {
		return processTime;
	}

	public void setProcessTime(Long processTime) {
		this.processTime = processTime;
	}
	
	
}
