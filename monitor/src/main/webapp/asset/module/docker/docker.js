(function () {
	//alert(111);
	var table = null;

	//从详情模板拷贝内容
	function generateDetail() {
		return $("#nodeDetailTemplate").html();
	}

	function fillInData(row) {
		
		var subRow = $(row.node()).next("tr");
		var containerId = row.data().containerId;		
		subRow.find(".overlay").show();
		$.ajax({
			type: "POST",
			url: monitorWebBaseURL + "docker/total",
			data: {
				//id: containerId,
				//nocache: new Date().getTime()
			},
			dataType: "json",
			success: function (data) {
				if (data != null) {
					// var cpuSpeed, cpuIdle, loadOne, loadFive, diskFree, diskTotal, memFree, memTotal, memBuffers,
					// 	memCached, memShared;									
					
					/*
					 * 以下判断为对静态数组的操作，接入数据库后，需更改					 * 
					 */
					var goal =0 ;
					for(i=0;i<data.dataObject.length;i++)
						{
							if(data.dataObject[i].id==containerId){
								goal = i;
								break;
							}
						}
					//alert(data.dataObject[goal].aliases[0]);					
					var statslength = data.dataObject[goal].stats.length;
					var cpusTotal = data.dataObject[goal].stats[0].cpu.usage.per_cpu_usage.length;
					var cpusTotalusage = (data.dataObject[goal].stats[statslength-1].cpu.usage.total-data.dataObject[goal].stats[statslength-2].cpu.usage.total)/(data.dataObject[goal].stats[statslength-1].timestamp-data.dataObject[goal].stats[statslength-2].timestamp)/10000000;
					var cpusBreakdownUser = (data.dataObject[goal].stats[statslength-1].cpu.usage.user - data.dataObject[goal].stats[statslength-2].cpu.usage.user)/(data.dataObject[goal].stats[statslength-1].timestamp-data.dataObject[goal].stats[statslength-2].timestamp)/10000000;
					var cpuBreakdownSystem = (data.dataObject[goal].stats[statslength-1].cpu.usage.system - data.dataObject[goal].stats[statslength-2].cpu.usage.system)/(data.dataObject[goal].stats[statslength-1].timestamp-data.dataObject[goal].stats[statslength-2].timestamp)/10000000;
					var cpuscore0Usage =(data.dataObject[goal].stats[statslength-1].cpu.usage.per_cpu_usage[0] - data.dataObject[goal].stats[statslength-2].cpu.usage.per_cpu_usage[0])/(data.dataObject[goal].stats[statslength-1].timestamp-data.dataObject[goal].stats[statslength-2].timestamp)/10000000;
					var cpuscore1Usage = (data.dataObject[goal].stats[statslength-1].cpu.usage.per_cpu_usage[1] - data.dataObject[goal].stats[statslength-2].cpu.usage.per_cpu_usage[1])/(data.dataObject[goal].stats[statslength-1].timestamp-data.dataObject[goal].stats[statslength-2].timestamp)/10000000;
					

					//var memTotalBytes = 2048;
					var memUsage = data.dataObject[goal].stats[statslength-1].memory.working_set;
					var memCacheUsage = data.dataObject[goal].stats[statslength-1].memory.cache;
					var memRss = data.dataObject[goal].stats[statslength-1].memory.rss;					

					var nr_io_wait = data.dataObject[goal].stats[statslength-1].task_stats.nr_io_wait;
					var nr_running = data.dataObject[goal].stats[statslength-1].task_stats.nr_running;
					var nr_sleeping = data.dataObject[goal].stats[statslength-1].task_stats.nr_sleeping;
					var nr_stopped = data.dataObject[goal].stats[statslength-1].task_stats.nr_stopped;
					var nr_uninterruptible = data.dataObject[goal].stats[statslength-1].task_stats.nr_uninterruptible;



					// 填入数据
					subRow.find(".cpuNum").html(cpusTotal.toFixed(1));
					subRow.find(".cpusTotalusage").html(cpusTotalusage.toFixed(2)+"%");
					subRow.find(".cpusBreakdownUser").html(cpusBreakdownUser.toFixed(2)+"%");
					subRow.find(".cpuBreakdownSystem").html(cpuBreakdownSystem.toFixed(2)+"%");
					subRow.find(".cpuscore0Usage").html(cpuscore0Usage.toFixed(2)+"%");
					subRow.find(".cpuscore1Usage").html(cpuscore1Usage.toFixed(2)+"%");

					//subRow.find(".memTotal").html(memTotalBytes + " MB");
					subRow.find(".memUsage").html((memUsage / 1024 / 1024 ).toFixed(2) + " MB");
					subRow.find(".memCacheUsage").html((memCacheUsage / 1024 / 1024).toFixed(2) + " MB");
					subRow.find(".memRss").html((memRss/1024/1024).toFixed(2) + "MB");

				
					subRow.find(".nr_io_wait").html(nr_io_wait);
					subRow.find(".nr_running").html(nr_running);
					subRow.find(".nr_sleeping").html(nr_sleeping);
					subRow.find(".nr_stopped").html(nr_stopped);
					subRow.find(".nr_uninterruptible").html(nr_uninterruptible);

					

					//显示饼图
//					var memoryPieChart = echarts.init(subRow.find(".pieChart")[0]);
//					memoryPieChart.setOption({
//						tooltip: {
//							trigger: 'item',
//							formatter: "{a} <br/>{b}: {c} GB ({d}%)",
//							position: ['25%', '90%']
//						},
//						legend: {
//							orient: 'vertical',
//							x: 'right',
//							top: '20%',
//							data: ['可用内存', '已用内存']
//						},
//						series: [
//							{
//								name: '内存使用',
//								type: 'pie',
//								radius: '55%',
//								avoidLabelOverlap: false,
//								roseType: "radius",
//								center: ['35%', '50%'],
//								label: {
//									normal: {
//										show: false,
//									}
//								},
//								labelLine: {
//									normal: {
//										show: false
//									}
//								},
//								itemStyle: {
//									normal: {
//										shadowBlur: 30,
//										shadowColor: 'rgba(0, 0, 0, 0.5)'
//									}
//								},
//								data: [
//									{value: (memFreeBytes / 1024 / 1024 / 1024).toFixed(2), name: '可用内存'},
//									{
//										value: ((memTotalBytes - memFreeBytes) / 1024 / 1024 / 1024).toFixed(2),
//										name: '已用内存'
//									},
//								]
//							}
//						]
//					});

					subRow.find(".overlay").hide();
				}
			}
		});
	}

	function fetchMasterInfo(){
		$.ajax({
			type: "POST",
			url: monitorWebBaseURL + "docker/total",
			data: {},
			dataType: "json",
			success: function (data) {
				if (data != null) {
					$("#dockerCpuNum").html(data.dataObject[0].stats[0].cpu.usage.per_cpu_usage.length);
					$("#dockerperiod").html(data.dataObject[0].spec.cpu.period/1000000+"s");
					$("#dockerContainerNum").html(data.dataObject.length);					
					$("#masterHost").html("<span class='badge bg-blue'>" + "docker" + "</span>");
//					$("#masterTotalMem").html((data.mem_total_bytes / 1048576).toFixed(1) + "MB");
				}
			}
		});
	}

	$(function () {

		fetchMasterInfo();

		table = $('#dockerListTable').DataTable({
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
				url: monitorWebBaseURL + "docker/total",
				dataType: "json",
				dataSrc: function (data) {
					var itemList = [];
					$.each(data.dataObject,function (i,item) {						
						itemList.push({
							appName:item.aliases[0],
//							disk:(item.resources.disk / 1024).toFixed(1) +"GB",
							mem:"2GB",
							cpus:item.stats[i].cpu.usage.per_cpu_usage.length,
//							gpus:item.resources.gpus,
							containerId:item.id,
							namespace:item.namespace,
							registered_time:new Date(Math.round(item.spec.creation_time) * 1000).customFormat("#YYYY#/#MM#/#DD# #hh#:#mm#:#ss#"),
							state:item.active == true?"活动的":"失联的",
						});
					});
					return itemList;
				}
			},
			columns: [
				{data: "appName"},
				{data: "containerId"},
				{data: "namespace"},
				{data: "cpus"},				
				{data: "mem"},				
				{data: "registered_time"},
				{
					"class": 'details-control see-detail-span',
					"orderable": false,
					"data": null,
					"defaultContent": '<i class="fa fa-plus-square-o"></i>'
				}
			],
			"initComplete": function () {
				$('#dockerListTable tbody tr:eq(0) td.see-detail-span').click();
			}
		});

		// 添加查看节点详情的点击事件
		$('#dockerListTable').find('tbody').on('click', 'td.details-control', function () {
			var tr = $(this).closest('tr');
			var row = table.row(tr);
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
				//alert(row.data());
				
			}
		});
	});
})();