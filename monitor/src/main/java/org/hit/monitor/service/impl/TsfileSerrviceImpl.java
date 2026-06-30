package org.hit.monitor.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.hit.monitor.common.CONFIG;
import org.hit.monitor.service.TsfileSerrvice;
import org.springframework.stereotype.Service;

@Service("TsfileSerrvice")
public class TsfileSerrviceImpl implements TsfileSerrvice {
	//写入成功点数次数
	@Override
	public String fetchTotalPoints() throws Exception {
		 Connection connection = null;
		 String TOTAL_POINTS_SUCCESS =null;
		 Statement statement = null;
		    try {
		    	Class.forName("cn.edu.tsinghua.iotdb.jdbc.TsfileDriver");
		        connection = DriverManager.getConnection(CONFIG.TSFILE_JDBC_URL, CONFIG.TSFILE_JDBC_USERNAME, CONFIG.TSFILE_JDBC_PASSWORD);
		        statement = connection.createStatement();
		        boolean hasResultSet = statement.execute(" select MAX_VALUE(TOTAL_POINTS_SUCCESS) from root.stats.write.global");
		        if (hasResultSet) {
		            ResultSet res = statement.getResultSet();	
		           // Map<String, String> m = new HashMap<>();
		            /*while (res.next()) {           	
		            	//m.put("Time", res.getString("Time"));
		            	//m.put("TOTAL_POINTS_SUCCESS",res.getString("root.stats.write.global.TOTAL_POINTS_SUCCESS"));
		                System.out.println(res.getString(1) + " | " + res.getString(2));
		            	
		            }*/
		            //System.out.println(m);
		            res.next();
		            TOTAL_POINTS_SUCCESS = res.getString(2);
		            //TOTAL_POINTS = res.getString("MAX_VALUE(TOTAL_POINTS_SUCCESS)");
		            
		            //MAX_TIME = res.getString("MAX_VALUE(root.stats.write.global.TOTAL_POINTS_SUCCESS)");
		          // System.out.println(TOTAL_POINTS/*+"    "+MAX_TIME*/);
		        }
		    	
		    } catch (Exception e) {
		        e.printStackTrace();
		    } finally {
		        if(statement != null){
		            statement.close();
		        }
		        if(connection != null){
		            connection.close();
		        }
		    }
		return TOTAL_POINTS_SUCCESS;
	}
	
