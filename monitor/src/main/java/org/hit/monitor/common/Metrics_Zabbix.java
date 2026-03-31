package org.hit.monitor.common;

/**
 * 定义了所有系统监控的指标
 */
public interface Metrics_Zabbix {
	
	/**
	 * 返回指标的名称，也代表了指标在数据库中的表名
	 */
	String metricsName();
	
	/**
	 * 返回指标的描述
	 */
	String description();
	
	/**
	 * 返回在zabbix中所属的表
	 */
	String table();
	
	
	/**
	 * DataNode相关指标
	 **/
	enum DataNode implements Metrics_Zabbix {
		
		Write_Block_Op_Avg_Time("m_dfs_datanode_writeblockopavgtime", "", "history"),
		Read_Block_Op_Avg_Time("dfs.datanode.ReadBlockOpAvgTime", "", "history"),
		Blocks_Written("dfs.datanode.BlocksWritten", "", "history"),
		Blocks_Read("dfs.datanode.BlocksRead", "", "history"),
		Bytes_Writen("dfs.datanode.BytesWritten", "", "history"),
		Bytes_Read("dfs.datanode.BytesRead", "", "history");
		
		private String metricsName;
		private String description;
		private String table;
		
		private DataNode(String name, String description, String table) {
			this.metricsName = name;
			this.description = description;
			this.table = table;
		}
		
		@Override
		public String description() {
			return description;
		}
		
		@Override
		public String metricsName() {
			return metricsName;
		}
		
		@Override
		public String table() {
			return table;
		}
	}
	
	
	/**
	 * CPU相关的参指标
	 **/
	enum CPU implements Metrics_Zabbix {
		
		NUM("cpu_num", "cpu数量", "history"),
		SPEED("cpu_speed", "cpu主频", "history"),
		STEAL("system.cpu.util[,steal]", "", "history"),
		IDLE("system.cpu.util[,idle]", "CPU空闲比率", "history"),
		NICE("system.cpu.util[,nice]", "用户进程空间内改变过优先级的进程占用CPU百分比", "history"),
		SYSTEM("system.cpu.util[,system]", "内核空间所占CPU", "history"),
		USER("system.cpu.util[,user]", "用户空间所占CPU", "history"),
		WIO("system.cpu.util[,iowait]", "CPU由于一些显著的I/O而进入空闲状态所占的比例", "history");
		
		private String metricsName;
		private String description;
		private String table;
		
		private CPU(String name, String description, String table) {
			this.metricsName = name;
			this.description = description;
			this.table = table;
		}
		
		@Override
		public String description() {
			return description;
		}
		
		@Override
		public String metricsName() {
			return metricsName;
		}
		
		@Override
		public String table() {
			return table;
		}
	}
	
	/**
	 * 内存相关的参指标
	 **/
	enum MEMORY implements Metrics_Zabbix {
		
		BUFFERS("mem_buffers", "", "history"),
		CACHED("mem_cached", "", "history"),
		FREE("mem_free", "", "history"),
		SHARED("mem_shared", "", "history"),
		TOTAL("mem_total", "", "history"),
		SWAP_FREE("system.swap.size[,free]", "", "history_uint"),
		SWAP_TOTAL("system.swap.size[,total]", "", "history_uint");
		
		private String metricsName;
		private String description;
		private String table;
		
		private MEMORY(String name, String description, String table) {
			this.metricsName = name;
			this.description = description;
			this.table = table;
		}
		
		@Override
		public String description() {
			return description;
		}
		
		@Override
		public String metricsName() {
			return metricsName;
		}
		
		@Override
		public String table() {
			return table;
		}
	}
	
	/**
	 * 负载相关的参指标
	 **/
	enum LOAD implements Metrics_Zabbix {
		
		LOAD_ONE("system.cpu.load[percpu,avg1]", "", "history"),
		LOAD_FIVE("system.cpu.load[percpu,avg5]", "", "history"),
		LOAD_FIFTE("system.cpu.load[percpu,avg15]", "", "history");
		
		private String metricsName;
		private String description;
		private String table;
		
		private LOAD(String name, String description, String table) {
			this.metricsName = name;
			this.description = description;
			this.table = table;
		}
		
		@Override
		public String description() {
			return description;
		}
		
		@Override
		public String metricsName() {
			return metricsName;
		}
		
		@Override
		public String table() {
			return table;
		}
	}
	
	/**
	 * 进程相关的参指标
	 **/
	enum PROCESS implements Metrics_Zabbix {
		
		PROC_RUN("proc.num[,,run]", "", "history_uint"),
		PROC_TOTAL("proc.num[]", "", "history_uint");
		
		private String metricsName;
		private String description;
		private String table;
		
		private PROCESS(String name, String description, String table) {
			this.metricsName = name;
			this.description = description;
			this.table = table;
		}
		
