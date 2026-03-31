package org.hit.monitor.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.hit.monitor.bo.QueryYarnAppHeuristicResultDetailsBO;
import org.hit.monitor.model.YarnAppHeuristicResultDO;
import org.hit.monitor.model.YarnAppHeuristicResultDetailsDO;

public interface YarnAppHeuristicResultDetailsDao {

	/** 根据主键查询 **/
	public YarnAppHeuristicResultDetailsDO selectYarnAppHeuristicResultDetailsById(Long name);
	
	/** 根据主键查询是否存在 **/
	public int selectYarnAppHeuristicResultDetailsCountById(Long name);

	/** 查询一个，必须传入能唯一确定一个的参数 **/
	public YarnAppHeuristicResultDetailsDO selectOne(QueryYarnAppHeuristicResultDetailsBO query);
	
	/** 查询列表，一次最多查出1000条 **/
	public List<YarnAppHeuristicResultDetailsDO> selectYarnAppHeuristicResultDetailsList(QueryYarnAppHeuristicResultDetailsBO query);
	
	/** 分页查找 **/
	public List<YarnAppHeuristicResultDetailsDO> selectYarnAppHeuristicResultDetailsPage(QueryYarnAppHeuristicResultDetailsBO query);
	
	/** 分页计数 **/
	public int selectYarnAppHeuristicResultDetailsCount(QueryYarnAppHeuristicResultDetailsBO query);

	/** 添加 **/
	public int insertYarnAppHeuristicResultDetails(YarnAppHeuristicResultDetailsDO yarnAppHeuristicResultDetails);

	/** 完全修改 **/
	public int updateYarnAppHeuristicResultDetails(YarnAppHeuristicResultDetailsDO yarnAppHeuristicResultDetails);

	/** 选择性修改 **/
	public int updateYarnAppHeuristicResultDetailsSelective(YarnAppHeuristicResultDetailsDO yarnAppHeuristicResultDetails);

	/** 删除 **/
	public int deleteYarnAppHeuristicResultDetails(YarnAppHeuristicResultDetailsDO yarnAppHeuristicResultDetails);
	
	/**根据AppHeuristicId查询List**/
	List<YarnAppHeuristicResultDetailsDO> selectListByAppHeuristicId(Integer Id);
	/**批量插入**/
	
	public void insertBatch(@Param(value = "list") List<YarnAppHeuristicResultDetailsDO> list,@Param("id") Integer id);
}