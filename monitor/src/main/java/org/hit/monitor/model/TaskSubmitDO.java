package org.hit.monitor.model;

/**
 * @ClassName: TaskSubmit
 * @date 2025-03-14
 * 
 */
public class TaskSubmitDO {

	private Long id;//  ( 主键 )
	private String host;//
	private String triggerDes;



	private String taskDes;
	private String triggerName;
	private String severity;//
	private Integer status;//
	private String taskParametersName;
	private String taskParametersValue;
	private Integer excutionFrequency;
	private Long startTime;//

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
	public String getTaskDes() {
		return taskDes;
	}

	public void setTaskDes(String taskDes) {
		this.taskDes = taskDes;
	}

	public String getTriggerDes() {
		return triggerDes;
	}

	public void setTriggerDes(String triggerDes) {
		this.triggerDes = triggerDes;
	}

	public String getTriggerName() {
		return triggerName;
	}

	public void setTriggerName(String triggerName) {
		this.triggerName = triggerName;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getTaskParametersName() {
		return taskParametersName;
	}

	public void setTaskParametersName(String taskParametersName) {
		this.taskParametersName = taskParametersName;
	}

	public String getTaskParametersValue() {
		return taskParametersValue;
	}

	public void setTaskParametersValue(String taskParametersValue) {
		this.taskParametersValue = taskParametersValue;
	}

	public Integer getExcutionFrequency() {
		return excutionFrequency;
	}

	public void setExcutionFrequency(Integer excutionFrequency) {
		this.excutionFrequency = excutionFrequency;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	@Override
	public String toString() {
		return "TaskSubmitDO{" +
				"id=" + id +
				", host='" + host + '\'' +
				", triggerDes='" + triggerDes + '\'' +
				", taskDes='" + taskDes + '\'' +
				", triggerName='" + triggerName + '\'' +
				", severity='" + severity + '\'' +
				", status=" + status +
				", taskParametersName='" + taskParametersName + '\'' +
				", taskParametersValue='" + taskParametersValue + '\'' +
				", excutionFrequency=" + excutionFrequency +
				", startTime=" + startTime +
				'}';
	}
}