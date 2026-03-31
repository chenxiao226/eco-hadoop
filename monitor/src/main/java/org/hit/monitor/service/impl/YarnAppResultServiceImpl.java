package org.hit.monitor.service.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hit.monitor.bo.QueryYarnAppResultBO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.dao.YarnAppHeuristicResultDao;
import org.hit.monitor.dao.YarnAppHeuristicResultDetailsDao;
import org.hit.monitor.dao.YarnAppResultDao;
import org.hit.monitor.model.YarnAppHeuristicResultDO;
import org.hit.monitor.model.YarnAppHeuristicResultDetailsDO;
import org.hit.monitor.model.YarnAppResultDO;
import org.hit.monitor.service.YarnAppResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class YarnAppResultServiceImpl implements YarnAppResultService {

	private Logger log = Logger.getLogger(this.getClass());

	// @Autowired
	// private AppResultDAO appResultMapper;

	@Autowired
	private YarnAppResultDao yarnAppResultDao;

	@Autowired
	private YarnAppHeuristicResultDao yarnAppHeuristicResultDao;

	@Autowired
	private YarnAppHeuristicResultDetailsDao yarnAppHeuristicResultDetailsDao;

	// 分页查找jobhistory数据
	@Override
	public BatchResultDTO<YarnAppResultDO> queryAppResultByPage(QueryYarnAppResultBO query) {
		BatchResultDTO<YarnAppResultDO> result = new BatchResultDTO<YarnAppResultDO>();

		try {

			int count = yarnAppResultDao.selectYarnAppResultCount(query);// 获得totalRecord
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

			List<YarnAppResultDO> list = yarnAppResultDao.selectYarnAppResultPage(query);
			result.setModule(list);
			result.setSuccess(true);

		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("分页查询失败");
			log.error("YarnAppResultServiceImpl queryAppResultByPage error", e);
		}

		return result;
	}

	// 根据appid，返回一个历史任务的详细信息
	@Override
	public ResultDTO<YarnAppResultDO> queryAppResultDetailsById(QueryYarnAppResultBO query) {

		// 先选择一个appJob
		ResultDTO<YarnAppResultDO> result = new ResultDTO<YarnAppResultDO>();

		try {
			YarnAppResultDO YarnAppResultDO = yarnAppResultDao.selectOne(query);

			// Heuristic
			List<YarnAppHeuristicResultDO> yarnAppHeuristicResultDOList = yarnAppHeuristicResultDao
					.selectListByAppId(query.getId());

			YarnAppResultDO.setYarnAppHeuristicResultDOList(yarnAppHeuristicResultDOList);
			// Heuristic详细信息
			for (YarnAppHeuristicResultDO appHeuristicResult : yarnAppHeuristicResultDOList) {

				List<YarnAppHeuristicResultDetailsDO> yarnAppHeuristicResultDetailsDOList = yarnAppHeuristicResultDetailsDao
						.selectListByAppHeuristicId(appHeuristicResult.getId());
				appHeuristicResult.setYarnAppHeuristicResultDetailsDOList(yarnAppHeuristicResultDetailsDOList);
			}

			result.setModule(YarnAppResultDO);
			result.setSuccess(true);

		} catch (Exception e) {
			result.setSuccess(false);
			result.setErrorDetail("查询失败");
			log.error("YarnAppResultServiceImpl queryAppResultDetailsById error", e);
		}
		return result;

	}

	@Override
	public void insertAppResultDetails(YarnAppResultDO yarnAppResultDO) {
		// TODO Auto-generated method stub
		// 如果任务分析过，就不插入记录
		try {
			YarnAppResultDO yarnAppResultDO2 = yarnAppResultDao.selectYarnAppResultByAppId(yarnAppResultDO.getId());
			if (yarnAppResultDO2 != null) {
				return;
			}
		} catch (Exception e) {
			log.error("YarnAppResultServiceImpl insertAppResultDetails insertYarnAppResult error", e);
		}
		try {
			// 先插入整体结果
			yarnAppResultDao.insertYarnAppResult(yarnAppResultDO);

		} catch (Exception e) {
			log.error("YarnAppResultServiceImpl insertAppResultDetails insertYarnAppResult error", e);
		}
		try {
			// 在插入启发式结果
			yarnAppHeuristicResultDao.insertBatch(yarnAppResultDO.getYarnAppHeuristicResultDOList());

		} catch (Exception e) {
			log.error("YarnAppResultServiceImpl insertAppResultDetails insertYarnAppHeuristicResultDao error", e);
		}
		try {
			// 在插入启发式详细结果
			for (YarnAppHeuristicResultDO yarnAppHeuristicResultDO : yarnAppResultDO
					.getYarnAppHeuristicResultDOList()) {
				yarnAppHeuristicResultDetailsDao.insertBatch(
						yarnAppHeuristicResultDO.getYarnAppHeuristicResultDetailsDOList(),
						yarnAppHeuristicResultDO.getId());
			}

		} catch (Exception e) {
			// TODO: handle exception
			log.error("YarnAppResultServiceImpl insertAppResultDetails insertYarnAppHeuristicResultDao error", e);
		}
	}

	@Override
	public Long getLastTime() {
		try {
			YarnAppResultDO yarnAppResultDO = yarnAppResultDao.getLastYarnAppResultResult();
			if (yarnAppResultDO != null) {
				return  yarnAppResultDO.getFinishTime();
			}else{
				return (long)0;
			}
		} catch (Exception e) {
			log.error("YarnAppResultServiceImpl insertAppResultDetails insertYarnAppResult error", e);
		}
		return (long)0;
	}
}
