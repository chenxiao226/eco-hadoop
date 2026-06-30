(function () {
	
	var table = null;
	
	$(function () {
		table = $('#processListTable').DataTable({
			"paging": true,
			"lengthChange": true,
			"aLengthMenu": [5, 10, 20],
			"searching": false,
			"ordering": true,
			"order": [[0, "desc"]], //按照提交时间降序
			"info": true,
			"autoWidth": false,
			"language": {
				'emptyTable': '没有进程相关的数据',
				'loadingRecords': '加载中...',
				'processing': '查询中...',
				'search': '搜索:',
				'lengthMenu': '每页 _MENU_ 条记录',
				//'zeroRecords': '没有正在运行的应用',
				'paginate': {
					'next': '下一页',
					'previous': '上一页'
				},
				'info': '第 _PAGE_ 页 / 共 _PAGES_ 页',
				'infoEmpty': '没有数据',
				'infoFiltered': '(从 _MAX_ 条记录中筛选)'
			},
			ajax: {
				url: monitorWebBaseURL + "alert/process/fetch",    
				dataType: "json",
				dataSrc : function(data) { // 查询只需要这个dataSrc
					if (data !== null)
						return data.result;
					else
						return [];
				}
			},
			columns: [  //使用对象数组告诉datatables每列对应的属性
				
				{
					data : null,				
					"defaultContent" : "<input name=\"Select\" type=\"checkbox\" value=\"\" />"
				},
			    {
				       data: "machine"
				},
				{
						data: "process"
				},
				{
				        data: "status",
				        	"render" : function(data, type, row) {
								if (data == 0) {
									return "Stop";
								} else {
									return "Start";
								}
							}
				},
				
			]
		});
		
		$("#chooseIpBtn")
		.click(
				function() {
					//  主机ip
					var hostIp = getName($("#hostip").val());
					
					$
							.ajax({
								type : "POST",
								url : monitorWebBaseURL + "alert/process/fetch",
								data : {
									'machine':hostIp,
								},
								dataType : "json",
								success : function(data) {
									if (data.success) {
										var result = data.result;
										table.clear();
										table.rows.add(data.result);
										table.draw();
									}
								}
							});
				});
		$("#queryBtn")
		.click(
				function() {
					//  主机ip，进程名称
					var hostIp = getName($("#hostIP").val());
					var processname = getName($("#processName").val());
					
					$
							.ajax({
								type : "POST",
								url : monitorWebBaseURL + "alert/process/fetch",
								data : {
									'machine':hostIp,
									'process':processname
								},
								dataType : "json",
								success : function(data) {
									if (data.success) {
										var result = data.result;
										table.clear();
										table.rows.add(data.result);
										table.draw();
									}
								}
							});
				});
		
		$("#chooseBtn")
		.click(
				function() {
					

			    str = $(":checkbox:checked").map(function() {

			          // return $(this).parent().siblings('td').text();  // 根据checkbox定位到后面的td，然后使用text()函数获取内容			    	   
			    	return $(this).parent().siblings().eq(1).text(); 
			           
			        }).get().join("\t"); // 获取内容数组并拼接为字符串

			      var hostIp = getName($("#hostip").val());
			     if(hostIp != null){
			      str1=hostIp +"#"+str;				
			      alert(str1); 
			    	$
					.ajax({
						type : "POST",
						url : monitorWebBaseURL + "alert/Process/transfer",
						data :
							{
							'message' : str1						
							},
						dataType : "json",
						success : function(data) {
							if (data.success) {
								var result = data.result;
								table.clear();
								table.rows.add(data.result);
								table.draw();
							}
						}
					});
			      }
			      else 
			    	  alert("请输入主机IP");

			    }	
//				//选择的行
//				
//				//数据抽取
//				
//				//AJAX传数据
		);
		$("#refreshBtn")
		.click(
				function() {
					
              str="process!!!2&";
				
			      alert(str); 
			    	$
					.ajax({
						type : "POST",
						url : monitorWebBaseURL + "alert/Process/refresh",
						data :
							{
							'refmessage' : str						
							},
						dataType : "json",
						success : function(data) {
							if (data.success) {
								var result = data.result;
								table.clear();
								table.rows.add(data.result);
								table.draw();
							}
						}
					});

			    }
		);
		
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
	function getName(name) {
		if (name != null && name.length != 0) {
			return name;
		} else {
			return null;
		}
	}
})();