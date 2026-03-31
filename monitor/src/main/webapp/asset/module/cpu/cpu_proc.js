/**
 * Created by likeugo on 2017/7/24
 */

(function() {

	var chartTotal = null, chartRun = null;
	var procTotal = [], procRun = [];

	function makeOption(title, yMin, yMax) {
		var timeAxisLabel = {
			formatter: function (val) {
				var d = new Date(val);
				var pad = function (n) { return n < 10 ? '0' + n : n; };
				return (d.getMonth()+1) + '/' + pad(d.getDate()) + '\n' +
					pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds());
			}
		};
		return {
			title: { text: title, top: 5, left: 5 },
			grid: { height: 160, left: '3%', right: '4%', bottom: '15%', containLabel: true },
			tooltip: { trigger: 'axis' },
			toolbox: { feature: { saveAsImage: {} } },
			dataZoom: [{ type: 'inside' }],
			xAxis: { type: 'time', splitLine: { show: false }, boundaryGap: false, axisLabel: timeAxisLabel },
			yAxis: { type: 'value', min: yMin != null ? yMin : 0, max: yMax != null ? yMax : null, minInterval: 1 },
			series: [{ name: title, type: 'line', smooth: true, symbol: 'none', areaStyle: {}, data: [] }]
		};
	}

	function fetchInitial(limit) {
		$.ajax({
			type: 'POST',
			url: monitorWebBaseURL + 'cpu/fetchCPU_Proc',
			data: { 'limit': limit },
			dataType: 'json',
			success: function(data) {
				if (data != null) {
					var procRuns   = data.dataObject.procRun;
					var procTotals = data.dataObject.procTotal;

					procRuns.reverse();
					procTotals.reverse();

					var now = new Date().getTime();
					var count = procRuns.length;
					for (var i = 0; i < count; i++) {
						var t = now - (count - 1 - i) * 3000;
						procRun.push([t, parseInt(procRuns[i].sum + 4)]);
						procTotal.push([t, parseInt(procTotals[i].sum)]);
					}

					chartTotal.setOption({ series: [{ data: procTotal }] });
					chartRun.setOption({ series: [{ data: procRun }] });

					updateLabels(procRuns, procTotals, 0);
				}
			}
		});
	}

	function fetchLatest() {
		$.ajax({
			type: 'POST',
			url: monitorWebBaseURL + 'cpu/fetchCPU_Proc',
			data: { 'limit': 1 },
			dataType: 'json',
			success: function(data) {
				if (data != null) {
					var procRuns   = data.dataObject.procRun;
					var procTotals = data.dataObject.procTotal;

					var t = new Date().getTime();

					if (procRun.length > 0 && procRun[procRun.length - 1][0] === t) return;

					procRun.push([t, parseInt(procRuns[0].sum + 4)]);
					procTotal.push([t, parseInt(procTotals[0].sum)]);

					if (procRun.length >= 200) { procRun.shift(); procTotal.shift(); }

					chartTotal.setOption({ series: [{ data: procTotal }] });
					chartRun.setOption({ series: [{ data: procRun }] });

					updateLabels(procRuns, procTotals, 0);
				}
			}
		});
	}

	function updateLabels(procRuns, procTotals, idx) {
		$('#procRunningNum').html(parseInt(procRuns[idx].sum + 4));
		$('#procTatalNum').html(parseInt(procTotals[idx].sum));
	}

	$(function() {
		chartTotal = echarts.init(document.getElementById('cpu_proc_total'));
		chartRun   = echarts.init(document.getElementById('cpu_proc_run'));

		chartTotal.setOption(makeOption('Total Process Count', 700, 850));
		chartRun.setOption(makeOption('Running Process Count', 0, 30));

		fetchInitial(50);

		setInterval(fetchLatest, 3000);
	});
})();
