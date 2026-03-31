package org.hit.monitor.service;

import org.hit.monitor.bo.QueryYarnAppResultBO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.model.YarnAppResultDO;

public interface YarnAppResultService {
	/**
	 * 分页查询AppResult
	 */
	public BatchResultDTO<YarnAppResultDO> queryAppResultByPage(QueryYarnAppResultBO query);

	/**
	 * 根据AppID,获得一个详细的数据信息
	 */
	public ResultDTO<YarnAppResultDO> queryAppResultDetailsById(QueryYarnAppResultBO query);
    
	/**
	 * 将分析后的结果插入数据库中
	 */
	public void insertAppResultDetails(YarnAppResultDO yarnAppResultDO);
	
	/**
	 * @method: getLastTime 
	 * @Description: 获得最后分析的时间 
	 * Long 返回类型
	 */
	public Long getLastTime();
	
}
