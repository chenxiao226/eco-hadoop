package org.hit.monitor.bo;

/**
 * @ClassName: YarnAppResult查询BO
 * @date 2017-5-8
 * 
 */
public class QueryYarnAppResultBO extends BaseQueryBO{

	private static final long serialVersionUID = 7871440048151990900L;
	
	private String id;// The application id, e.g., application_1236543456321_1234567 ( 主键 )
	private String name;// The application name 
	private String username;// The user who started the application 
	private String queueName;// The queue the application was submitted to 
	private Long startTime;// The time in which application started 
	private Long finishTime;// The time in which application finished 
	private String trackingUrl;// The web URL that can be used to track the application 
	private String jobType;// The Job Type e.g, Pig, Hive, Spark, HadoopJava 
	private Integer severity;// Aggregate severity of all the heuristics. Ranges from 0(LOW) to 4(CRITICAL) 
	private Integer score;// The application score which is the sum of heuristic scores 
	private Integer workflowDepth;// The application depth in the scheduled flow. Depth starts from 0 
	private String scheduler;// The scheduler which triggered the application 
	private String jobName;// The name of the job in the flow to which this app belongs 
	private String jobExecId;// A unique reference to a specific execution of the job/action(job in the workflow). This should filter all applications (mapreduce/spark) triggered by the job for a particular execution. 
	private String flowExecId;// A unique reference to a specific flow execution. This should filter all applications fired by a particular flow execution. Note that if the scheduler supports sub-workflows, then this ID should be the super parent flow execution id that triggered the the applications and sub-workflows. 
	private String jobDefId;// A unique reference to the job in the entire flow independent of the execution. This should filter all the applications(mapreduce/spark) triggered by the job for all the historic executions of that job. 
	private String flowDefId;// A unique reference to the entire flow independent of any execution. This should filter all the historic mr jobs belonging to the flow. Note that if your scheduler supports sub-workflows, then this ID should reference the super parent flow that triggered the all the jobs and sub-workflows. 
	private String jobExecUrl;// A url to the job execution on the scheduler 
	private String flowExecUrl;// A url to the flow execution on the scheduler 
	private String jobDefUrl;// A url to the job definition on the scheduler 
	private String flowDefUrl;// A url to the flow definition on the scheduler 
	private Long resourceUsed;// The resources used by the job in MB Seconds 
	private Long resourceWasted;// The resources wasted by the job in MB Seconds 
	private Long totalDelay;// The total delay in starting of mappers and reducers 
	
	/**  获取 The application id, e.g., application_1236543456321_1234567 ( 主键 )  **/
	public String getId(){
		return id;
	}
	
	/**  设定 The application id, e.g., application_1236543456321_1234567 ( 主键 )  **/
	public void setId(String id){
		this.id = id;
	}
	
	/**  获取 The application name   **/
	public String getName(){
		return name;
	}
	
	/**  设定 The application name   **/
	public void setName(String name){
		this.name = name;
	}
	
	/**  获取 The user who started the application   **/
	public String getUsername(){
		return username;
	}
	
	/**  设定 The user who started the application   **/
	public void setUsername(String username){
		this.username = username;
	}
	
	/**  获取 The queue the application was submitted to   **/
	public String getQueueName(){
		return queueName;
	}
	
	/**  设定 The queue the application was submitted to   **/
	public void setQueueName(String queueName){
		this.queueName = queueName;
	}
	
	/**  获取 The time in which application started   **/
	public Long getStartTime(){
		return startTime;
	}
	
	/**  设定 The time in which application started   **/
	public void setStartTime(Long startTime){
		this.startTime = startTime;
	}
	
	/**  获取 The time in which application finished   **/
	public Long getFinishTime(){
		return finishTime;
	}
	
	/**  设定 The time in which application finished   **/
	public void setFinishTime(Long finishTime){
		this.finishTime = finishTime;
	}
	
	/**  获取 The web URL that can be used to track the application   **/
	public String getTrackingUrl(){
		return trackingUrl;
	}
	
	/**  设定 The web URL that can be used to track the application   **/
	public void setTrackingUrl(String trackingUrl){
		this.trackingUrl = trackingUrl;
	}
	
	/**  获取 The Job Type e.g, Pig, Hive, Spark, HadoopJava   **/
	public String getJobType(){
		return jobType;
	}
	
	/**  设定 The Job Type e.g, Pig, Hive, Spark, HadoopJava   **/
	public void setJobType(String jobType){
		this.jobType = jobType;
	}
	
	/**  获取 Aggregate severity of all the heuristics. Ranges from 0(LOW) to 4(CRITICAL)   **/
	public Integer getSeverity(){
		return severity;
	}
	
	/**  设定 Aggregate severity of all the heuristics. Ranges from 0(LOW) to 4(CRITICAL)   **/
	public void setSeverity(Integer severity){
		this.severity = severity;
	}
	
	/**  获取 The application score which is the sum of heuristic scores   **/
	public Integer getScore(){
		return score;
	}
	
	/**  设定 The application score which is the sum of heuristic scores   **/
	public void setScore(Integer score){
		this.score = score;
	}
	
	/**  获取 The application depth in the scheduled flow. Depth starts from 0   **/
	public Integer getWorkflowDepth(){
		return workflowDepth;
	}
	
