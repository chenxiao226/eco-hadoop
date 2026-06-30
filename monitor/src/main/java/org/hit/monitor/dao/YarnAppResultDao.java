package org.hit.monitor.dao;

import java.util.List;

import org.hit.monitor.bo.QueryYarnAppResultBO;
import org.hit.monitor.model.YarnAppResultDO;

public interface YarnAppResultDao {

	/** 根据主键查询 **/
	public YarnAppResultDO selectYarnAppResultById(Long id);
	
	/** 根据主键查询是否存在 **/
	public int selectYarnAppResultCountById(Long id);

	/** 查询一个，必须传入能唯一确定一个的参数 **/
	public YarnAppResultDO selectOne(QueryYarnAppResultBO query);
	
	/** 查询列表，一次最多查出1000条 **/
	public List<YarnAppResultDO> selectYarnAppResultList(QueryYarnAppResultBO query);
	
	/** 分页查找 **/
	public List<YarnAppResultDO> selectYarnAppResultPage(QueryYarnAppResultBO query);
	
	/** 分页计数 **/
	public int selectYarnAppResultCount(QueryYarnAppResultBO query);

	/** 添加 **/
	public int insertYarnAppResult(YarnAppResultDO yarnAppResult);

	/** 完全修改 **/
	public int updateYarnAppResult(YarnAppResultDO yarnAppResult);

	/** 选择性修改 **/
	public int updateYarnAppResultSelective(YarnAppResultDO yarnAppResult);

	/** 删除 **/
	public int deleteYarnAppResult(YarnAppResultDO yarnAppResult);
	
	/**修正主键查询**/
	public YarnAppResultDO selectYarnAppResultByAppId(String id);
	
	/**获得最后的分析时间**/
	public YarnAppResultDO getLastYarnAppResultResult();
}