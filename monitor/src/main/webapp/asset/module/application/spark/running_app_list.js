(function () {

	var table = null;

	//从详情模板拷贝内容
	function generateDetail(data) {
		var tp = $("#nodeDetailTemplate");
		return tp.html();
	}

	function formatGMTTime(time) {
		return time.substring(0,10) + " " + time.substring(11,19);
	}
	
	function fillInData(row) {
		var subRow = $(row.node()).next("tr");
		subRow.find(".overlay").show();
		$.ajax({
			type: "GET",
			url: monitorWebBaseURL + "app/spark/" + row.data().id + "/jobs",
			dataType: "json",
			success: function (data) {
				if (data !== null) {

					//写入APP所有Job的信息
					var jobsContent = "";
					if(data.length === 0){
						jobsContent = "<div style='text-align: center;'>没有更多内容</div>";
					} else {
						var count = 0;
						$.each(data, function (i, job) {
							if (count !== 0) {
								jobsContent += "<hr />"
							}
							var jobTemp = $("#jobTemplate");
							jobTemp.find(".job_id").html(job.jobId);
							jobTemp.find(".job_name").html(job.name);
							jobTemp.find(".submission_time").html(formatGMTTime(job.submissionTime));
							jobTemp.find(".stage_count").html('<span class="badge bg-blue">' + job.stageIds.length + '</span>');

							var jobProgressVal = 100 * job.numCompletedTasks / job.numTasks;
							var jobProgress = '<div class="progress" style="margin-top: 0;width: 50%;">\
									<div class="progress-bar progress-bar-success progress-bar-striped" role="progressbar" aria-valuenow="' + jobProgressVal + '" aria-valuemin="0" aria-valuemax="100" style="min-width: 2em;width: ' + jobProgressVal + '%">\
										' + jobProgressVal.toFixed(1) + '%\
									</div>\
								</div>';
							jobTemp.find(".job_progress").html(jobProgress);

							jobTemp.find(".num_tasks").html('<span class="badge bg-blue">' + job.numTasks + '</span>');
							jobTemp.find(".num_active_tasks").html('<span class="badge bg-orange">' + job.numActiveTasks + '</span>');
							jobTemp.find(".num_completed_tasks").html('<span class="badge bg-success">' + job.numCompletedTasks + '</span>');
							jobTemp.find(".num_skipped_tasks").html('<span class="badge bg-success">' + job.numSkippedTasks + '</span>');
							jobTemp.find(".num_failed_tasks").html('<span class="badge bg-red">' + job.numFailedTasks + '</span>');
							jobTemp.find(".num_active_stages").html('<span class="badge bg-orange">' + job.numActiveStages + '</span>');
							jobTemp.find(".num_completed_stages").html('<span class="badge bg-success">' + job.numCompletedStages + '</span>');
							jobTemp.find(".num_skipped_stages").html('<span class="badge bg-success">' + job.numSkippedStages + '</span>');
							jobTemp.find(".num_failed_stages").html('<span class="badge bg-red">' + job.numFailedStages + '</span>');
							jobsContent += jobTemp.html();
							count++;
						});
					}
					subRow.find(".jobListContent").html(jobsContent);
					subRow.find(".overlay").hide();

				}
			}
		});
	}

	$(function () {
		table = $('#appListTable').DataTable({
			"paging": true,
			"lengthChange": true,
			"aLengthMenu": [5, 10, 20],
			"searching": true,
			"ordering": true,
			"order": [[5, "desc"]], //按照提交时间降序
			"info": true,
			"autoWidth": false,
			"language": {
				'emptyTable': '没有正在运行的应用',
				'loadingRecords': '加载中...',
				'processing': '查询中...',
				'search': '搜索:',
				'lengthMenu': '每页 _MENU_ 条记录',
				'zeroRecords': '没有正在运行的应用',
				'paginate': {
					'next': '下一页',
					'previous': '上一页'
				},
				'info': '第 _PAGE_ 页 / 共 _PAGES_ 页',
				'infoEmpty': '没有数据',
				'infoFiltered': '(从 _MAX_ 条记录中筛选)'
			},
			ajax: {
				url: monitorWebBaseURL + "app/spark",
				dataType: "json",
				dataSrc: function (result) {
					var itemList = [];
					$.each(result,function (i,item) {

						//计算所有尝试的持续时间总和
						var sumDuration = 0;
						$.each(item.attempts,function (ii,att) {
							sumDuration += att.duration;
						});
						var firstAttempts = item.attempts[0];
						itemList.push({
							id:item.id,
							name:item.name,
							startTime:firstAttempts.startTimeEpoch,
							duration:sumDuration,
							sparkUser:firstAttempts.sparkUser,
							progress:100.0,
						});
					});
					return itemList;
				}
			},
			columns: [
				{data: "id"},
				{data: "sparkUser"},
				{data: "name"},
				{
					"data": 'startTime',
					"render": function (data, type, row) {
						var d = new Date(data);
						return d.customFormat("#YYYY#/#MM#/#DD# #hh#:#mm#:#ss#");
					}
				},
				{
					"data": 'duration',
					"render": function (data, type, row) {

						var totalSeconds = 0;
						var totalMins = 0;
						var totalHours = 0;
						var totalDays = 0;

						totalDays = Math.floor(data / 1000 / 60 / 60 / 24);
						data -= totalDays * 1000 * 60 * 60 * 24;
						totalHours = Math.floor(data / 1000 / 60 / 60);
						data -= totalHours * 1000 * 60 * 60;
						totalMins = Math.floor(data / 1000 / 60);
						data -= totalMins * 1000 * 60;
						totalSeconds = Math.floor(data / 1000);

						var str = "";
						if (totalDays !== 0) str = str + totalDays + "d:";
						if (totalHours !== 0) str = str + totalHours + "h:";
						if (totalMins !== 0) str = str + totalMins + "m:";
						str = str + totalSeconds + "s";
						return str;
					},
					"orderable": false
				},
				{
					"class": 'details-control see-detail-span',
					"orderable": false,
					"data": null,
					"defaultContent": '<i class="fa fa-plus-square-o"></i>'
				}
			],
			"initComplete": function () {
				$('#appListTable').find('tbody tr:eq(0) td.see-detail-span').click();
			}
		});


		// 添加查看节点详情的点击事件
		$('#appListTable').find('tbody').on('click', 'td.details-control', function () {
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
			}
		});

		// setInterval(function () {
		// 	table.ajax.reload(null, false);
		// }, 10000);
	});
})();