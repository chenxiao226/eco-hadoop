/**
 * Created by gaoyu on 2018/7/28
 * 
 */

(function () {
	

  
    // 获取用户数 缓冲池总页数
    function fetchdbName_Times() { 
    	$.ajax({
			type: "POST",
			url: monitorWebBaseURL + "Neo4j/kernel",
			dataType: "json",
			success: function (data) {
				if (data.success) {

					var dbName = data.dataObject.dbName;
					var KernelStartTime = data.dataObject.KernelStartTime;
					var StoreCreationDate = data.dataObject.StoreCreationDate;
					
					
					
					//更新页面指标信息								
					/*$("#dbName").html(dbName);*/
					//alert(dbName);				
					var newDate = new Date();
					newDate.setTime(KernelStartTime);
					
					//$("#KernelStartTime").html(newDate.toLocaleString());
					
					if (!isNaN(StoreCreationDate) && StoreCreationDate !== 0) {
						newDate.setTime(StoreCreationDate);
						//$("#StoreCreationDate").html(newDate.toLocaleString());
					}
				}
			}
		});
    }
    function fetchPagecache(){
    	$.ajax({
			type: "POST",
			url: monitorWebBaseURL + "Neo4j/pagecache",
			data: {},
			dataType: "json",
			success: function (data) {
				if (data != null) {
					$("#BytesWritten").html(data.dataObject.BytesWritten);
					$("#FileUnmappings").html(data.dataObject.FileUnmappings);
					$("#head_FileUnmappings").html(data.dataObject.FileUnmappings);					
					$("#Evictions").html(data.dataObject.Evictions);
					$("#BytesRead").html(data.dataObject.BytesRead);
					$("#FileMappings").html(data.dataObject.FileMappings);					
				}
			}
		});
    }
 // 定时刷新上述数据
	function timeTask() {
		fetchdbName_Times();
		fetchPagecache();
	}

	$(function () {
		// 获取一些一段时间内基本不会改变的指标
		fetchdbName_Times();
		fetchPagecache();
		setInterval(timeTask, 8000);
	});
	
	
/*	$(function () {
		$.ajax({
			type: "POST",
			url: monitorWebBaseURL + "Neo4j/pagecache",
			data: {},
			dataType: "json",
			success: function (data) {
				if (data != null) {
					$("#BytesWritten").html(data.dataObject.BytesWritten);
					$("#FileUnmappings").html(data.dataObject.FileUnmappings);
					$("#Evictions").html(data.dataObject.Evictions);
					$("#BytesRead").html(data.dataObject.BytesRead);
					$("#FileMappings").html(data.dataObject.FileMappings);					
				}
			}
		});
		
	});*/
    
})();

