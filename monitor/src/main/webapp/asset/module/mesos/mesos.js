/**
 * Created by likeugo on 2017/7/28
 *
 */
(function () {

	var activeFramework = null;
	var marathonFrameworksTable = null;

	//从详情模板拷贝内容
	function generateDetail() {
		return $("#appDetailTemplate").html();
	}

	function fillInData(row) {
		var subRow = $(row.node()).next("tr");
		var cpu = row.data().cpus;
		var appId = row.data().name;
		subRow.find(".overlay").show();
		$.ajax({
			type: "POST",
			url: monitorWebBaseURL + "mesos/marathon/"+appId+"/tasks",
			data: {},
			dataType: "json",
			success: function (data) {
				if (data != null && data.tasks != null) {
					var html = "";
					$.each(data.tasks, function(i, item){
						var host = item.host;
						var port = item.ports.join(",");
						var instanceId = item.id;
						var state = item.state;
						html += '\
							<tr>\
								<td>'+host+'</td>\
								<td>'+port+'</td>\
								<td>'+instanceId+'</td>\
								<td>'+state+'</td>\
							</tr>';
					});
					subRow.find(".instanceList").html(html);
					subRow.find(".overlay").hide();
				}
			}
		});
	}

	// 获取mesos数据
	function fetchMesosInterval() {
		$.ajax({
			type: "POST",
			url: monitorWebBaseURL + "mesos/metrics/snapshot",
			dataType: "json",
			success: function (data) {
				if (data.success) {
					var jsonData = JSON.parse(data.dataObject);

					var cpus_total = jsonData["master\/cpus_total"];
					var cpus_used = jsonData["master\/cpus_used"];

					var mem_total = jsonData["master\/mem_total"];
					var mem_used = jsonData["master\/mem_used"];

					var disk_total = jsonData["master\/disk_total"];
					var disk_used = jsonData["master\/disk_used"];

					var gpus_total = jsonData["master\/gpus_total"];
					var gpus_used = jsonData["master\/gpus_used"];

					// 更新页面指标信息
					$("#cpuTotal_Used").html(cpus_used.toFixed(1) + "/" + cpus_total);
					$("#gpuTotal_Used").html(gpus_used + "/" + gpus_total);
					$("#memTotal_Used").html(getMem(mem_used) + "/" + getMem(mem_total));
					$("#diskTotal_Used").html(getMem(disk_used) + "/" + getMem(disk_total));
				}
			}
		});
	}

	// frameworks
	function initFrameworks() {
		//初始化Mesos
		activeFramework = $('#activeFrameworksTable').DataTable({
			"paging": true,
			"lengthChange": true,
			"aLengthMenu": [5, 10, 20],
			"searching": false,
			"ordering": true,
			"order": [[9, "desc"]], // 按照提交时间降序
			"info": true,
			"autoWidth": false,
			"language": {
				'emptyTable': '没有运行中的框架',
				'loadingRecords': '加载中...',
				'processing': '查询中...',
				'search': '搜索:',
				'lengthMenu': '每页 _MENU_ 条记录',
				'zeroRecords': '没有运行中的框架',
				'paginate': {
					'next': '下一页',
					'previous': '上一页'
				},
				'info': '第 _PAGE_ 页 / 共 _PAGES_ 页',
				'infoEmpty': '没有数据',
				'infoFiltered': '(从 _MAX_ 条记录中筛选)'
			},
			ajax: {
				url: monitorWebBaseURL + "mesos/getFrameworksByMesos",
				dataType: "json",
				dataSrc: function (data) { // 查询只需要这个dataSrc
					if (data.success.toString() == "true")
						return data.dataObject;
					else
						return [];
				}
			},
			columns: [
				{
					data: "hostname",
				},
				{
					data: "name"
				},
				{
					data: "user"
				},
				{
					data: "role"
				},
				{
					data: "tasks"
				},
				{
					data: "cpus"
				},
				{
					data: "gpus"
				},
				{
					data: "mem",
					"render": function (data, type, row) {
						return getMem(data);
					}
				},
				{
					data: "disk",
					"render": function (data, type, row) {
						return getMem(data);
					}
				},
				{
					data: "registered_time",
					"render": function (data, type, row) {
						return getDateTime(data);
					}
				}
			]
		});

		//初始化marathonFrameworksTable
		marathonFrameworksTable = $('#marathonFrameworksTable').DataTable({
			"paging": true,
			"lengthChange": true,
			"aLengthMenu": [5, 10, 20],
			"searching": false,
			"ordering": true,
			"order": [[0, "desc"]], // 按照提交时间降序
			"info": true,
			"autoWidth": false,
			"language": {
				'emptyTable': '没有在marathon的任务',
				'loadingRecords': '加载中...',
				'processing': '查询中...',
				'search': '搜索:',
				'lengthMenu': '每页 _MENU_ 条记录',
				'zeroRecords': '没有在marathon的任务',
				'paginate': {
					'next': '下一页',
					'previous': '上一页'
				},
				'info': '第 _PAGE_ 页 / 共 _PAGES_ 页',
				'infoEmpty': '没有数据',
				'infoFiltered': '(从 _MAX_ 条记录中筛选)'
			},
			ajax: {
				url: monitorWebBaseURL + "mesos/getFrameworksByMarathon",
				dataType: "json",
				dataSrc: function (data) { // 查询只需要这个dataSrc
					if (data.success.toString() == "true")
						return data.dataObject;
					else
						return [];
				}
			},
			columns: [
				{
					data: "name",
					"render": function (data, type, row) {
						if(data.indexOf("/") != -1){
							return data.substring(1);
						}
						return data;
					}
				},
				{
					data: "cpus"
				},
				{
					data: "gpus"
				},
				{
					data: "mem",
					"render": function (data, type, row) {
						return getMem(data);
					}
				},
				{
					data: "disk",
					"render": function (data, type, row) {
						return getMem(data);
					}
				},
				{
					data: "instances"
				},
				{
					"class": 'details-control see-detail-span',
					"orderable": false,
					"data": null,
					"defaultContent": '<i class="fa fa-plus-square-o"></i>'
				}
			],
			"initComplete": function () {
				$('#marathonFrameworksTable tbody tr:eq(0) td.see-detail-span').click();
			}
		});

		// 添加查看节点详情的点击事件
		$('#marathonFrameworksTable').find('tbody').on('click', 'td.details-control', function () {
			var tr = $(this).closest('tr');
			var row = marathonFrameworksTable.row(tr);
			if (row.child.isShown()) {
				row.child.hide();
				tr.removeClass('shown');
				$(tr).find("td.details-control i").removeClass('fa-minus-square');
				$(tr).find("td.details-control i").addClass('fa fa-plus-square-o');
			}
			else {
				row.child(generateDetail()).show();
				tr.addClass('shown');
				$(tr).find("td.details-control i").removeClass('fa fa-plus-square-o');
				$(tr).find("td.details-control i").addClass('fa fa-minus-square');

				var subRow = $(row.node()).next("tr");
				subRow.find(".refreshBtn").click(function () {
					fillInData(row);
				});
				// 填入数据
				fillInData(row);
			}
		});
	}

	//这个是定时刷新
	function fetchFrameWorkInterval() {
		if (activeFramework == null) {//去初始化
			initFrameworks();
			return;
		}
		$.ajax({
			type: "POST",
			url: monitorWebBaseURL + "mesos/frameworks",
			dataType: "json",
			success: function (data) {
				if (data.success) {
					var jsonData = JSON.parse(data.dataObject);
					var active = []; //活跃
					var inactive = [];//不活跃
					var total = jsonData.frameworks;
					$.each(total, function (i, m) {
						if (m["active"].toString() == "true") {
							active.push(m);
						} else {
							inactive.push(m);
						}

					});
				}
			}
		});
	}

	// 定时刷新上述数据
	function timeTask() {
		fetchMesosInterval();
		//initFrameworks();
	}

	$(function () {
		// 获取一些一段时间内基本不会改变的指标
		fetchMesosInterval();
		initFrameworks();
		// setInterval(timeTask, 8000);
		// //setInterval(fetchFrameWorkInterval, 8000);
		// setInterval( function () {
		// 	activeFramework.ajax.reload( null, false); // 刷新表格数据，分页信息不会重置
		// }, 8000 );
		// //设置一个时间间隔
		// setTimeout(() => {
		// 	//alert("");
		// }, 4000);
		// setInterval( function () {
		// 	marathonFrameworksTable.ajax.reload( null, false); // 刷新表格数据，分页信息不会重置
		// }, 8000 );
	});

	function getMem(mem) {// 单位是m
		// 基本转换成G
		var result = (mem / 1024).toFixed(1);
		if (result < 1024) {
			return result + "<small>GB</small>";
		}
		else {
			return (result / 1024).toFixed(1) + "<small>TB</small>";
		}
	}

	function getDateTime(tempTime) {
		var d = new Date(tempTime * 1000);
		return d.customFormat("#YYYY#:#MM#:#DD# #hh#:#mm#:#ss#");
	}
})();

