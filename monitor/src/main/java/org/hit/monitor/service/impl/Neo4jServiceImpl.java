package org.hit.monitor.service.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.hit.monitor.service.Neo4jService;
import org.influxdb.InfluxDB;
import org.influxdb.dto.QueryResult;
import org.springframework.stereotype.Service;

@Service("Neo4jService")
public class Neo4jServiceImpl implements Neo4jService {
	
	static JMXConnector connector;

	@Override
	public Map<String, Object> fetchKernelMetrics() throws MalformedObjectNameException, IOException {
		Map<String, Object> KernelMetrics = new HashMap<String, Object>();
		KernelMetrics.put("dbName", query("select last(dbName) from neo4j"));	
		KernelMetrics.put("KernelStartTime", query("select last(KernelStartTime) from neo4j"));
		KernelMetrics.put("StoreCreationDate",query("select last(StoreCreationDate) from neo4j"));
		return KernelMetrics;
	}

	@Override
	public Map<String, Object> fetchPageCacheMetrics() throws MalformedObjectNameException, IOException {
		Map<String, Object> PageCacheMetrics = new HashMap<String, Object>();
		PageCacheMetrics.put("BytesRead", query("select last(BytesRead) from neo4j"));
		PageCacheMetrics.put("BytesWritten", query("select last(BytesWritten) from neo4j"));
		PageCacheMetrics.put("FileMappings",query("select last(FileMappings) from neo4j"));
		PageCacheMetrics.put("FileUnmappings", query("select last(FileUnmappings) from neo4j"));
		PageCacheMetrics.put("Evictions", query("select last(Evictions) from neo4j"));
		return PageCacheMetrics;
	}

	@Override
	public Map<String, Object> fetchPrimitiveCount() throws MalformedObjectNameException, IOException {
		Map<String, Object> PrimitiveCount = new HashMap<String, Object>();
		PrimitiveCount.put("NumberOfNodeIdsInUse", query("select last(NumberOfNodeIdsInUse) from neo4j"));
		PrimitiveCount.put("NumberOfPropertyIdsInUse", query("select last(NumberOfPropertyIdsInUse) from neo4j"));
		PrimitiveCount.put("NumberOfRelationshipIdsInUse", query("select last(NumberOfRelationshipIdsInUse) from neo4j"));
		PrimitiveCount.put("NumberOfRelationshipTypeIdsInUse", query("select last(NumberOfRelationshipTypeIdsInUse) from neo4j"));
		return PrimitiveCount;
	}
	
	
	/*以下为获取数据 建立连接的通用步骤*/
	
	/*private static Object getMetrics(String objectname,String property)throws IOException, MalformedObjectNameException {


	     MBeanServerConnection mbsc = createMBeanServer(CONFIG.neo4j_IP, CONFIG.neo4j_PORT); 
		
	     ObjectName objName = new ObjectName(objectname);     
	    
	     return getAttributes(mbsc,objName,property);
	}
	//建立rmi连接
	public static MBeanServerConnection createMBeanServer(String ip, String jmxport){
		//jmxurl
		
		try {
			String jmxURL = "service:jmx:rmi:///jndi/rmi://"+ip+":"+jmxport+"/jmxrmi";
			JMXServiceURL serviceURL = new JMXServiceURL(jmxURL);
		    connector = JMXConnectorFactory.connect(serviceURL);
		    MBeanServerConnection mbsc = connector.getMBeanServerConnection();
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
		 String retentionPolicy = CONFIG.NEO4J_TABLE;
		 InfluxDB influxDB = new InfluxDBUtils(url, user, password).builder();//建立influxdb连接
	     InfluxdbService service = new org.hit.monitor.common.InfluxdbService(dbname,retentionPolicy,influxDB);
	     QueryResult qResult  = service.query(cmd);
	     Object obj = qResult.getResults().get(0).getSeries().get(0).getValues().get(0).get(1);
		return obj.toString();
	}
}
