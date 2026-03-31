package org.hit.monitor.common;

/**
 * 定义了所有系统监控的指标
 */
public interface Metrics_Ganglia {

    /**
     * 返回指标的名称，也代表了指标在数据库中的表名
     */
    String metricsName();

    /**
     * 返回指标的描述
     */
    String description();


    /**
     * DataNode相关指标
     **/
    enum DataNode implements Metrics {

        Write_Block_Op_Avg_Time("m_dfs.datanode.WriteBlockOpAvgTime",""),
        Read_Block_Op_Avg_Time("m_dfs.datanode.ReadBlockOpAvgTime",""),
        Blocks_Written("m_dfs.datanode.BlocksWritten",""),
        Blocks_Read("m_dfs.datanode.BlocksRead",""),
        Bytes_Writen("m_dfs.datanode.BytesWritten",""),
        Bytes_Read("m_dfs.datanode.BytesRead","");


        private String metricsName;
        private String description;

        private DataNode(String name, String description) {
            this.metricsName = name;
            this.description = description;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public String metricsName() {
            return metricsName;
        }
    }


    /**
     * CPU相关的参指标
     **/
    enum CPU implements Metrics {

        AIDLE("m_cpu_aidle", ""),
        NUM("m_cpu_num", "cpu数量"),
        SPEED("m_cpu_speed", "cpu主频"),
        STEAL("m_cpu_steal", ""),
        IDLE("m_cpu_idle", "CPU空闲比率"),
        NICE("m_cpu_nice", "用户进程空间内改变过优先级的进程占用CPU百分比 "),
        SYSTEM("m_cpu_system", "内核空间所占CPU"),
        USER("m_cpu_user", "用户空间所占CPU"),
        WIO("m_cpu_wio", "CPU由于一些显著的I/O而进入空闲状态所占的比例");

        private String metricsName;
        private String description;

        private CPU(String name, String description) {
            this.metricsName = name;
            this.description = description;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public String metricsName() {
            return metricsName;
        }
    }

    /**
     * 内存相关的参指标
     **/
    enum MEMORY implements Metrics {

        BUFFERS("m_mem_buffers", ""),
        CACHED("m_mem_cached", ""),
        FREE("m_mem_free", ""),
        SHARED("m_mem_shared", ""),
        TOTAL("m_mem_total", ""),
        SWAP_FREE("m_swap_free", ""),
        SWAP_TOTAL("m_swap_total", "");

        private String metricsName;
        private String description;

        private MEMORY(String name, String description) {
            this.metricsName = name;
            this.description = description;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public String metricsName() {
            return metricsName;
        }
    }

    /**
     * 负载相关的参指标
     **/
    enum LOAD implements Metrics {

        LOAD_ONE("m_load_one", ""),
        LOAD_FIVE("m_load_five", ""),
        LOAD_FIFTE("m_load_fifte", "");

        private String metricsName;
        private String description;

        private LOAD(String name, String description) {
            this.metricsName = name;
            this.description = description;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public String metricsName() {
            return metricsName;
        }
    }

    /**
     * 进程相关的参指标
     **/
    enum PROCESS implements Metrics {

        PROC_RUN("m_proc_run", ""),
        PROC_TOTAL("m_proc_total", "");

        private String metricsName;
        private String description;

        private PROCESS(String name, String description) {
            this.metricsName = name;
            this.description = description;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public String metricsName() {
            return metricsName;
        }
    }

    /**
     * 网络相关的参指标
     **/
    enum NETWORK implements Metrics {

        BYTES_IN("m_bytes_in", ""),
        BYTES_OUT("m_bytes_out", ""),
        PKTS_IN("m_pkts_in", ""),
        PKTS_OUT("m_pkts_out", "");

        private String metricsName;
        private String description;

        private NETWORK(String name, String description) {
            this.metricsName = name;
            this.description = description;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public String metricsName() {
            return metricsName;
        }
    }

    /**
     * 磁盘相关的参指标
     **/
    enum DISK implements Metrics {

