package org.hit.monitor.controller;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 接受故障预警子系统的报警信息
 */
@Controller
@RequestMapping("/bug")
public class BugController extends BaseController {

	private Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * 获取节点列表
	 */
	@ResponseBody
	@RequestMapping("/fetchBugList")
	public String fetchBugList(HttpServletRequest request) {
		String result = "[{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"}]";

		return result;
	}
	
	@ResponseBody
	@RequestMapping("/fetchruleList")
	public String fetchruleList(HttpServletRequest request) {
		String result = "[{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38,666\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"},{\"software\": \"net\",\"content\": \"2017-07-24 15:19:38\",\"component\": \"datanode\",\"machine\": \"master\",\"user\": \"zhanggr\",\"occurDate\": \"spark-shll\",\"faultType\": \"IO\"}]";

		return result;
	}
	
	
	
	
	
	
}
