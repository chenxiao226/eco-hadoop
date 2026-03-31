package org.hit.monitor.controller;

import org.hit.monitor.bo.QueryMetricsBO;
import org.hit.monitor.common.BatchResultDTO;
import org.hit.monitor.common.Metrics;
import org.hit.monitor.model.MetricsDataDO;
import org.hit.monitor.service.MetricsService;
import org.hit.monitor.service.TsfileSerrvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/parameterhdfs")
public class ParameterHDFSController extends BaseController {

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private TsfileSerrvice tsfileSerrvice;

    private final MetricsService metricsService;

    @Autowired
    public ParameterHDFSController(MetricsService metricsService) {
        this.metricsService = metricsService;
    }

    /**
     * 获取内存监控中的基本数据(总内存和总交换空间)
     */
    @ResponseBody
    @RequestMapping("/memory")
    public String fetchMemoryData(HttpServletRequest request) {

        try {
            Integer limit = getIntegerParameterFromRequest(request, "limit", 1);
            Long start = getLongParameterFromRequest(request, "start", null);

            QueryMetricsBO filter = new QueryMetricsBO();
            filter.setDesc(true);
            filter.setLimit(limit);
            filter.setStart(start);

            BatchResultDTO<MetricsDataDO> nonHeapMemoryUsageInit = metricsService.fetchData(Metrics.TSFILE.NON_HEAP_MEMORY_USAGE_INIT, filter);
            BatchResultDTO<MetricsDataDO> nonHeapMemoryUsageUsed = metricsService.fetchData(Metrics.TSFILE.NON_HEAP_MEMORY_USAGE_USED, filter);
            BatchResultDTO<MetricsDataDO> nonHeapMemoryUsageMax = metricsService.fetchData(Metrics.TSFILE.NON_HEAP_MEMORY_USAGE_MAX, filter);
            BatchResultDTO<MetricsDataDO> nonHeapMemoryUsageCommitted = metricsService.fetchData(Metrics.TSFILE.NON_HEAP_MEMORY_USAGE_COMMITTED, filter);
            BatchResultDTO<MetricsDataDO> heapMemoryUsageUsed = metricsService.fetchData(Metrics.TSFILE.HEAP_MEMORY_USAGE_USED, filter);
            BatchResultDTO<MetricsDataDO> heapMemoryUsageMax = metricsService.fetchData(Metrics.TSFILE.HEAP_MEMORY_USAGE_MAX, filter);
            BatchResultDTO<MetricsDataDO> heapMemoryUsageCommitted = metricsService.fetchData(Metrics.TSFILE.HEAP_MEMORY_USAGE_COMMITTED, filter);
            BatchResultDTO<MetricsDataDO> heapMemoryUsageInit = metricsService.fetchData(Metrics.TSFILE.HEAP_MEMORY_USAGE_INIT, filter);
            BatchResultDTO<MetricsDataDO> freePhysicalMemorySize = metricsService.fetchData(Metrics.TSFILE.FREE_PHYSICAL_MEMORY_SIZE, filter);

            Map<String, Object> result = new HashMap<String, Object>();
            result.put("nonHeapMemoryUsageInit", nonHeapMemoryUsageInit);
            result.put("heapMemoryUsageUsed", heapMemoryUsageUsed);
            result.put("heapMemoryUsageMax", heapMemoryUsageMax);
            result.put("nonHeapMemoryUsageUsed", nonHeapMemoryUsageUsed);
            result.put("heapMemoryUsageCommitted", heapMemoryUsageCommitted);
            result.put("nonHeapMemoryUsageMax", nonHeapMemoryUsageMax);
            result.put("nonHeapMemoryUsageCommitted", nonHeapMemoryUsageCommitted);
            result.put("heapMemoryUsageInit", heapMemoryUsageInit);
            result.put("freePhysicalMemorySize", freePhysicalMemorySize);

            return responseControllerResultSuccess(result);
        } catch (Exception e) {
            log.error("获取TsFile JVM内存数据出错", e);
            return responseControllerResultError("获取TsFile JVM内存数据出错");
        }
    }

    /**
     * 获取CPU使用率数据
     */
    @ResponseBody
    @RequestMapping("/cpu")
    public String fetchLoadData(HttpServletRequest request) {
        try {
            Integer limit = getIntegerParameterFromRequest(request, "limit", 1);
            Long cpuStart = getLongParameterFromRequest(request, "start", null);

            QueryMetricsBO filter = new QueryMetricsBO();
            filter.setDesc(true);
            filter.setLimit(limit);
            filter.setStart(cpuStart);

            BatchResultDTO<MetricsDataDO> result = metricsService.fetchData(Metrics.TSFILE.CPU_RATIO, filter);
            if (result.isSuccess()) {
                return responseControllerResultSuccess(result.getModule());
            } else {
                return responseControllerResultError(result.getErrorDetail());
            }
        } catch (Exception e) {
            log.error("获取TsFile CPU使用率数据出错", e);
            return responseControllerResultError("获取TsFile CPU使用率数据出错");
        }
    }

    /**
     * 获取磁盘数据
     */
    @ResponseBody
    @RequestMapping("/disk")
    public String fetchDiskData(HttpServletRequest request) {
        try {
            Integer limit = getIntegerParameterFromRequest(request, "limit", 1);
            Long start = getLongParameterFromRequest(request, "start", null);

            QueryMetricsBO filter = new QueryMetricsBO();
            filter.setDesc(true);
            filter.setLimit(limit);
            filter.setStart(start);

            BatchResultDTO<MetricsDataDO> result = metricsService.fetchData(Metrics.TSFILE.DATA_SIZE_IN_BYTE, filter);

            if (result.isSuccess()) {
                return responseControllerResultSuccess(result.getModule());
            } else {
                return responseControllerResultError(result.getErrorDetail());
            }
        } catch (Exception e) {
            log.error("获取TsFile磁盘数据出错", e);
            return responseControllerResultError("获取TsFile磁盘数据出错");
        }
    }

    /*写入点数*/
    @ResponseBody
    @RequestMapping("/totalponits")
    public String fetchponits() throws Exception{

        Map<String, Object> result = new HashMap<String, Object>();
        result.put("TOTAL_POINTS_SUCCESS", tsfileSerrvice.fetchTotalPoints());
        result.put("TOTAL_POINTS_FAIL", tsfileSerrvice.fetchTotalPointsFAIL());
        result.put("REQ_SUCCESS", tsfileSerrvice.fetchREQSuccess());
        result.put("REQ_FAIL", tsfileSerrvice.fetchREQFail());
        return responseControllerResultSuccess(result);
    }

}