        DISK_FREE("m_disk_free", ""),
        DISK_TOTAL("m_disk_total", ""),
        PART_MAX_USED("m_part_max_used", "");

        private String metricsName;
        private String description;

        private DISK(String name, String description) {
            this.metricsName = name;
            this.description = description;
        }

        @Override
        public String description() {
            return description;
        }

		@Override
		public String metricsName() {
			return metricsName;
		}
	}

	/** namenode相关的指标 **/
	enum NAMENODE implements Metrics {

		GC_TIME("m_jvm.JvmMetrics.GcTimeMillis",""),
		GC_COUNT("m_jvm.JvmMetrics.GcCount",""),
		MEM_HEAP_USED_M("m_jvm.JvmMetrics.MemHeapUsedM","Current non-heap memory used in MB"),
		THREADS_BLOCKED("m_jvm.JvmMetrics.ThreadsBlocked",""),
		THREADS_WAITING("m_jvm.JvmMetrics.ThreadsWaiting",""),
		TOTAL_FILES("m_dfs.FSNamesystem.TotalFiles",""),
		BLOCKS_TOTAL("m_dfs.FSNamesystem.BlocksTotal",""),
		CORRUPT_BLOCKS("m_dfs.FSNamesystem.CorruptBlocks",""),
		MISSING_BLOCKS("m_dfs.FSNamesystem.MissingBlocks",""),
		CAPACITY_TOTAL("m_dfs.FSNamesystem.CapacityTotal","Current raw capacity of DataNodes in bytes"),
		CAPACITY_USED("m_dfs.FSNamesystem.CapacityUsed","Current used capacity across all DataNodes in bytes"),
		MEM_HEAP_MAX_M("m_jvm.JvmMetrics.MemHeapMaxM","");

		private String metricsName;
		private String description;

		private NAMENODE(String name,String description) {
			this.metricsName = name;
			this.description = description;
		}

		@Override
		public String description() {
			return description;
		}

        @Override
        public String metricsName() {
            return metricsName;
        }

    }

    /**
     * RPC相关的参指标
     **/
    enum RPC implements Metrics {

        RETRY_CACHE_NAMENODE_RETRY_CACHE_CACHE_CLEARED("m_rpc.RetryCache.NameNodeRetryCache.CacheCleared", ""),
        RETRY_CACHE_NAMENODE_RETRY_CACHE_CACHE_HIT("m_rpc.RetryCache.NameNodeRetryCache.CacheHit", ""),
        RETRY_CACHE_NAMENODE_RETRY_CACHE_CACHE_UPDATED("m_rpc.RetryCache.NameNodeRetryCache.CacheUpdated", ""),
        RPC_CALL_QUEUE_LENGTH("m_rpc.rpc.CallQueueLength", ""),
        RPC_NUM_OPEN_CONNECTIONS("m_rpc.rpc.NumOpenConnections", ""),
        RPC_RECEIVED_BYTES("m_rpc.rpc.ReceivedBytes", ""),
        RPC_AUTHENTICATION_FAILURES("m_rpc.rpc.RpcAuthenticationFailures", ""),
        RPC_AUTHENTICATION_SUCCESSES("m_rpc.rpc.RpcAuthenticationSuccesses", ""),
        RPC_AUTHORIZATION_FAILURES("m_rpc.rpc.RpcAuthorizationFailures", ""),
        RPC_AUTHORIZATION_SUCCESSES("m_rpc.rpc.RpcAuthorizationSuccesses", ""),
        RPC_PROCESSING_TIME_AVG_TIME("m_rpc.rpc.RpcProcessingTimeAvgTime", ""),
        RPC_PROCESSING_TIME_NUM_OPS("m_rpc.rpc.RpcProcessingTimeNumOps", ""),
        RPC_QUEUE_TIME_AVG_TIME("m_rpc.rpc.RpcQueueTimeAvgTime", ""),
        RPC_QUEUE_TIME_NUM_OPS("m_rpc.rpc.RpcQueueTimeNumOps", ""),
        RPC_SENT_BYTES("m_rpc.rpc.SentBytes", "");