	//全局失败次数
	@Override
	public String fetchTotalPointsFAIL() throws Exception {
		 Connection connection = null;
		 String TOTAL_POINTS_FAIL =null;
		 Statement statement = null;
		    try {
		    	Class.forName("cn.edu.tsinghua.iotdb.jdbc.TsfileDriver");
		        connection = DriverManager.getConnection(CONFIG.TSFILE_JDBC_URL, CONFIG.TSFILE_JDBC_USERNAME, CONFIG.TSFILE_JDBC_PASSWORD);
		        statement = connection.createStatement();
		        boolean hasResultSet = statement.execute(" select MAX_VALUE(TOTAL_POINTS_FAIL) from root.stats.write.global");
		        if (hasResultSet) {
		            ResultSet res = statement.getResultSet();	
		           // Map<String, String> m = new HashMap<>();
		            /*while (res.next()) {           	
		            	//m.put("Time", res.getString("Time"));
		            	//m.put("TOTAL_POINTS_SUCCESS",res.getString("root.stats.write.global.TOTAL_POINTS_SUCCESS"));
		                System.out.println(res.getString(1) + " | " + res.getString(2));
		            	
		            }*/
		            //System.out.println(m);
		            res.next();
		            TOTAL_POINTS_FAIL = res.getString(2);
		            //TOTAL_POINTS = res.getString("MAX_VALUE(TOTAL_POINTS_SUCCESS)");
		            
		            //MAX_TIME = res.getString("MAX_VALUE(root.stats.write.global.TOTAL_POINTS_SUCCESS)");
		          // System.out.println(TOTAL_POINTS/*+"    "+MAX_TIME*/);
		        }
		    	
		    } catch (Exception e) {
		        e.printStackTrace();
		    } finally {
		        if(statement != null){
		            statement.close();
		        }
		        if(connection != null){
		            connection.close();
		        }
		    }
		return TOTAL_POINTS_FAIL;
	}
	
	
	//全局写入请求成功次数 
	@Override
	public String fetchREQSuccess() throws Exception {
		 Connection connection = null;
		 String REQ_SUCCESS =null;
		 Statement statement = null;
		    try {
		    	Class.forName("cn.edu.tsinghua.iotdb.jdbc.TsfileDriver");
		        connection = DriverManager.getConnection(CONFIG.TSFILE_JDBC_URL, CONFIG.TSFILE_JDBC_USERNAME, CONFIG.TSFILE_JDBC_PASSWORD);
		        statement = connection.createStatement();
		        boolean hasResultSet = statement.execute(" select MAX_VALUE(TOTAL_REQ_SUCCESS) from root.stats.write.global");
		        if (hasResultSet) {
		            ResultSet res = statement.getResultSet();	
		           // Map<String, String> m = new HashMap<>();
		            /*while (res.next()) {           	
		            	//m.put("Time", res.getString("Time"));
		            	//m.put("TOTAL_POINTS_SUCCESS",res.getString("root.stats.write.global.TOTAL_POINTS_SUCCESS"));
		                System.out.println(res.getString(1) + " | " + res.getString(2));
		            	
		            }*/
		            //System.out.println(m);
		            res.next();
		            REQ_SUCCESS = res.getString(2);
		            //TOTAL_POINTS = res.getString("MAX_VALUE(TOTAL_POINTS_SUCCESS)");
		            
		            //MAX_TIME = res.getString("MAX_VALUE(root.stats.write.global.TOTAL_POINTS_SUCCESS)");
		          // System.out.println(TOTAL_POINTS/*+"    "+MAX_TIME*/);
		        }
		    	
		    } catch (Exception e) {
		        e.printStackTrace();
		    } finally {
		        if(statement != null){
		            statement.close();
		        }
		        if(connection != null){
		            connection.close();
		        }
		    }
		return REQ_SUCCESS;
	}
	
	
	//全局写入请求失败次数
	@Override
	public String fetchREQFail() throws Exception {
		 Connection connection = null;
		 String REQ_FAIL =null;
		 Statement statement = null;
		    try {
		    	Class.forName("cn.edu.tsinghua.iotdb.jdbc.TsfileDriver");
		        connection = DriverManager.getConnection(CONFIG.TSFILE_JDBC_URL, CONFIG.TSFILE_JDBC_USERNAME, CONFIG.TSFILE_JDBC_PASSWORD);
		        statement = connection.createStatement();
		        boolean hasResultSet = statement.execute(" select MAX_VALUE(TOTAL_REQ_FAIL) from root.stats.write.global");
		        if (hasResultSet) {
		            ResultSet res = statement.getResultSet();	
		           // Map<String, String> m = new HashMap<>();
		            /*while (res.next()) {           	
		            	//m.put("Time", res.getString("Time"));
		            	//m.put("TOTAL_POINTS_SUCCESS",res.getString("root.stats.write.global.TOTAL_POINTS_SUCCESS"));
		                System.out.println(res.getString(1) + " | " + res.getString(2));
		            	
		            }*/
		            //System.out.println(m);
		            res.next();
		            REQ_FAIL = res.getString(2);
		            //TOTAL_POINTS = res.getString("MAX_VALUE(TOTAL_POINTS_SUCCESS)");
		            
		            //MAX_TIME = res.getString("MAX_VALUE(root.stats.write.global.TOTAL_POINTS_SUCCESS)");
		          // System.out.println(TOTAL_POINTS/*+"    "+MAX_TIME*/);
		        }
		    	
		    } catch (Exception e) {
		        e.printStackTrace();
		    } finally {
		        if(statement != null){
		            statement.close();
		        }
		        if(connection != null){
		            connection.close();
		        }
		    }
		return REQ_FAIL;
	}
	
}
