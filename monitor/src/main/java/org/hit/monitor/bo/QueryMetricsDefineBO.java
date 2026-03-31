package org.hit.monitor.bo;

/**
 * @ClassName: MetricsDefine查询BO
 * @date 2017-4-25
 */
public class QueryMetricsDefineBO extends BaseQueryBO {

	private static final long serialVersionUID = -525048406255081854L;

	private Integer metricsId;// 不可为0 ( 主键 )
	private String name;//
	private Integer step;//
	private String dataSourceType;//

	/** 获取 不可为0 ( 主键 )  **/
	public Integer getMetricsId() {
		return metricsId;
	}

	/** 设定 不可为0 ( 主键 )  **/
	public void setMetricsId(Integer metricsId) {
		this.metricsId = metricsId;
	}

	/** 获取   **/
	public String getName() {
		return name;
	}

	/** 设定   **/
	public void setName(String name) {
		this.name = name;
	}

	/** 获取   **/
	public Integer getStep() {
		return step;
	}

	/** 设定   **/
	public void setStep(Integer step) {
		this.step = step;
	}

	/** 获取   **/
	public String getDataSourceType() {
		return dataSourceType;
	}

	/** 设定   **/
	public void setDataSourceType(String dataSourceType) {
		this.dataSourceType = dataSourceType;
	}

}
