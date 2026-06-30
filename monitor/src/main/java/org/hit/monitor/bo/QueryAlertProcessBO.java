package org.hit.monitor.bo;

/**
 * @ClassName: AlertProcess查询BO
 * @date 2017-7-27
 */
public class QueryAlertProcessBO extends BaseQueryBO {
	private Long id;
	private String machine;//  
	//设定id为主键
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	private String process;//  
	private Integer status;//  

	public String getMachine() {
		return machine;
	}
	public void setMachine(String machine) {
		this.machine = machine;
	}
	public String getProcess() {
		return process;
	}
	public void setProcess(String process) {
		this.process = process;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	

}