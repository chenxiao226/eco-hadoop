/**
 * Created by likeugo on 2017/7/24
 */

(function() {

	var WIN = 3 * 60 * 1000;
	var chartUsage = null, chartSystem = null, chartUser = null, chartWio = null;

	var cpuIdle = [], cpuSystem = [], cpuUser = [], cpuWio = [];
	var lastProcessTime = 0;

	var timeAxisLabel = {
		formatter: function (val) {
			var d = new Date(val);
			var pad = function (n) { return n < 10 ? '0' + n : n; };
			return (d.getMonth()+1) + '/' + pad(d.getDate()) + '\n' +
				pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds());
		}
	};

	function makeOption(title, yMin, yMax) {
		return {
			title: { text: title, top: 5, left: 5 },
			grid: { height: 130, left: '3%', right: '4%', bottom: '12%', containLabel: true },
			tooltip: { trigger: 'axis' },
			toolbox: { feature: { saveAsImage: {} } },
			dataZoom: [{ type: 'inside' }],
			xAxis: { type: 'time', splitLine: { show: false }, boundaryGap: false, axisLabel: timeAxisLabel },
			yAxis: { type: 'value', name: '(%)', min: yMin != null ? yMin : 0, max: yMax != null ? yMax : null, minInterval: 1 },
			series: [{ name: title, type: 'line', smooth: true, symbol: 'none', data: [] }]
		};
	}

	function fetchCPUDataInterval(limit) {
		if (limit == null) limit = 1;
		$.ajax({
			type: 'POST',
			url: monitorWebBaseURL + 'cpu/fetchCPU_Ratio',
			data: { 'limit': limit },
			dataType: 'json',
			success: function(data) {
				if (data != null) {
					var cpuIdles = data.dataObject.cpuIdle;
					var cpuNices = data.dataObject.cpuNice;
					var cpuSystems = data.dataObject.cpuSystem;
					var cpuUsers = data.dataObject.cpuUser;
					var cpuWios = data.dataObject.cpuWio;

					cpuIdles.reverse();
					cpuSystems.reverse();
					cpuUsers.reverse();
					cpuWios.reverse();

					var now = new Date().getTime();
					var count = cpuIdles.length;
					for (var i in cpuIdles) {
						var t = now - (count - 1 - i) * 1000;
						cpuIdle.push([t, (100 - cpuIdles[i].sum / cpuIdles[i].num).toFixed(2)]);
						cpuSystem.push([t, (cpuSystems[i].sum / cpuSystems[i].num).toFixed(2)]);
						cpuUser.push([t, (cpuUsers[i].sum / cpuUsers[i].num).toFixed(2)]);
						cpuWio.push([t, (cpuWios[i].sum / cpuWios[i].num).toFixed(2)]);
						if (t > lastProcessTime) lastProcessTime = t;
					}

					trimData();
					updateCharts();
					updateLabels(cpuIdles, cpuSystems, cpuUsers, cpuWios, count - 1);
				}
			}
		});
	}

	function fetchCPUDataIntervalByone() {
		$.ajax({
			type: 'POST',
			url: monitorWebBaseURL + 'cpu/fetchCPU_Ratio',
			data: { 'limit': 1 },
			dataType: 'json',
			success: function(data) {
				if (data != null) {
					var cpuIdles = data.dataObject.cpuIdle;
					var cpuSystems = data.dataObject.cpuSystem;
					var cpuUsers = data.dataObject.cpuUser;
					var cpuWios = data.dataObject.cpuWio;

					var t = new Date().getTime();
					cpuIdle.shift();
					cpuSystem.shift();
					cpuUser.shift();
					cpuWio.shift();
					cpuIdle.push([t, (100 - cpuIdles[0].sum / cpuIdles[0].num).toFixed(2)]);
					cpuSystem.push([t, (cpuSystems[0].sum / cpuSystems[0].num).toFixed(2)]);
					cpuUser.push([t, (cpuUsers[0].sum / cpuUsers[0].num).toFixed(2)]);
					cpuWio.push([t, (cpuWios[0].sum / cpuWios[0].num).toFixed(2)]);

					updateCharts();
					updateLabels(cpuIdles, cpuSystems, cpuUsers, cpuWios, 0);
				}
			}
		});
	}

	function trimData() {
		if (cpuIdle.length >= 200) { cpuIdle.shift(); cpuSystem.shift(); cpuUser.shift(); cpuWio.shift(); }
	}

	function updateCharts() {
		var now = new Date().getTime();
		chartUsage.setOption({ series: [{ data: cpuIdle }], xAxis: { min: now - WIN, max: now } });
		chartSystem.setOption({ series: [{ data: cpuSystem }], xAxis: { min: now - WIN, max: now } });
		chartUser.setOption({ series: [{ data: cpuUser }], xAxis: { min: now - WIN, max: now } });
		chartWio.setOption({ series: [{ data: cpuWio }], xAxis: { min: now - WIN, max: now } });
	}

	function updateLabels(cpuIdles, cpuSystems, cpuUsers, cpuWios, idx) {
		$("#cpuIdle2").html((100 - cpuIdles[idx].sum / cpuIdles[idx].num).toFixed(2) + "%");
		$("#cpuSystem").html((cpuSystems[idx].sum / cpuSystems[idx].num).toFixed(2) + "%");
		$("#cpuUser").html((cpuUsers[idx].sum / cpuUsers[idx].num).toFixed(2) + "%");
		$("#cpuWio").html((cpuWios[idx].sum / cpuWios[idx].num).toFixed(2) + "%");
	}

	$(function() {
		chartUsage = echarts.init(document.getElementById('cpu_ratio_usage'));
		chartSystem = echarts.init(document.getElementById('cpu_ratio_system'));
		chartUser = echarts.init(document.getElementById('cpu_ratio_user'));
		chartWio = echarts.init(document.getElementById('cpu_ratio_wio'));

		chartUsage.setOption(makeOption('CPU Usage (%)', 0, 100));
		chartSystem.setOption(makeOption('Kernel (%)', 0, 30));
		chartUser.setOption(makeOption('User (%)', 0, 60));
		chartWio.setOption(makeOption('I/O Wait (%)', 0, 5));

		fetchCPUDataInterval(50);
		setTimeout(function() {
			chartUsage.hideLoading();
			chartSystem.hideLoading();
			chartUser.hideLoading();
			chartWio.hideLoading();
		}, 1000);

		setInterval(fetchCPUDataIntervalByone, 5000);
	});
})();