		@Override
		public String description() {
			return description;
		}
		
		@Override
		public String metricsName() {
			return metricsName;
		}
		
		@Override
		public String table() {
			return table;
		}
	}
	
	/**
	 * 网络相关的参指标
	 **/
	enum NETWORK implements Metrics_Zabbix {
		
		BYTES_IN("net.if.in[eth0]", "", "history_uint"),
		BYTES_OUT("net.if.out[eth0]", "", "history_uint");
		
		private String metricsName;
		private String description;
		private String table;
		
		private NETWORK(String name, String description, String table) {
			this.metricsName = name;
			this.description = description;
			this.table = table;
		}
		
		@Override
		public String description() {
			return description;
		}
		
		@Override
		public String metricsName() {
			return metricsName;
		}
		
		@Override
		public String table() {
			return table;
		}
	}
	
	/**
	 * 磁盘相关的参指标
	 **/
	enum DISK implements Metrics_Zabbix {
		
		DISK_FREE("vfs.fs.size[/,free]", "", "history_uint"),
		DISK_TOTAL("vfs.fs.size[/,total]", "", "history_uint");
		
		private String metricsName;
		private String description;
		private String table;
		
		private DISK(String name, String description, String table) {
			this.metricsName = name;
			this.description = description;
			this.table = table;
		}
		
		@Override
		public String description() {
			return description;
		}
		
		@Override
		public String metricsName() {
			return metricsName;
		}
		
		@Override
		public String table() {
			return table;
		}
	}
	
	/** namenode相关的指标 **/
	enum NAMENODE implements Metrics_Zabbix {
		
		GC_TIME("jvm.JvmMetrics.GcTimeMillis", "", "history"),
		GC_COUNT("jvm.JvmMetrics.GcCount", "", "history"),
		MEM_HEAP_USED_M("jvm.JvmMetrics.MemHeapUsedM", "Current non-heap memory used in MB", "history"),
		THREADS_BLOCKED("jvm.JvmMetrics.ThreadsBlocked", "", "history"),
		THREADS_WAITING("jvm.JvmMetrics.ThreadsWaiting", "", "history"),
		TOTAL_FILES("dfs.FSNamesystem.TotalFiles", "", "history"),
		BLOCKS_TOTAL("dfs.FSNamesystem.BlocksTotal", "", "history"),
		CORRUPT_BLOCKS("dfs.FSNamesystem.CorruptBlocks", "", "history"),
		MISSING_BLOCKS("dfs.FSNamesystem.MissingBlocks", "", "history"),
		CAPACITY_TOTAL("dfs.FSNamesystem.CapacityTotal", "Current raw capacity of DataNodes in bytes", "history"),
		CAPACITY_USED("dfs.FSNamesystem.CapacityUsed", "Current used capacity across all DataNodes in bytes", "history"),
		MEM_HEAP_MAX_M("jvm.JvmMetrics.MemHeapMaxM", "", "history");
		
		private String metricsName;
		private String description;
		private String table;
		
		private NAMENODE(String name, String description, String table) {
			this.metricsName = name;
			this.description = description;
			this.table = table;
		}
		
		@Override
		public String description() {
			return description;
		}
		
		@Override
		public String metricsName() {
			return metricsName;
		}
		
		@Override
		public String table() {
			return table;
		}
		
	}
	
	/**
	 * RPC相关的参指标
	 **/
	enum RPC implements Metrics_Zabbix {
		
		RETRY_CACHE_NAMENODE_RETRY_CACHE_CACHE_CLEARED("rpc.RetryCache.NameNodeRetryCache.CacheCleared", "", "history"),
		RETRY_CACHE_NAMENODE_RETRY_CACHE_CACHE_HIT("rpc.RetryCache.NameNodeRetryCache.CacheHit", "", "history"),
		RETRY_CACHE_NAMENODE_RETRY_CACHE_CACHE_UPDATED("rpc.RetryCache.NameNodeRetryCache.CacheUpdated", "", "history"),
		RPC_CALL_QUEUE_LENGTH("rpc.rpc.CallQueueLength", "", "history"),
		RPC_NUM_OPEN_CONNECTIONS("rpc.rpc.NumOpenConnections", "", "history"),
		RPC_RECEIVED_BYTES("rpc.rpc.ReceivedBytes", "", "history"),
		RPC_AUTHENTICATION_FAILURES("rpc.rpc.RpcAuthenticationFailures", "", "history"),
		RPC_AUTHENTICATION_SUCCESSES("rpc.rpc.RpcAuthenticationSuccesses", "", "history"),
		RPC_AUTHORIZATION_FAILURES("rpc.rpc.RpcAuthorizationFailures", "", "history"),
		RPC_AUTHORIZATION_SUCCESSES("rpc.rpc.RpcAuthorizationSuccesses", "", "history"),
		RPC_PROCESSING_TIME_AVG_TIME("rpc.rpc.RpcProcessingTimeAvgTime", "", "history"),
		RPC_PROCESSING_TIME_NUM_OPS("rpc.rpc.RpcProcessingTimeNumOps", "", "history"),
		RPC_QUEUE_TIME_AVG_TIME("rpc.rpc.RpcQueueTimeAvgTime", "", "history"),
		RPC_QUEUE_TIME_NUM_OPS("rpc.rpc.RpcQueueTimeNumOps", "", "history"),
		RPC_SENT_BYTES("rpc.rpc.SentBytes", "", "history");
		
