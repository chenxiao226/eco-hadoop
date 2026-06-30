package org.hit.monitor.model;

/**
 * @ClassName: MonitorItems
 * @date 2017-7-20
 * 
 */
public class MonitorItemsDO {

	private Long itemid;//  ( 主键 )
	private Integer type;//  
	private Long hostid;//  
	private String name;//  
	private String key;//  
	private Integer delay;//  
	private Integer history;//  
	private Integer trends;//  
	private Integer status;//  
	private Integer valueType;//  
	private String units;//  
	private Integer multiplier;//  
	private Integer delta;//  
	private String formula;//  
	private Long lastlogsize;//  
	private String logtimefmt;//  
	private Integer dataType;//  
	private Integer mtime;//  
	private Integer flags;//  
	private String description;//  
	private String lifetime;//  
	private Integer state;//  
	
	/**获取  ( 主键 )  */
	public Long getItemid(){
		return itemid;
	}
	
	/**设定  ( 主键 )  */
	public void setItemid(Long itemid){
		this.itemid = itemid;
	}
	
	/**获取    */
	public Integer getType(){
		return type;
	}
	
	/**设定    */
	public void setType(Integer type){
		this.type = type;
	}
	
	/**获取    */
	public Long getHostid(){
		return hostid;
	}
	
	/**设定    */
	public void setHostid(Long hostid){
		this.hostid = hostid;
	}
	
	/**获取    */
	public String getName(){
		return name;
	}
	
	/**设定    */
	public void setName(String name){
		this.name = name;
	}
	
	/**获取    */
	public String getKey(){
		return key;
	}
	
	/**设定    */
	public void setKey(String key){
		this.key = key;
	}
	
	/**获取    */
	public Integer getDelay(){
		return delay;
	}
	
	/**设定    */
	public void setDelay(Integer delay){
		this.delay = delay;
	}
	
	/**获取    */
	public Integer getHistory(){
		return history;
	}
	
	/**设定    */
	public void setHistory(Integer history){
		this.history = history;
	}
	
	/**获取    */
	public Integer getTrends(){
		return trends;
	}
	
	/**设定    */
	public void setTrends(Integer trends){
		this.trends = trends;
	}
	
	/**获取    */
	public Integer getStatus(){
		return status;
	}
	
	/**设定    */
	public void setStatus(Integer status){
		this.status = status;
	}
	
	/**获取    */
	public Integer getValueType(){
		return valueType;
	}
	
	/**设定    */
	public void setValueType(Integer valueType){
		this.valueType = valueType;
	}
	
	/**获取    */
	public String getUnits(){
		return units;
	}
	
	/**设定    */
	public void setUnits(String units){
		this.units = units;
	}
	
	/**获取    */
	public Integer getMultiplier(){
		return multiplier;
	}
	
	/**设定    */
	public void setMultiplier(Integer multiplier){
		this.multiplier = multiplier;
	}
	
	/**获取    */
	public Integer getDelta(){
		return delta;
	}
	
	/**设定    */
	public void setDelta(Integer delta){
		this.delta = delta;
	}
	
	/**获取    */
	public String getFormula(){
		return formula;
	}
	
	/**设定    */
	public void setFormula(String formula){
		this.formula = formula;
	}
	
	/**获取    */
	public Long getLastlogsize(){
		return lastlogsize;
	}
	
	/**设定    */
	public void setLastlogsize(Long lastlogsize){
		this.lastlogsize = lastlogsize;
	}
	
	/**获取    */
	public String getLogtimefmt(){
		return logtimefmt;
	}
	
	/**设定    */
	public void setLogtimefmt(String logtimefmt){
		this.logtimefmt = logtimefmt;
	}
	
	/**获取    */
	public Integer getDataType(){
		return dataType;
	}
	
	/**设定    */
	public void setDataType(Integer dataType){
		this.dataType = dataType;
	}
	
	/**获取    */
	public Integer getMtime(){
		return mtime;
	}
	
	/**设定    */
	public void setMtime(Integer mtime){
		this.mtime = mtime;
	}
	
	/**获取    */
	public Integer getFlags(){
		return flags;
	}
	
	/**设定    */
	public void setFlags(Integer flags){
		this.flags = flags;
	}
	
	/**获取    */
	public String getDescription(){
		return description;
	}
	
	/**设定    */
	public void setDescription(String description){
		this.description = description;
	}
	
	/**获取    */
	public String getLifetime(){
		return lifetime;
	}
	
	/**设定    */
	public void setLifetime(String lifetime){
		this.lifetime = lifetime;
	}
	
	/**获取    */
	public Integer getState(){
		return state;
	}
	
	/**设定    */
	public void setState(Integer state){
		this.state = state;
	}
	
}