package org.hit.monitor.service;

import org.hit.monitor.bo.QueryYarnAppHeuristicResultDetailsBO;
import org.hit.monitor.common.BaseResultDTO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.model.YarnAppHeuristicResultDetailsDO;

public interface YarnAppHeuristicResultDetailsService {

	/**
	 * 根据id查询
	 */
	public ResultDTO<YarnAppHeuristicResultDetailsDO> queryYarnAppHeuristicResultDetailsById(Long name);
	
	/**
	 * 查询一个，必须传入能唯一确定一个的参数
	 */
	public ResultDTO<YarnAppHeuristicResultDetailsDO> queryOne(QueryYarnAppHeuristicResultDetailsBO query);

	/**
	 * 查询列表
	 */
	public BatchResultDTO<YarnAppHeuristicResultDetailsDO> queryYarnAppHeuristicResultDetailsList(QueryYarnAppHeuristicResultDetailsBO query);
	
	/**
	 * 分页查询
	 */
	public BatchResultDTO<YarnAppHeuristicResultDetailsDO> queryYarnAppHeuristicResultDetailsPage(QueryYarnAppHeuristicResultDetailsBO query);

	/**
	 * 添加
	 */
	public BaseResultDTO createYarnAppHeuristicResultDetails(YarnAppHeuristicResultDetailsDO yarnAppHeuristicResultDetails);
	
	/**
	 * 选择性修改，对于空的字段，不进行修改
	 */
	public BaseResultDTO modifyYarnAppHeuristicResultDetails(YarnAppHeuristicResultDetailsDO yarnAppHeuristicResultDetails);
	
	/**
	 * 完全修改，对于空的字段，也修改
	 */
	public BaseResultDTO modifyYarnAppHeuristicResultDetailsCompletely(YarnAppHeuristicResultDetailsDO yarnAppHeuristicResultDetails);
	
	/**
	 * 逻辑删除
	 */
	public BaseResultDTO removeYarnAppHeuristicResultDetails(YarnAppHeuristicResultDetailsDO yarnAppHeuristicResultDetails);
	
}