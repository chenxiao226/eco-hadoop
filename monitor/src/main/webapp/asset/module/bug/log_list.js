(function () {
	var table = null;
	$(function () {
		table = $('#logListTable').DataTable({
			"paging": true,
			"lengthChange": true,
			"aLengthMenu": [5, 10, 20],
			"searching": false,
			"ordering": true,
			"order": [[0, "desc"]], //按照提交时间降序
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
				url: monitorWebBaseURL + "alert/log/fetch",
				dataType: "json",
				dataSrc: function (data) {  //查询只需要这个dataSrc
					if (data!== null)
						return data.result;
					else
						return [];
				}
			},
			columns: [
				{data: "occurTime",
					"render" : function(data, type, row) {
						return  formatDateTime(data);
					}
				},
				{data: "host"},
				{data: "software"
				},
				{data: "component",
				},
				{data: "faultType",
				},
				{data: null,
					"defaultContent": "<button type=\"button\" class=\"btn btn-primary btn-sm btn-flat\">确认</button>"
					},
				{data: null,
				"defaultContent": "<button type=\"button\" class=\"btn btn-primary btn-sm btn-flat bg-green\">详情</button>"
				},
				{data: "content",
			     visible:false
				}
			]
		});
		// 查询事件
		$("#queryBtn").click(
			function () {
				//  主机名称
				var host = $("#Name").val();
				// 时间
				var start = getDateTime($("#startDate").val());
				var end = getDateTime($("#endDate").val());
				$
					.ajax({
						type: "POST",
						url: monitorWebBaseURL + "alert/log/fetch",
						data: {
							'start': start,
							'end': end,
							'host': host
						},
						dataType: "json",
						success: function (data) {
							if (data.success) {
								var result = data.result;
								table.clear();
								table.rows.add(data.result);
								table.draw();
							}
						}
					});
			});
		 $('#logListTable tbody').on( 'click', 'button', function () {
		        var data = table.row($(this).parents('tr')).data();
		        var modal = $('#alertContentModal');
				modal.find(".modalContent").html(data["content"]);
				modal.modal({
					"backdrop": false
				});
		    } );
		 
		 
		 $('.form_datetime').datetimepicker({
		        // language: 'fr',
		        weekStart : 1,
		        todayBtn : 1,
		        autoclose : 1,
		        todayHighlight : 1,
		        startView : 2,
		        forceParse : 0,
		        showMeridian : 1,
		        minuteStep : 3,
		        language : 'zh-CN'
		    });
		    $('.form_date').datetimepicker({
		        language : 'fr',
		        weekStart : 1,
		        todayBtn : 1,
		        autoclose : 1,
		        todayHighlight : 1,
		        startView : 2,
		        minView : 2,
		        forceParse : 0
		    });
		    $('.form_time').datetimepicker({
		        language : 'fr',
		        weekStart : 1,
		        todayBtn : 1,
		        autoclose : 1,
		        todayHighlight : 1,
		        startView : 1,
		        minView : 0,
		        maxView : 1,
		        forceParse : 0
		    });

		    $(".form_datetime").datetimepicker({
		        format : "dd MM yyyy - hh:ii",
		        autoclose : true,
		        todayBtn : true,
		        startDate : new Date(),
		        minuteStep : 1
		    });
		    
		    $("#rule").click(function(){
				location.href = "/monitor/asset/module/bug/rule.html";
			});
			$("#log").click(function(){
				location.href = "/monitor/asset/module/bug/log.html";
			});
			$("#process").click(function(){
				location.href = "/monitor/asset/module/bug/process.html";
			});
			
		 
	});
	// 返回一个时间戳
	function getDateTime(dateTime) {
		if (dateTime != null && dateTime.length != 0) {
			var dd = new Date(dateTime).getTime();
			return dd;
		} else {
			return null;
		}
	}
	// 时间戳转换成yyyy-mm-dd hh:mm:ss
	function formatDateTime(inputTime) {
		var date = new Date(inputTime);
		var y = date.getFullYear();
		var m = date.getMonth() + 1;
		m = m < 10 ? ('0' + m) : m;
		var d = date.getDate();
		d = d < 10 ? ('0' + d) : d;
		var h = date.getHours();
		h = h < 10 ? ('0' + h) : h;
		var minute = date.getMinutes();
		var second = date.getSeconds();
		minute = minute < 10 ? ('0' + minute) : minute;
		second = second < 10 ? ('0' + second) : second;
		return y + '-' + m + '-' + d + ' ' + h + ':' + minute + ':' + second;
	}
})();