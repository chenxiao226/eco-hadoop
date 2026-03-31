package org.hit.monitor.bo;

/**
 * @ClassName: YarnAppHeuristicResultDetails查询BO
 * @date 2017-5-23
 * 
 */
public class QueryYarnAppHeuristicResultDetailsBO extends BaseQueryBO{

	private static final long serialVersionUID = -4060007083648963283L;
	
	private Integer yarnAppHeuristicResultId;// The application heuristic result id ( 主键 )
	private String name;// The analysis detail entry name/key ( 主键 )
	private String value;// The analysis detail value corresponding to the name 
	private String details;// More information on analysis details. e.g, stacktrace 
	
	/**  获取 The application heuristic result id ( 主键 )  **/
	public Integer getYarnAppHeuristicResultId(){
		return yarnAppHeuristicResultId;
	}
	
	/**  设定 The application heuristic result id ( 主键 )  **/
	public void setYarnAppHeuristicResultId(Integer yarnAppHeuristicResultId){
		this.yarnAppHeuristicResultId = yarnAppHeuristicResultId;
	}
	
	/**  获取 The analysis detail entry name/key ( 主键 )  **/
	public String getName(){
		return name;
	}
	
	/**  设定 The analysis detail entry name/key ( 主键 )  **/
	public void setName(String name){
		this.name = name;
	}
	
	/**  获取 The analysis detail value corresponding to the name   **/
	public String getValue(){
		return value;
	}
	
	/**  设定 The analysis detail value corresponding to the name   **/
	public void setValue(String value){
		this.value = value;
	}
	
	/**  获取 More information on analysis details. e.g, stacktrace   **/
	public String getDetails(){
		return details;
	}
	
	/**  设定 More information on analysis details. e.g, stacktrace   **/
	public void setDetails(String details){
		this.details = details;
	}
	
}