(function () {

	var table = null;

	function generateDetail(data) {
		var tp = $("#nodeDetailTemplate");
		tp.find(".m_clusterId").html(data.clusterId);
		tp.find(".m_queue").html(data.queue);
		tp.find(".m_trackingUI").html(data.trackingUI);
		tp.find(".m_trackingUrl").html('<a href="'+data.trackingUrl+'" class="trackingUrl">'+data.trackingUrl+'</a>');
		tp.find(".m_amContainerLogs").html('<a href="'+data.amContainerLogs+'" class="trackingUrl">'+data.amContainerLogs+'</a>');
		tp.find(".m_amHostHttpAddress").html('<a href="'+data.amHostHttpAddress+'" class="trackingUrl">'+data.amHostHttpAddress+'</a>');
		tp.find(".m_allocatedMB").html(data.allocatedMB);
		tp.find(".m_allocatedVCores").html(data.allocatedVCores);
		tp.find(".m_runningContainers").html(data.runningContainers);
		tp.find(".m_memorySeconds").html(data.memorySeconds);
		tp.find(".m_vcoreSeconds").html(data.vcoreSeconds);
		tp.find(".overlay").hide();
		return tp.html();
	}

	$(function () {
		table = $('#appListTable').DataTable({
			"paging": true,
			"lengthChange": true,
			"aLengthMenu": [5, 10, 20],
			"searching": true,
			"ordering": true,
			"order": [[5, "desc"]],
			"info": true,
			"autoWidth": false,
			"language": {
				'emptyTable': 'No running applications',
				'loadingRecords': 'Loading...',
				'processing': 'Processing...',
				'search': 'Search:',
				'lengthMenu': 'Show _MENU_ entries',
				'zeroRecords': 'No running applications',
				'paginate': { 'next': 'Next', 'previous': 'Previous' },
				'info': 'Page _PAGE_ of _PAGES_',
				'infoEmpty': 'No data',
				'infoFiltered': '(filtered from _MAX_ total entries)'
			},
			ajax: {
				url: monitorWebBaseURL + "app/list",
				dataType: "json",
				dataSrc: function (result) {
					var apps = (result.apps && result.apps.app) ? result.apps.app : [];
					var running = 0, failed = 0, killed = 0, finished = 0;
					$.each(apps, function (_, a) {
						var s = (a.state || '').toUpperCase();
						if (s === 'RUNNING')  running++;
						if (s === 'FAILED')   failed++;
						if (s === 'KILLED')   killed++;
						if (s === 'FINISHED') finished++;
					});
					$('#mrRunningNum').text(running);
					$('#mrFailedNum').text(failed);
					$('#mrKilledNum').text(killed);
					$('#mrFinishedNum').text(finished);
					return apps;
				}
			},
			columns: [
				{data: "id"},
				{data: "user"},
				{data: "name"},
				{data: "applicationType"},
				{
					"data": 'progress',
					"render": function (data) {
						return '<div class="progress" style="margin-top:0">' +
							'<div class="progress-bar progress-bar-success progress-bar-striped" role="progressbar" style="min-width:2em;width:' + data + '%">' +
							data.toFixed(1) + '%</div></div>';
					}
				},
				{
					"data": 'startedTime',
					"render": function (data) {
						var d = new Date(data);
						var pad = function(n){return n<10?"0"+n:n;};
						return d.getFullYear()+"/"+pad(d.getMonth()+1)+"/"+pad(d.getDate())+" "+pad(d.getHours())+":"+pad(d.getMinutes())+":"+pad(d.getSeconds());
					}
				},
				{
					"data": 'elapsedTime',
					"render": function (data) {
						var days  = Math.floor(data / 86400000); data -= days  * 86400000;
						var hours = Math.floor(data / 3600000);  data -= hours * 3600000;
						var mins  = Math.floor(data / 60000);    data -= mins  * 60000;
						var secs  = Math.floor(data / 1000);
						var str = "";
						if (days)  str += days  + "d:";
						if (hours) str += hours + "h:";
						if (mins)  str += mins  + "m:";
						str += secs + "s";
						return str;
					}
				}
			],
			"createdRow": function (row, data) {
			if ((data.state || "").toUpperCase() !== "RUNNING") return;
				row = table.row(row);
				if (!row.child.isShown()) row.child(generateDetail(data)).show();
			}
		});

		setInterval(function () { table.ajax.reload(null, false); }, 5000);
	});
})();