		private String metricsName;
		private String description;
		private String table;
		
		private RPC(String name, String description, String table) {
			this.metricsName = name;
			this.description = description;
			this.table = table;
		}
		
		@Override
		public String description() {
			return description;
		}
		
		@Override
		public String metricsName() {
			return metricsName;
		}
		
		@Override
		public String table() {
			return table;
		}
	}
	
	/**
	 * YARN相关的参指标
	 **/
	enum YARN_QUEUE_METRICS implements Metrics_Zabbix {
		
		AGGREGATE_CONTAINERS_RELEASED("yarn.QueueMetrics.AggregateContainersReleased", "", "history"),
		APPS_RUNNING("yarn.QueueMetrics.AppsRunning", "", "history"),
		ALLOCATED_MB("yarn.QueueMetrics.AllocatedMB", "", "history"),
		ACTIVE_USERS("yarn.QueueMetrics.ActiveUsers", "", "history"),
		APPS_COMPLETED("yarn.QueueMetrics.AppsCompleted", "", "history"),
		AVAILABLE_MB("yarn.QueueMetrics.AvailableMB", "", "history"),
		ALLOCATED_CONTAINERS("yarn.QueueMetrics.AllocatedContainers", "", "history"),
		PENDING_CONTAINERS("yarn.QueueMetrics.PendingContainers", "", "history"),
		PENDING_VCORES("yarn.QueueMetrics.PendingVCores", "", "history"),
		RUNNING_0("yarn.QueueMetrics.running_0", "", "history"),
		APPS_KILLED("yarn.QueueMetrics.AppsKilled", "", "history"),
		RESERVED_MB("yarn.QueueMetrics.ReservedMB", "", "history"),
		ACTIVE_APPLICATIONS("yarn.QueueMetrics.ActiveApplications", "", "history"),
		RUNNING_300("yarn.QueueMetrics.running_300", "", "history"),
		RESERVED_VCORES("yarn.QueueMetrics.ReservedVCores", "", "history"),
		RUNNING_60("yarn.QueueMetrics.running_60", "", "history"),
		APPS_SUBMITTED("yarn.QueueMetrics.AppsSubmitted", "", "history"),
		ALLOCATED_VCORES("yarn.QueueMetrics.AllocatedVCores", "", "history"),
		RUNNING_1440("yarn.QueueMetrics.running_1440", "", "history"),
		APPS_FAILED("yarn.QueueMetrics.AppsFailed", "", "history"),
		APPS_PENDING("yarn.QueueMetrics.AppsPending", "", "history"),
		RESERVED_CONTAINERS("yarn.QueueMetrics.ReservedContainers", "", "history"),
		AVAILABLE_VCORES("yarn.QueueMetrics.AvailableVCores", "", "history"),
		AGGREGATE_CONTAINERS_ALLOCATED("yarn.QueueMetrics.AggregateContainersAllocated", "", "history"),
		PENDING_MB("yarn.QueueMetrics.PendingMB", "", "history");
		
		private String metricsName;
		private String description;
		private String table;
		
		private YARN_QUEUE_METRICS(String name, String description, String table) {
			this.metricsName = name;
			this.description = description;
			this.table = table;
		}
		
		@Override
		public String description() {
			return description;
		}
		
		@Override
		public String metricsName() {
			return metricsName;
		}
		
		@Override
		public String table() {
			return table;
		}
	}
	
	/**
	 * Yarn相关的参指标
	 **/
	enum YARN implements Metrics_Zabbix {
		
		NUM_ACTIVENMS("yarn.ClusterMetrics.NumActiveNMs", "", "history"),
		NUM_LOSTNMS("yarn.ClusterMetrics.NumLostNMs", "", "history");
		
		private String metricsName;
		private String description;
		private String table;
		
		private YARN(String name, String description, String table) {
			this.metricsName = name;
			this.description = description;
			this.table = table;
		}
		
		@Override
		public String description() {
			return description;
		}
		
		@Override
		public String metricsName() {
			return metricsName;
		}
		
		@Override
		public String table() {
			return table;
		}
	}
	
}
