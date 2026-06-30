package org.hit.monitor.service;

import java.text.ParseException;
import java.util.List;

import org.hit.monitor.bo.QueryMetricsBO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.DockerVO;
import org.hit.monitor.common.Metrics;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.model.MetricsDataDO;
import org.hit.monitor.model.dockermodel.CADvisor;
import org.hit.monitor.model.dockermodel.Docker;
import org.hit.monitor.model.dockermodel.Stats;

public interface DockerService {
	

	public List<Stats> getData();


	
	public List<Docker> jsonToEntity(String json);
	
	public ResultDTO<String> fetchDockerMetricsJSONByAPI(String url);

	public Docker allDataTransform (Docker docker) throws ParseException;
	
	//以下接口为临时写前台用，待链接数据库后会修改获删除

}
