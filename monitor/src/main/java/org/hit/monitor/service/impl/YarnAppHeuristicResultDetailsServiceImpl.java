package org.hit.monitor.service.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hit.monitor.bo.QueryYarnAppHeuristicResultDetailsBO;
import org.hit.monitor.common.BaseResultDTO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.dao.YarnAppHeuristicResultDetailsDao;
import org.hit.monitor.model.YarnAppHeuristicResultDetailsDO;
import org.hit.monitor.service.YarnAppHeuristicResultDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class YarnAppHeuristicResultDetailsServiceImpl implements YarnAppHeuristicResultDetailsService {

	private Logger log = Logger.getLogger(this.getClass());

	@Autowired
	private YarnAppHeuristicResultDetailsDao yarnAppHeuristicResultDetailsDao;

	@Override
	public ResultDTO<YarnAppHeuristicResultDetailsDO> queryYarnAppHeuristicResultDetailsById(Long name) {
	
		ResultDTO<YarnAppHeuristicResultDetailsDO> result = new ResultDTO<YarnAppHeuristicResultDetailsDO>();
		
		try {
			YarnAppHeuristicResultDetailsDO yarnAppHeuristicResultDetails = yarnAppHeuristicResultDetailsDao.selectYarnAppHeuristicResultDetailsById(name);
			result.setModule(yarnAppHeuristicResultDetails);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("根据主键查询失败");
			log.error("YarnAppHeuristicResultDetailsServiceImpl queryYarnAppHeuristicResultDetailsById error", e);
		}
		return result;
	}
	
	@Override
	public ResultDTO<YarnAppHeuristicResultDetailsDO> queryOne(QueryYarnAppHeuristicResultDetailsBO query) {
	
		ResultDTO<YarnAppHeuristicResultDetailsDO> result = new ResultDTO<YarnAppHeuristicResultDetailsDO>();
		
		try {
			YarnAppHeuristicResultDetailsDO yarnAppHeuristicResultDetails = yarnAppHeuristicResultDetailsDao.selectOne(query);
			result.setModule(yarnAppHeuristicResultDetails);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("查询失败");
			log.error("YarnAppHeuristicResultDetailsServiceImpl queryYarnAppHeuristicResultDetailsById error", e);
		}
		return result;
	}

	@Override
	public BatchResultDTO<YarnAppHeuristicResultDetailsDO> queryYarnAppHeuristicResultDetailsList(QueryYarnAppHeuristicResultDetailsBO query) {
	
		BatchResultDTO<YarnAppHeuristicResultDetailsDO> result = new BatchResultDTO<YarnAppHeuristicResultDetailsDO>();
		
		try {
			List<YarnAppHeuristicResultDetailsDO> list = yarnAppHeuristicResultDetailsDao.selectYarnAppHeuristicResultDetailsList(query);
			result.setModule(list);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("查询列表失败");
			log.error("YarnAppHeuristicResultDetailsServiceImpl queryYarnAppHeuristicResultDetailsList error", e);
		}
		return result;
	}
	
	@Override
	public BatchResultDTO<YarnAppHeuristicResultDetailsDO> queryYarnAppHeuristicResultDetailsPage(QueryYarnAppHeuristicResultDetailsBO query) {
	
		BatchResultDTO<YarnAppHeuristicResultDetailsDO> result = new BatchResultDTO<YarnAppHeuristicResultDetailsDO>();
		
		try {
			int count = yarnAppHeuristicResultDetailsDao.selectYarnAppHeuristicResultDetailsCount(query);
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
			
			List<YarnAppHeuristicResultDetailsDO> list = yarnAppHeuristicResultDetailsDao.selectYarnAppHeuristicResultDetailsList(query);
			result.setModule(list);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("分页查询失败");
			log.error("YarnAppHeuristicResultDetailsServiceImpl queryYarnAppHeuristicResultDetailsPage error", e);
		}
		return result;
	}

	@Override
	public BaseResultDTO createYarnAppHeuristicResultDetails(YarnAppHeuristicResultDetailsDO yarnAppHeuristicResultDetails) {
	
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			yarnAppHeuristicResultDetailsDao.insertYarnAppHeuristicResultDetails(yarnAppHeuristicResultDetails);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("添加失败");
			log.error("YarnAppHeuristicResultDetailsServiceImpl createYarnAppHeuristicResultDetails error", e);
		}
		return result;
	}

	@Override
	public BaseResultDTO modifyYarnAppHeuristicResultDetails(YarnAppHeuristicResultDetailsDO yarnAppHeuristicResultDetails) {
	
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			//只修改设定值得字段，属于选择性修改
			yarnAppHeuristicResultDetailsDao.updateYarnAppHeuristicResultDetailsSelective(yarnAppHeuristicResultDetails);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("修改失败");
			log.error("YarnAppHeuristicResultDetailsServiceImpl modifyYarnAppHeuristicResultDetails error", e);
		}
		return result;
	}

	@Override
	public BaseResultDTO modifyYarnAppHeuristicResultDetailsCompletely(YarnAppHeuristicResultDetailsDO yarnAppHeuristicResultDetails) {
	
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			//不断字段是否非空，都进行修改，属于完全修改
			yarnAppHeuristicResultDetailsDao.updateYarnAppHeuristicResultDetails(yarnAppHeuristicResultDetails);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("修改失败");
			log.error("YarnAppHeuristicResultDetailsServiceImpl modifyYarnAppHeuristicResultDetailsCompletely error", e);
		}
		return result;
	}

	@Override
	public BaseResultDTO removeYarnAppHeuristicResultDetails(YarnAppHeuristicResultDetailsDO yarnAppHeuristicResultDetails) {
	
		BaseResultDTO result = new BaseResultDTO();
		
		try {
			yarnAppHeuristicResultDetailsDao.deleteYarnAppHeuristicResultDetails(yarnAppHeuristicResultDetails);
			result.setSuccess(true);
		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("删除失败");
			log.error("YarnAppHeuristicResultDetailsServiceImpl removeYarnAppHeuristicResultDetails error", e);
		}
		return result;
	}

}