package org.hit.monitor.common;
/**
 *@version:2018年4月23日下午9:08:22
*/
/**
 * @author Administrator
 *
 */

import org.influxdb.InfluxDB;

import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.util.Map;
public class InfluxdbService {

    private String database;
    private String policyName;
    private InfluxDB influxDB;

    public InfluxdbService(String database, String policyName, InfluxDB influxDB) {
        this.database = database;
        this.policyName = policyName;
        this.influxDB = influxDB;
    }


    public QueryResult query(String cmd){
        return influxDB.query(new Query(cmd,database));
    }

   

    public String getDatabase() {
        return database;
    }

    public String getPolicyName() {
        return policyName;
    }

    public InfluxDB getInfluxDB() {
        return influxDB;
    }
}