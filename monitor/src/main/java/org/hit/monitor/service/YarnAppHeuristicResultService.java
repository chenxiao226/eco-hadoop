package org.hit.monitor.service;

import org.hit.monitor.bo.QueryYarnAppHeuristicResultBO;
import org.hit.monitor.common.BaseResultDTO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.model.YarnAppHeuristicResultDO;

public interface YarnAppHeuristicResultService {

	/**
	 * 根据id查询
	 */
	public ResultDTO<YarnAppHeuristicResultDO> queryYarnAppHeuristicResultById(Long id);
	
	/**
	 * 查询一个，必须传入能唯一确定一个的参数
	 */
	public ResultDTO<YarnAppHeuristicResultDO> queryOne(QueryYarnAppHeuristicResultBO query);

	/**
	 * 查询列表
	 */
	public BatchResultDTO<YarnAppHeuristicResultDO> queryYarnAppHeuristicResultList(QueryYarnAppHeuristicResultBO query);
	
	/**
	 * 分页查询
	 */
	public BatchResultDTO<YarnAppHeuristicResultDO> queryYarnAppHeuristicResultPage(QueryYarnAppHeuristicResultBO query);

	/**
	 * 添加
	 */
	public BaseResultDTO createYarnAppHeuristicResult(YarnAppHeuristicResultDO yarnAppHeuristicResult);
	
	/**
	 * 选择性修改，对于空的字段，不进行修改
	 */
	public BaseResultDTO modifyYarnAppHeuristicResult(YarnAppHeuristicResultDO yarnAppHeuristicResult);
	
	/**
	 * 完全修改，对于空的字段，也修改
	 */
	public BaseResultDTO modifyYarnAppHeuristicResultCompletely(YarnAppHeuristicResultDO yarnAppHeuristicResult);
	
	/**
	 * 逻辑删除
	 */
	public BaseResultDTO removeYarnAppHeuristicResult(YarnAppHeuristicResultDO yarnAppHeuristicResult);
	
}