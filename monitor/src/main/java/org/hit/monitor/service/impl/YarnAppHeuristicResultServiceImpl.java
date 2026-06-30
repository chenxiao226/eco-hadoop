package org.hit.monitor.service.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hit.monitor.bo.QueryYarnAppHeuristicResultBO;
import org.hit.monitor.common.BaseResultDTO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.dao.YarnAppHeuristicResultDao;
import org.hit.monitor.model.YarnAppHeuristicResultDO;
import org.hit.monitor.service.YarnAppHeuristicResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class YarnAppHeuristicResultServiceImpl implements YarnAppHeuristicResultService {

	private Logger log = Logger.getLogger(this.getClass());

	@Autowired
	private YarnAppHeuristicResultDao yarnAppHeuristicResultDao;

	@Override
	public ResultDTO<YarnAppHeuristicResultDO> queryYarnAppHeuristicResultById(Long id) {
	
		ResultDTO<YarnAppHeuristicResultDO> result = new ResultDTO<YarnAppHeuristicResultDO>();
		
		try {
			YarnAppHeuristicResultDO yarnAppHeuristicResult = yarnAppHeuristicResultDao.selectYarnAppHeuristicResultById(id);
			result.setModule(yarnAppHeuristicResult);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("根据主键查询失败");
			log.error("YarnAppHeuristicResultServiceImpl queryYarnAppHeuristicResultById error", e);
		}
		return result;
	}
	
	@Override
	public ResultDTO<YarnAppHeuristicResultDO> queryOne(QueryYarnAppHeuristicResultBO query) {
	
		ResultDTO<YarnAppHeuristicResultDO> result = new ResultDTO<YarnAppHeuristicResultDO>();
		
		try {
			YarnAppHeuristicResultDO yarnAppHeuristicResult = yarnAppHeuristicResultDao.selectOne(query);
			result.setModule(yarnAppHeuristicResult);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("查询失败");
			log.error("YarnAppHeuristicResultServiceImpl queryYarnAppHeuristicResultById error", e);
		}
		return result;
	}

	@Override
	public BatchResultDTO<YarnAppHeuristicResultDO> queryYarnAppHeuristicResultList(QueryYarnAppHeuristicResultBO query) {
	
		BatchResultDTO<YarnAppHeuristicResultDO> result = new BatchResultDTO<YarnAppHeuristicResultDO>();
		
		try {
			List<YarnAppHeuristicResultDO> list = yarnAppHeuristicResultDao.selectYarnAppHeuristicResultList(query);
			result.setModule(list);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("查询列表失败");
			log.error("YarnAppHeuristicResultServiceImpl queryYarnAppHeuristicResultList error", e);
		}
		return result;
	}
	
	@Override
	public BatchResultDTO<YarnAppHeuristicResultDO> queryYarnAppHeuristicResultPage(QueryYarnAppHeuristicResultBO query) {
	
		BatchResultDTO<YarnAppHeuristicResultDO> result = new BatchResultDTO<YarnAppHeuristicResultDO>();
		
		try {
			int count = yarnAppHeuristicResultDao.selectYarnAppHeuristicResultCount(query);
			query.setRecord(count);

			// 没数据
			if (count < 1) {
				result.setSuccess(true);
				return result;
			}
			// 查询页面超过最大页码
			if (query.getPageNo() > query.getTotalPages()) {
				result.setSuccess(true);
				return result;
			}
			
			List<YarnAppHeuristicResultDO> list = yarnAppHeuristicResultDao.selectYarnAppHeuristicResultList(query);
			result.setModule(list);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("分页查询失败");
			log.error("YarnAppHeuristicResultServiceImpl queryYarnAppHeuristicResultPage error", e);
		}
		return result;
	}

	@Override
	public BaseResultDTO createYarnAppHeuristicResult(YarnAppHeuristicResultDO yarnAppHeuristicResult) {
	
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			yarnAppHeuristicResultDao.insertYarnAppHeuristicResult(yarnAppHeuristicResult);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("添加失败");
			log.error("YarnAppHeuristicResultServiceImpl createYarnAppHeuristicResult error", e);
		}
		return result;
	}

	@Override
	public BaseResultDTO modifyYarnAppHeuristicResult(YarnAppHeuristicResultDO yarnAppHeuristicResult) {
	
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			//只修改设定值得字段，属于选择性修改
			yarnAppHeuristicResultDao.updateYarnAppHeuristicResultSelective(yarnAppHeuristicResult);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("修改失败");
			log.error("YarnAppHeuristicResultServiceImpl modifyYarnAppHeuristicResult error", e);
		}
		return result;
	}

	@Override
	public BaseResultDTO modifyYarnAppHeuristicResultCompletely(YarnAppHeuristicResultDO yarnAppHeuristicResult) {
	
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			//不断字段是否非空，都进行修改，属于完全修改
			yarnAppHeuristicResultDao.updateYarnAppHeuristicResult(yarnAppHeuristicResult);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("修改失败");
			log.error("YarnAppHeuristicResultServiceImpl modifyYarnAppHeuristicResultCompletely error", e);
		}
		return result;
	}

	@Override
	public BaseResultDTO removeYarnAppHeuristicResult(YarnAppHeuristicResultDO yarnAppHeuristicResult) {
	
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			yarnAppHeuristicResultDao.deleteYarnAppHeuristicResult(yarnAppHeuristicResult);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("删除失败");
			log.error("YarnAppHeuristicResultServiceImpl removeYarnAppHeuristicResult error", e);
		}
		return result;
	}

}