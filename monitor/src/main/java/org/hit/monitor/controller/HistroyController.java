package org.hit.monitor.controller;

import javax.servlet.http.HttpServletRequest;

import org.hit.monitor.bo.QueryYarnAppResultBO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.ResultDTO;
import org.hit.monitor.model.YarnAppResultDO;
import org.hit.monitor.service.YarnAppResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/history")
public class HistroyController extends BaseController {

	@Autowired
	private YarnAppResultService appResultService;

	@RequestMapping("/getHistoryListByPage")
	@ResponseBody
	public String getHistoryListByPage(Integer pageindex, Integer pageSize, Long start, Long end, String name,
			QueryYarnAppResultBO query) {

		// pageindex = 1
		// pageSize = 10
		// 创建一个
		// QueryAppResultBO query = new QueryAppResultBO();
		if (pageindex != null) {
			query.setPageNo(pageindex);
		}
		if (pageSize != null) {
			query.setPageSize(pageSize);
		}
		if (query.getName().length() == 0) {
			query.setName(null);
		}

		BatchResultDTO<YarnAppResultDO> result = appResultService.queryAppResultByPage(query);

		return responsePageSuccess(result.getModule(), query);
	}

	@RequestMapping("/getHistoryDetailsById")
	@ResponseBody
	public String getHistoryDetailsById(String AppId) {
		System.out.println("进入 getHistoryDetailsByAppId 方法");

		QueryYarnAppResultBO query = new QueryYarnAppResultBO();

		query.setId(AppId);
//		query.setId("application_1488786416942_0001");
		System.out.println("AppID 参数：" + AppId);
		ResultDTO<YarnAppResultDO> resultDTO = appResultService.queryAppResultDetailsById(query);
		System.out.println("查询结果：" + resultDTO);
		resultDTO.setSuccess(true);

		return responseJson(resultDTO);
	}

	@RequestMapping("/getHistoryDetailsByAppId")
	public ModelAndView getHistoryDetailsByAppId(String AppID,HttpServletRequest request) {

		QueryYarnAppResultBO query = new QueryYarnAppResultBO();

		query.setId(AppID);

		ResultDTO<YarnAppResultDO> resultDTO = appResultService.queryAppResultDetailsById(query);

		// resultDTO.setSuccess(true);

		ModelAndView modelAndView = new ModelAndView();
		modelAndView.addObject("resultDTO", resultDTO);
		modelAndView.addObject("AppId","app1");
		request.setAttribute("AppId","app1");
		modelAndView.setViewName("forward:/asset/module/history/historyjobDetail.html");
		return modelAndView;
	}

}
