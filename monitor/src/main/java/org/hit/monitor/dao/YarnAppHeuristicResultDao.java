package org.hit.monitor.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.hit.monitor.bo.QueryYarnAppHeuristicResultBO;
import org.hit.monitor.model.YarnAppHeuristicResultDO;

public interface YarnAppHeuristicResultDao {

	/** 根据主键查询 **/
	public YarnAppHeuristicResultDO selectYarnAppHeuristicResultById(Long id);

	/** 根据主键查询是否存在 **/
	public int selectYarnAppHeuristicResultCountById(Long id);

	/** 查询一个，必须传入能唯一确定一个的参数 **/
	public YarnAppHeuristicResultDO selectOne(QueryYarnAppHeuristicResultBO query);

	/** 查询列表，一次最多查出1000条 **/
	public List<YarnAppHeuristicResultDO> selectYarnAppHeuristicResultList(QueryYarnAppHeuristicResultBO query);

	/** 分页查找 **/
	public List<YarnAppHeuristicResultDO> selectYarnAppHeuristicResultPage(QueryYarnAppHeuristicResultBO query);

	/** 分页计数 **/
	public int selectYarnAppHeuristicResultCount(QueryYarnAppHeuristicResultBO query);

	/** 添加 **/
	public int insertYarnAppHeuristicResult(YarnAppHeuristicResultDO yarnAppHeuristicResult);

	/** 完全修改 **/
	public int updateYarnAppHeuristicResult(YarnAppHeuristicResultDO yarnAppHeuristicResult);

	/** 选择性修改 **/
	public int updateYarnAppHeuristicResultSelective(YarnAppHeuristicResultDO yarnAppHeuristicResult);

	/** 删除 **/
	public int deleteYarnAppHeuristicResult(YarnAppHeuristicResultDO yarnAppHeuristicResult);

	// 批量插入
	public void insertBatch(@Param("list") List<YarnAppHeuristicResultDO> attachmentTables);

	// 根据applicationID获得List
	public List<YarnAppHeuristicResultDO> selectListByAppId(String appId);

}