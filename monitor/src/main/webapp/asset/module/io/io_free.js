/**
 * Created by likeugo on 2017/7/25
 * 
 */

(function () {

    var loadChart = null;
    var loadData = [];
    
    function fetchTotal_Free_DataInterval() { 
    	$.ajax({
			type: "POST",
			url: monitorWebBaseURL + "io/fetchDiskTotalAndFree",
			dataType: "json",
			success: function (data) {
				if (data.success) {
					var diskTotal = data.dataObject.diskTotal.sum;
					var diskFree = data.dataObject.diskFree.sum;
                    var diskUsed = diskTotal-diskFree;
					// 更新页面指标信息
					if (!isNaN(diskTotal) && diskTotal !== 0) {
						$("#diskTotal").html(getTotal(diskTotal));
					}
					if (!isNaN(diskFree) && diskFree !== 0) {
						$("#diskUsed").html(getTotal(diskUsed));
						$("#diskused-progress-bar").css("width", 100 * (diskUsed / diskTotal)+ "%");
						$("#diskused-progress-description").html((100*diskUsed / diskTotal).toFixed(2));
						
					}
				}
			}
		});
    }

 // 定时刷新上述数据
	function timeTask() {
		fetchTotal_Free_DataInterval();
	}

	$(function () {
		// 获取一些一段时间内基本不会改变的指标
		fetchTotal_Free_DataInterval();
		setInterval(timeTask, 8000);
	});
	
	function getTotal(diskTotal){
		// diskTotal是以G
		if(diskTotal>1024){
			return (diskTotal/1024).toFixed(2)+"<small>TB</small>";
		}else{
			return diskTotal.toFixed(2)+"<small>GB</small>";
		}
	}
    
})();