        private String metricsName;
        private String description;

        private RPC(String name, String description) {
            this.metricsName = name;
            this.description = description;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public String metricsName() {
            return metricsName;
        }
    }

    /**
     * YARN相关的参指标
     **/
    enum YARN_QUEUE_METRICS implements Metrics {

        AGGREGATE_CONTAINERS_RELEASED("m_yarn.QueueMetrics.AggregateContainersReleased", ""),
        APPS_RUNNING("m_yarn.QueueMetrics.AppsRunning", ""),
        ALLOCATED_MB("m_yarn.QueueMetrics.AllocatedMB", ""),
        ACTIVE_USERS("m_yarn.QueueMetrics.ActiveUsers", ""),
        APPS_COMPLETED("m_yarn.QueueMetrics.AppsCompleted", ""),
        AVAILABLE_MB("m_yarn.QueueMetrics.AvailableMB", ""),
        ALLOCATED_CONTAINERS("m_yarn.QueueMetrics.AllocatedContainers", ""),
        PENDING_CONTAINERS("m_yarn.QueueMetrics.PendingContainers", ""),
        PENDING_VCORES("m_yarn.QueueMetrics.PendingVCores", ""),
        RUNNING_0("m_yarn.QueueMetrics.running_0", ""),
        APPS_KILLED("m_yarn.QueueMetrics.AppsKilled", ""),
        RESERVED_MB("m_yarn.QueueMetrics.ReservedMB", ""),
        ACTIVE_APPLICATIONS("m_yarn.QueueMetrics.ActiveApplications", ""),
        RUNNING_300("m_yarn.QueueMetrics.running_300", ""),
        RESERVED_VCORES("m_yarn.QueueMetrics.ReservedVCores", ""),
        RUNNING_60("m_yarn.QueueMetrics.running_60", ""),
        APPS_SUBMITTED("m_yarn.QueueMetrics.AppsSubmitted", ""),
        ALLOCATED_VCORES("m_yarn.QueueMetrics.AllocatedVCores", ""),
        RUNNING_1440("m_yarn.QueueMetrics.running_1440", ""),
        APPS_FAILED("m_yarn.QueueMetrics.AppsFailed", ""),
        APPS_PENDING("m_yarn.QueueMetrics.AppsPending", ""),
        RESERVED_CONTAINERS("m_yarn.QueueMetrics.ReservedContainers", ""),
        AVAILABLE_VCORES("m_yarn.QueueMetrics.AvailableVCores", ""),
        AGGREGATE_CONTAINERS_ALLOCATED("m_yarn.QueueMetrics.AggregateContainersAllocated", ""),
        PENDING_MB("m_yarn.QueueMetrics.PendingMB", "");

        private String metricsName;
        private String description;

        private YARN_QUEUE_METRICS(String name, String description) {
            this.metricsName = name;
            this.description = description;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public String metricsName() {
            return metricsName;
        }
    }
    /**
     * Yarn相关的参指标
     **/
    enum YARN implements Metrics {

 	   NUM_ACTIVENMS("m_yarn.ClusterMetrics.NumActiveNMs",""),
 	   NUM_LOSTNMS("m_yarn.ClusterMetrics.NumLostNMs","");

        private String metricsName;
        private String description;

        private YARN(String name, String description) {
            this.metricsName = name;
            this.description = description;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public String metricsName() {
            return metricsName;
        }
    }
    /**
     * 功耗相关的参指标
     **/
    enum POWER implements Metrics {

        MASTER_POWER("p_master_power", ""),
        SLAVE_ONE_POWER("p_slave1_power", ""),
        SLAVE_TWO_POWER("p_slave2_power", ""),
        SLAVE_THREE_POWER("p_slave3_power", ""),
        CLUSTER_AVERAGE_POWER("p_cluster_average_power","");


        private String metricsName;
        private String description;

        private POWER(String name, String description) {
            this.metricsName = name;
            this.description = description;
        }

        @Override
        public String description() {
            return description;
        }

        @Override
        public String metricsName() {
            return metricsName;
        }
    }
    
}
