package org.hit.monitor.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.ibatis.javassist.expr.NewArray;
import org.apache.log4j.Logger;
import org.hit.monitor.bo.QueryMetricsBO;
import org.hit.monitor.common.CONFIG;
import org.hit.monitor.common.DockerVO;
import org.hit.monitor.common.Metrics;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.model.dockermodel.CADvisor;
import org.hit.monitor.model.dockermodel.Docker;
import org.hit.monitor.model.dockermodel.DockerdataList;
import org.hit.monitor.model.dockermodel.Stats;
import org.hit.monitor.service.DockerService;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;

@Service
public class DockerServiceImpl implements DockerService {

	private CADvisor cAdvisor;
	private Logger log = Logger.getLogger(this.getClass());
		
	
	
	@Override
	public List<Stats> getData() {

		// BatchResultDTO<MetricsDataDO> result = new
		// BatchResultDTO<MetricsDataDO>();

		// 参数校验
		// if (metrics == null) {
		// return result.returnError("参数错误");
		// }

		// if (filter == null) filter = new QueryMetricsBO();
		//
		//
		// filter.setMetricsName(metrics.metricsName());
		//
		// int count = cAdvisor.getDocker().getStats().size();
		// filter.setRecord(count);
		//
		// if (count < 1) {
		//// result.setSuccess(true);
		// return null;
		// }
		//
		//// List<MetricsDataDO> list = cAdvisor.getDocker().getStats();
		List<Stats> list = cAdvisor.getDocker().get(0).getStats();

		return list;

	}

	// 以下为获取数据

	// 通过url获取json字符串
	public static String loadJson(String url) {
		StringBuilder json = new StringBuilder();
		try {
			URL urlObject = new URL(url);
			URLConnection uc = urlObject.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()));
			String inputLine = null;
			while ((inputLine = in.readLine()) != null) {
				json.append(inputLine);
			}
			in.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json.toString();
	}
	

	// 时间转换为时间戳
	public long DataTransform(String time) throws ParseException {
		long ts;
		time = time.substring(0, 10) + time.substring(11, 19);
		//System.out.println(time);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
		Date date = simpleDateFormat.parse(time);
		ts = date.getTime();
		return ts / 1000;
	}
	//所有stats里的时间转换为时间戳
	
	public Docker allDataTransform (Docker docker) throws ParseException{
		int statslength;
		docker.getSpec().setCreation_time(String.valueOf(DataTransform(docker.getSpec().getCreation_time())));
		statslength=docker.getStats().size();
		//System.out.println(statslength);
		for(int i=0;i<statslength;i++)
			try {		
					docker.getStats().get(i).setTimestamp(String.valueOf(DataTransform(docker.getStats().get(i).getTimestamp())));
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		return docker;
	}
	
	//http 获取数据 
	public ResultDTO<String> fetchDockerMetricsJSONByAPI(String uri) {
		uri = "http://10.13.30.18:8655/api/v1.3/docker/";
		System.out.println("====================="+uri);
		ResultDTO<String> resultDTO = new ResultDTO<String>();
		try {
			CloseableHttpClient httpClient = HttpClientBuilder.create().build();
			HttpGet get = new HttpGet(uri);
			get.setHeader("Accept-Charset", "utf-8");
			get.setHeader("Accept", "application/json");
			get.setHeader("Cache-Control", "no-cache");
			HttpResponse res = httpClient.execute(get);
			if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				String result = EntityUtils.toString(res.getEntity());// 返回json格式：
				//System.out.println("+++++++++++++++++++============"+result);
				return resultDTO.returnSuccess(result);
			} else {
				return resultDTO.returnError("获取API数据出错");
			}
		} catch (Exception e) {
			log.error("获取集群指标异常", e);
			return resultDTO.returnError("获取API数据异常");
		}
	}

//	public DockerServiceImpl() {
//
//		String url = "http://localhost:8090/api/v1.3/docker/";
//		String json = loadJson(url);
//		json = "{\"docker" + json.substring(74, json.length());// 将开头默认的/docker/id替换为docker以便序列化
//		JSONObject jsonobject = JSONObject.parseObject(json);// 先转换为json对象
//		cAdvisor = jsonobject.parseObject(json, CADvisor.class);// 转换为对应的类
//
//	}
	
	//逆序列化转化为实体
	public List<Docker> jsonToEntity(String json){
	   //parseObject方式
//	   json = "{\"docker" + json.substring(74, json.length());
//	   JSONObject jsonobject = JSONObject.parseObject(json);// 先转换为json对象	  
//	   docker = jsonobject.parseObject(json, Docker.class);// 转换为对应的类
		List<Docker> docker = new ArrayList<Docker>();
		//流处理方式
		JSONReader reader = null;
		reader = new JSONReader(new StringReader(json));
		reader.startObject();
		while (reader.hasNext()) {
			String key = reader.readString();
//			System.out.println(key);
			Docker vo = reader.readObject(Docker.class);			
//			System.out.println(vo);
//			System.out.println(vo.getNamespace());
			docker.add(vo);
//			System.out.println(DockerdataList.docker);
			// handle vo ...
		}
		reader.endObject();
		reader.close();		
	   return docker;
	}

	


	//以下接口实现为临时写前台用，待链接数据库后会修改获删除
	

}
