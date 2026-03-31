package org.hit.monitor.model;

/**
 * @ClassName: YarnAppHeuristicResultDetails
 * 
 */
public class YarnAppHeuristicResultDetailsDO {

	private Integer yarnAppHeuristicResultId;// The application heuristic result
												// id ( 主键 )
	private String name;// The analysis detail entry name/key ( 主键 )
	private String value;// The analysis detail value corresponding to the name
	private String details;// More information on analysis details. e.g,
							// stacktrace

	/**
	 * 一下是其他业务
	 */
	public static final int NAME_LIMIT = 128;
	public static final int VALUE_LIMIT = 255;
	public static final int DETAILS_LIMIT = 65535;

	public YarnAppHeuristicResultDO YarnAppHeuristicResultDO;

	public YarnAppHeuristicResultDO getYarnAppHeuristicResultDO() {
		return YarnAppHeuristicResultDO;
	}

	public void setYarnAppHeuristicResultDO(YarnAppHeuristicResultDO yarnAppHeuristicResultDO) {
		YarnAppHeuristicResultDO = yarnAppHeuristicResultDO;
	}

	/** 获取 The application heuristic result id ( 主键 )  */
	public Integer getYarnAppHeuristicResultId() {
		return yarnAppHeuristicResultId;
	}

	/** 设定 The application heuristic result id ( 主键 )  */
	public void setYarnAppHeuristicResultId(Integer yarnAppHeuristicResultId) {
		this.yarnAppHeuristicResultId = yarnAppHeuristicResultId;
	}

	/** 获取 The analysis detail entry name/key ( 主键 )  */
	public String getName() {
		return name;
	}

	/** 设定 The analysis detail entry name/key ( 主键 )  */
	public void setName(String name) {
		this.name = name;
	}

	/** 获取 The analysis detail value corresponding to the name   */
	public String getValue() {
		return value;
	}

	/** 设定 The analysis detail value corresponding to the name   */
	public void setValue(String value) {
		this.value = value;
	}

	/** 获取 More information on analysis details. e.g, stacktrace   */
	public String getDetails() {
		return details;
	}

	/** 设定 More information on analysis details. e.g, stacktrace   */
	public void setDetails(String details) {
		this.details = details;
	}

}