	/**  设定 The application depth in the scheduled flow. Depth starts from 0   **/
	public void setWorkflowDepth(Integer workflowDepth){
		this.workflowDepth = workflowDepth;
	}
	
	/**  获取 The scheduler which triggered the application   **/
	public String getScheduler(){
		return scheduler;
	}
	
	/**  设定 The scheduler which triggered the application   **/
	public void setScheduler(String scheduler){
		this.scheduler = scheduler;
	}
	
	/**  获取 The name of the job in the flow to which this app belongs   **/
	public String getJobName(){
		return jobName;
	}
	
	/**  设定 The name of the job in the flow to which this app belongs   **/
	public void setJobName(String jobName){
		this.jobName = jobName;
	}
	
	/**  获取 A unique reference to a specific execution of the job/action(job in the workflow). This should filter all applications (mapreduce/spark) triggered by the job for a particular execution.   **/
	public String getJobExecId(){
		return jobExecId;
	}
	
	/**  设定 A unique reference to a specific execution of the job/action(job in the workflow). This should filter all applications (mapreduce/spark) triggered by the job for a particular execution.   **/
	public void setJobExecId(String jobExecId){
		this.jobExecId = jobExecId;
	}
	
	/**  获取 A unique reference to a specific flow execution. This should filter all applications fired by a particular flow execution. Note that if the scheduler supports sub-workflows, then this ID should be the super parent flow execution id that triggered the the applications and sub-workflows.   **/
	public String getFlowExecId(){
		return flowExecId;
	}
	
	/**  设定 A unique reference to a specific flow execution. This should filter all applications fired by a particular flow execution. Note that if the scheduler supports sub-workflows, then this ID should be the super parent flow execution id that triggered the the applications and sub-workflows.   **/
	public void setFlowExecId(String flowExecId){
		this.flowExecId = flowExecId;
	}
	
	/**  获取 A unique reference to the job in the entire flow independent of the execution. This should filter all the applications(mapreduce/spark) triggered by the job for all the historic executions of that job.   **/
	public String getJobDefId(){
		return jobDefId;
	}
	
	/**  设定 A unique reference to the job in the entire flow independent of the execution. This should filter all the applications(mapreduce/spark) triggered by the job for all the historic executions of that job.   **/
	public void setJobDefId(String jobDefId){
		this.jobDefId = jobDefId;
	}
	
	/**  获取 A unique reference to the entire flow independent of any execution. This should filter all the historic mr jobs belonging to the flow. Note that if your scheduler supports sub-workflows, then this ID should reference the super parent flow that triggered the all the jobs and sub-workflows.   **/
	public String getFlowDefId(){
		return flowDefId;
	}
	
	/**  设定 A unique reference to the entire flow independent of any execution. This should filter all the historic mr jobs belonging to the flow. Note that if your scheduler supports sub-workflows, then this ID should reference the super parent flow that triggered the all the jobs and sub-workflows.   **/
	public void setFlowDefId(String flowDefId){
		this.flowDefId = flowDefId;
	}
	
	/**  获取 A url to the job execution on the scheduler   **/
	public String getJobExecUrl(){
		return jobExecUrl;
	}
	
	/**  设定 A url to the job execution on the scheduler   **/
	public void setJobExecUrl(String jobExecUrl){
		this.jobExecUrl = jobExecUrl;
	}
	
	/**  获取 A url to the flow execution on the scheduler   **/
	public String getFlowExecUrl(){
		return flowExecUrl;
	}
	
	/**  设定 A url to the flow execution on the scheduler   **/
	public void setFlowExecUrl(String flowExecUrl){
		this.flowExecUrl = flowExecUrl;
	}
	
	/**  获取 A url to the job definition on the scheduler   **/
	public String getJobDefUrl(){
		return jobDefUrl;
	}
	
	/**  设定 A url to the job definition on the scheduler   **/
	public void setJobDefUrl(String jobDefUrl){
		this.jobDefUrl = jobDefUrl;
	}
	
	/**  获取 A url to the flow definition on the scheduler   **/
	public String getFlowDefUrl(){
		return flowDefUrl;
	}
	
	/**  设定 A url to the flow definition on the scheduler   **/
	public void setFlowDefUrl(String flowDefUrl){
		this.flowDefUrl = flowDefUrl;
	}
	
	/**  获取 The resources used by the job in MB Seconds   **/
	public Long getResourceUsed(){
		return resourceUsed;
	}
	
	/**  设定 The resources used by the job in MB Seconds   **/
	public void setResourceUsed(Long resourceUsed){
		this.resourceUsed = resourceUsed;
	}
	
	/**  获取 The resources wasted by the job in MB Seconds   **/
	public Long getResourceWasted(){
		return resourceWasted;
	}
	
	/**  设定 The resources wasted by the job in MB Seconds   **/
	public void setResourceWasted(Long resourceWasted){
		this.resourceWasted = resourceWasted;
	}
	
	/**  获取 The total delay in starting of mappers and reducers   **/
	public Long getTotalDelay(){
		return totalDelay;
	}
	
	/**  设定 The total delay in starting of mappers and reducers   **/
	public void setTotalDelay(Long totalDelay){
		this.totalDelay = totalDelay;
	}
	
}