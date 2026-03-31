package org.hit.monitor.service.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.hit.monitor.common.CONFIG;
import org.hit.monitor.common.InfluxDBUtils;
import org.hit.monitor.common.InfluxdbService;
import org.hit.monitor.service.CassandraService;
import org.influxdb.InfluxDB;
import org.influxdb.dto.QueryResult;
import org.springframework.stereotype.Service;




@Service("CassandraService")
public class CassandraServiceImpl implements CassandraService {
	static JMXConnector connector_cass;
	
	@Override
	public Map<String, Object> fetchTableCQLMetrics() throws MalformedObjectNameException, IOException {
		Map<String, Object> TableCQLMetrics = new HashMap<String, Object>();
		TableCQLMetrics.put("MemtableLiveDataSize", query("select last(MemtableLiveDataSize) from cassandra"));	
		TableCQLMetrics.put("MemtableColumnsCount", query("select last(MemtableColumnsCount) from cassandra"));
		TableCQLMetrics.put("Exceptions",query("select last(Exceptions) from cassandra"));
		//TableCQLMetrics.put("PreparedStatementsEvicted",query("select last(PreparedStatementsEvicted) from cassandra"));
		TableCQLMetrics.put("PreparedStatementsExecuted",query("select last(PreparedStatementsExecuted) from cassandra"));
		TableCQLMetrics.put("RegularStatementsExecuted",query("select last(RegularStatementsExecuted) from cassandra"));
		/*System.out.println("========================>"+TableCQLMetrics);
		System.out.println("========================>"+getMetrics("org.apache.cassandra.metrics:type=Table,name=MemtableLiveDataSize","Value"));*/
		return TableCQLMetrics;
	}

	@Override
	public Map<String, Object> fetchThreadPoolsMetrics() throws MalformedObjectNameException, IOException {
		Map<String, Object> ThreadPoolsMetrics = new HashMap<String, Object>();
		ThreadPoolsMetrics.put("ActiveTasks", query("select last(ActiveTasks) from cassandra"));
		ThreadPoolsMetrics.put("PendingTasks", query("select last(PendingTasks) from cassandra"));
		ThreadPoolsMetrics.put("CompletedTasks",query("select last(CompletedTasks) from cassandra"));	
		ThreadPoolsMetrics.put("MaxPoolSize", query("select last(MaxPoolSize) from cassandra"));
		return ThreadPoolsMetrics;
	}
	
	//keycache hit metrics
	@Override
	public Map<String, Object> fetchCacheRatio() throws MalformedObjectNameException, IOException {
		Map<String, Object> CacheRatio = new HashMap<String, Object>();
		CacheRatio.put("HitRate", query("select last(HitRate) from cassandra"));
		CacheRatio.put("Hitcounts", query("select last(Hitcounts) from cassandra"));
		CacheRatio.put("OneMinuteRate", query("select last(OneMinuteRate) from cassandra"));
		CacheRatio.put("FiveMinuteRate", query("select last(FiveMinuteRate) from cassandra"));
		return CacheRatio;
	}
	
	/*与neo4j相同的建立连接过程*/
	/*private static Object getMetrics(String objectname,String property)throws IOException, MalformedObjectNameException {


	     MBeanServerConnection mbsc = createMBeanServer(CONFIG.cassandra_IP, CONFIG.cassandra_PORT); 
		
	     ObjectName objName = new ObjectName(objectname);     
	    
	     return getAttributes(mbsc,objName,property);
	}
	//建立rmi连接
	public static MBeanServerConnection createMBeanServer(String ip, String jmxport){
		//jmxurl
		
		try {
			String jmxURL = "service:jmx:rmi:///jndi/rmi://"+ip+":"+jmxport+"/jmxrmi";
			JMXServiceURL serviceURL = new JMXServiceURL(jmxURL);
			connector_cass = JMXConnectorFactory.connect(serviceURL);
		    MBeanServerConnection mbsc = connector_cass.getMBeanServerConnection();
		    System.out.println("========================>连接成功");
		    return mbsc;
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
			System.out.println(ip+":"+jmxport+"连接失败");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(ip+":"+jmxport+"连接失败");
		}
		
		return null;
	}
	*//**
	 * 使用MBeanServer获取对象名为[objName]的MBean的[objAttr]属性值

	 * 静态代码: return MBeanServer.getAttribute(ObjectName name, String attribute)
	 
	 *//*

	private static Object getAttributes(MBeanServerConnection mbeanServer,
	        ObjectName objName, String objAttr) {
	    if (mbeanServer == null || objName == null || objAttr == null)
	        throw new IllegalArgumentException();
	    try {
	        return mbeanServer.getAttribute(objName, objAttr);
	   
	    } catch (Exception e) {
	        return null;
	    }
	}*/
	//读取西工大influxdb ， 暂时舍弃本地 获取方式，获取 日后改为传object 以数据库中的时间为准
	private static String query(String cmd){
		
		String url = CONFIG.INFLUXDB_URL;//获取数据库的检测ip
	     String user = CONFIG.INFLUXDB_USERNAME;
	     String password = CONFIG.INFLUXDB_PASSWORD;
	     String dbname = CONFIG.INFLUXDB_DBNAME;
		 String retentionPolicy = CONFIG.CASSANDRA_TABLE;
		 InfluxDB influxDB = new InfluxDBUtils(url, user, password).builder();//建立influxdb连接
	     InfluxdbService service = new org.hit.monitor.common.InfluxdbService(dbname,retentionPolicy,influxDB);
	     QueryResult qResult  = service.query(cmd);
	     Object obj = qResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(1);
		return obj.toString();
	}
	
	public Map<String, Object> test(){
		String t = query("select last(storageLoad) from cassandra");
		Map<String, Object> tet = new HashMap<String, Object>();
		tet.put("storageLoad",t);
		tet.put("conf", CONFIG.INFLUXDB_URL);
		return tet;
	}
}
