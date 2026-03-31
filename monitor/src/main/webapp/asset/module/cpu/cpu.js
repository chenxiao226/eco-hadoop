/**
 * Created by likeugo on 2017/7/24
 * 
 */

(function () {

    var loadChart = null;
    var loadData = [];

    
    // 获取负载数据，CPU核数，速度，空闲
    function fetchCPUNum_Speed_Ide_DataInterval() {
    	$.ajax({
			type: "POST",
			url: monitorWebBaseURL + "cpu/fetchCPU_Num_Spped_Idle",
			dataType: "json",
			success: function (data) {
				if (data.success) {
        			var cpuNum = data.dataObject.cpuNum && data.dataObject.cpuNum.sum;
					var cpuIdle = data.dataObject.cpuIdle && data.dataObject.cpuIdle.sum;
					var idleNum = data.dataObject.cpuIdle && data.dataObject.cpuIdle.num;
					var cpuSpeed = data.dataObject.cpuSpeed && data.dataObject.cpuSpeed.sum;
					var speedNum = data.dataObject.cpuSpeed && data.dataObject.cpuSpeed.num;

					//更新页面指标信息
					if (!isNaN(cpuIdle) && idleNum !== 0) {
						$("#cpuIdle").html((100- cpuIdle / idleNum).toFixed(2)+'%');
					}
					if (!isNaN(cpuSpeed) && speedNum !== 0) {
						$("#cpuSpeed").html(((cpuSpeed)/1000).toFixed(1)+"MHZ");
					}
					if (!isNaN(cpuNum) && cpuNum !== 0) {
						$("#cpuNum").html(parseInt(cpuNum));
					}
				}
			}
		});
    }

 // 定时刷新上述数据
	function timeTask() {
		fetchCPUNum_Speed_Ide_DataInterval();
	}

	$(function () {
		// 获取一些一段时间内基本不会改变的指标
		fetchCPUNum_Speed_Ide_DataInterval();
		setInterval(timeTask, 3000);
	});
    
})();

