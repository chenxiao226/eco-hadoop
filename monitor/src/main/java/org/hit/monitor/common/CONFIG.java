package org.hit.monitor.common;

import org.apache.http.impl.cookie.PublicSuffixDomainFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 定义了常用API的地址信息
 */
public class CONFIG {

    private static Logger log = LoggerFactory.getLogger(CONFIG.class);

    // YARN
    public static final String YARN_NODE_MANAGER;
    public static final String YARN_RESOURCE_MANAGER;
    public static final String YARN_TIMELINE_SERVER;
  

    // Map Reduce
    public static final String MAPREDUCE_APPLICATION_MASTER;
    public static final String MAPREDUCE_HISTORY_SERVER;
    
    // Spark API
    public static final String SPARK_HISTORY_SERVER;
    
    // Ganglia API
    public static final String GANGLIA;

    // master hostname
    public static final String MASTER_HOST_NAME;
    
    // Mesos Master Address
    public static final String MESOS_MASTER;
    
    // Mesos Agent Port
    public static final String MESOS_AGENT_PORT;
    
    // HDFS Web
    public static final String HDFS_WEB;
    
    // Marathon API
    public static final String MARATHON_API;

    //alert serverip and port
    public static final String ALERT_SERVER_IP;
    public static final String ALERT_SERVER_PORT;
    //docker
    public static final String DOCKER_IP;
    public static final String DOCKER_PORT;
    
    //cassandra
/*    public static final String cassandra_IP;
    public static final String cassandra_PORT;
    
    
    //neo4j
    public static final String neo4j_IP;
    public static final String neo4j_PORT;
    */
    //tsfile
    public static final String TSFILE_JDBC_URL;
    public static final String TSFILE_JDBC_USERNAME;
    public static final String TSFILE_JDBC_PASSWORD;
    
    //InfluxDB
    
    public static final String INFLUXDB_URL;
    public static final String INFLUXDB_USERNAME;
    public static final String INFLUXDB_PASSWORD;
    public static final String INFLUXDB_DBNAME;
    public static final String CASSANDRA_TABLE;
    public static final String NEO4J_TABLE;
    
    
    static {
        Properties properties = new Properties();
        InputStream in = CONFIG.class.getClassLoader().getResourceAsStream("monitor.properties");
        try {
            properties.load(in);
        } catch (IOException e) {
            log.error("初始化API配置项失败", e);
        }
        YARN_NODE_MANAGER = properties.getProperty("yarn_node_manager").trim();
        YARN_RESOURCE_MANAGER = properties.getProperty("yarn_resource_manager").trim();
        YARN_TIMELINE_SERVER = properties.getProperty("yarn_timeline_server").trim();
        MAPREDUCE_APPLICATION_MASTER = properties.getProperty("mapreduce_application_master").trim();
        MAPREDUCE_HISTORY_SERVER = properties.getProperty("mapreduce_history_server").trim();
        GANGLIA = properties.getProperty("ganglia_api").trim();
        MASTER_HOST_NAME = properties.getProperty("master_host_name").trim();
        SPARK_HISTORY_SERVER = properties.getProperty("spark_history_api").trim();
        MESOS_MASTER = "http://" + MASTER_HOST_NAME + ":" + properties.getProperty("mesos_master_port").trim();
        MESOS_AGENT_PORT = properties.getProperty("mesos_agent_port").trim();
        HDFS_WEB = properties.getProperty("hdfs_web").trim();
        MARATHON_API = properties.getProperty("marathon_api").trim();
        ALERT_SERVER_IP = properties.getProperty("alert_server_ip").trim();
        ALERT_SERVER_PORT = properties.getProperty("alert_server_port").trim();
       /* cassandra_IP = properties.getProperty("cassandra_server_ip").trim();
        cassandra_PORT = properties.getProperty("cassandra_server_port").trim();
        neo4j_IP = properties.getProperty("neo4j_server_ip").trim();
        neo4j_PORT = properties.getProperty("neo4j_server_port").trim();*/
        TSFILE_JDBC_URL = properties.getProperty("tsfile.jdbc.url").trim();
        TSFILE_JDBC_USERNAME = properties.getProperty("tsfile.jdbc.username").trim();
        TSFILE_JDBC_PASSWORD = properties.getProperty("tsfile.jdbc.password").trim();
        INFLUXDB_URL = properties.getProperty("influxDB_url").trim();
        INFLUXDB_USERNAME = properties.getProperty("influxDB_username").trim();
        INFLUXDB_PASSWORD = properties.getProperty("influxDB_password").trim();
        INFLUXDB_DBNAME = properties.getProperty("influxDB_dbname").trim();
        CASSANDRA_TABLE = properties.getProperty("cassandra_table").trim();
        NEO4J_TABLE = properties.getProperty("neo4j_table").trim();
        DOCKER_IP = properties.getProperty("docker_server_ip").trim();
        DOCKER_PORT = properties.getProperty("docker_server_port").trim();
    }
}
