/**
 * Created by gaoyu on 2018/8/8
 * 
 */

(function () {
	

  
    // 获取table cql指标数
    
    function fetchTableCQL(){
    	$.ajax({
			type: "POST",
			url: monitorWebBaseURL + "cassandra/Tabe_CQL",
			data: {},
			dataType: "json",
			success: function (data) {
				if (data != null) {
					$("#MemtableColumnsCount").html("<span class='badge bg-green'>"+data.dataObject.MemtableColumnsCount+"</span>");
					$("#MemtableLiveDataSize").html("<span class='badge bg-green'>"+data.dataObject.MemtableLiveDataSize+"</span>");
					$("#Exceptions").html("<span class='badge bg-red'>"+data.dataObject.Exceptions+"</span>");
					/*$("#PreparedStatementsEvicted").html(data.dataObject.PreparedStatementsEvicted);*/
					$("#RegularStatementsExecuted").html(data.dataObject.RegularStatementsExecuted);
					$("#PreparedStatementsExecuted").html(data.dataObject.PreparedStatementsExecuted);
				}
			}
		});
    }
 // 定时刷新上述数据
	function timeTask() {
		fetchTableCQL();
	
	}

	$(function () {
		// 获取一些一段时间内基本不会改变的指标
		fetchTableCQL();
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

