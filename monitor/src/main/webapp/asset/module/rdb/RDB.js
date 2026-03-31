/**
 * Created by gaoyu on 2018/8/5
 * 
 */

(function () {

    // 获取数据库名、开始时间信息
    function fetchCPUNum_Speed_Ide_DataInterval() { 
    	$.ajax({
			type: "POST",
			url: monitorWebBaseURL + "RDB/fetchrRdb_User_Buffer",
			dataType: "json",
			success: function (data) {
				if (data.success) {
					var userNum = data.dataObject.userNum;
					var buffer = data.dataObject.Innodb_buffer_pool_pages_total;
					//更新页面指标信息
					if (!isNaN(buffer) && buffer !== 0) {
						$("#buffer").html(parseInt(buffer));
					}
					
					if (!isNaN(userNum) && userNum !== 0) {
						$("#userNum").html(parseInt(userNum));
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
		setInterval(timeTask, 8000);
	});
	
	
	$(function () {

		table = $('#rdbProcessTable').DataTable({
			"paging": true,
			"lengthChange": true,
			"aLengthMenu": [10, 25, 50],
			"searching": true,
			"ordering": true,
			"order": [[2, "asc"]], //默认按照主机名升序排序
			"info": true,
			"autoWidth": false,
			"language": {
				'emptyTable': '没有数据',
				'loadingRecords': '加载中...',
				'processing': '查询中...',
				'search': '搜索:',
				'lengthMenu': '每页 _MENU_ 条记录',
				'zeroRecords': '没有数据',
				'paginate': {
					'next': '下一页',
					'previous': '上一页'
				},
				'info': '第 _PAGE_ 页 / 共 _PAGES_ 页',
				'infoEmpty': '没有数据',
				'infoFiltered': '(从 _MAX_ 条记录中筛选)'
			},
			ajax: {
				url: monitorWebBaseURL + "RDB/ProcessList",
				dataType: "json",
				dataSrc: function (data) {
					var itemList = [];
					$.each(data.dataObject,function (i,item) {						
						itemList.push({
							Id:item.id,
//							disk:(item.resources.disk / 1024).toFixed(1) +"GB",
							User:item.user,
							Host:item.host,
//							gpus:item.resources.gpus,
							Command:item.command,
							Time:item.time,
							State:item.state==""?"stopped":item.state,							
						});
					});
					return itemList;
				}
			},
			columns: [
				{data: "Id"},
				{data: "User"},
				{data: "Host"},
				{data: "Command"},				
				{data: "Time"},				
				{data: "State"},
				
			],			
		});
		
	});
    
})();

