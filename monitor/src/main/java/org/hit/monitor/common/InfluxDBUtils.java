package org.hit.monitor.common;
/**
 *@version:2018年4月23日下午9:02:03
*/
/**
 * @author Administrator
 *
 */


import org.influxdb.InfluxDB;

import org.influxdb.InfluxDBFactory;

public class InfluxDBUtils {

	    private String userName;
	    private String password;
	    private String url;
	    private InfluxDB influxDB;

	    public InfluxDBUtils(String url,String userName, String password) {
	        this.userName = userName;
	        this.password = password;
	        this.url = url;
	    }

	    public InfluxDB builder(){
	        if(influxDB == null){
	            synchronized (this){
	                if(influxDB == null){
	                    influxDB = InfluxDBFactory.connect(url,userName,password);
	                }
	            }
	        }
	        return influxDB;
	    }

	    public String getUserName() {
	        return userName;
	    }

	    public void setUserName(String userName) {
	        this.userName = userName;
	    }

	    public String getPassword() {
	        return password;
	    }

	    public void setPassword(String password) {
	        this.password = password;
	    }

	    public String getUrl() {
	        return url;
	    }

	    public void setUrl(String url) {
	        this.url = url;
	    }

	    public InfluxDB getInfluxDB() {
	        return influxDB;
	    }

	    public void setInfluxDB(InfluxDB influxDB) {
	        this.influxDB = influxDB;
	    }
	}

