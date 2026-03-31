package org.hit.monitor.model;

import java.util.List;
/**
 * @ClassName: YarnAppHeuristicResult
 * 
 */
public class YarnAppHeuristicResultDO {

	private Integer id;// The application heuristic result id ( 主键 )
	private String yarnAppResultId;// The application id
	private String heuristicClass;// Name of the JVM class that implements this
									// heuristic
	private String heuristicName;// The heuristic name
	private Integer severity;// The heuristic severity ranging from 0(LOW) to
								// 4(CRITICAL)
	private Integer score;// The heuristic score for the application. score =
							// severity * number_of_tasks(map/reduce) where
							// severity not in [0,1], otherwise score = 0

	/**
	 * 以下是业务逻辑字段
	 */
	private List<YarnAppHeuristicResultDetailsDO> yarnAppHeuristicResultDetailsDOList;
	
	/**
	 * 长度信息
	 * 
	 */
	public static final int HEURISTIC_NAME_LIMIT = 128;
	public static final int HEURISTIC_CLASS_LIMIT = 255;

	public List<YarnAppHeuristicResultDetailsDO> getYarnAppHeuristicResultDetailsDOList() {
		return yarnAppHeuristicResultDetailsDOList;
	}

	public void setYarnAppHeuristicResultDetailsDOList(
			List<YarnAppHeuristicResultDetailsDO> yarnAppHeuristicResultDetailsDOList) {
		this.yarnAppHeuristicResultDetailsDOList = yarnAppHeuristicResultDetailsDOList;
	}

	/** 获取 The application heuristic result id ( 主键 )  */
	public Integer getId() {
		return id;
	}

	/** 设定 The application heuristic result id ( 主键 )  */
	public void setId(Integer id) {
		this.id = id;
	}

	/** 获取 The application id   */
	public String getYarnAppResultId() {
		return yarnAppResultId;
	}

	/** 设定 The application id   */
	public void setYarnAppResultId(String yarnAppResultId) {
		this.yarnAppResultId = yarnAppResultId;
	}

	/** 获取 Name of the JVM class that implements this heuristic   */
	public String getHeuristicClass() {
		return heuristicClass;
	}

	/** 设定 Name of the JVM class that implements this heuristic   */
	public void setHeuristicClass(String heuristicClass) {
		this.heuristicClass = heuristicClass;
	}

	/** 获取 The heuristic name   */
	public String getHeuristicName() {
		return heuristicName;
	}

	/** 设定 The heuristic name   */
	public void setHeuristicName(String heuristicName) {
		this.heuristicName = heuristicName;
	}

	/** 获取 The heuristic severity ranging from 0(LOW) to 4(CRITICAL)   */
	public Integer getSeverity() {
		return severity;
	}

	/** 设定 The heuristic severity ranging from 0(LOW) to 4(CRITICAL)   */
	public void setSeverity(Integer severity) {
		this.severity = severity;
	}

	/**
	 * 获取 The heuristic score for the application. score = severity *
	 * number_of_tasks(map/reduce) where severity not in [0,1], otherwise score
	 * = 0  
	 */
	public Integer getScore() {
		return score;
	}

	/**
	 * 设定 The heuristic score for the application. score = severity *
	 * number_of_tasks(map/reduce) where severity not in [0,1], otherwise score
	 * = 0  
	 */
	public void setScore(Integer score) {
		this.score = score;
	}

}