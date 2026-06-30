(function () {

	var WIN = 1 * 60 * 1000;

	var chartNm    = null;
	var chartMem   = null;
	var chartCores = null;

	var activeData = [], lostData = [];
	var memAvailData = [], memAllocData = [];
	var coreAvailData = [], coreAllocData = [];

	var nmLast = 0, memLast = 0, coreLast = 0;

	function pushPoint(arr, t, v) {
		arr.push([t, v]);
		while (arr.length > 300) arr.shift();
	}

	function fetchClusterMetrics() {
		$.ajax({
			type: 'GET',
			url: '/monitor/yarn/clusterMetrics',
			dataType: 'json',
			success: function (data) {
				if (!data || !data.clusterMetrics) return;
				var m = data.clusterMetrics;
				var t = new Date().getTime();

				// NodeManager
				$('#nm-active').text(m.activeNodes  || 0);
				$('#nm-lost').text(m.lostNodes    || 0);
				pushPoint(activeData, t, m.activeNodes  || 0);
				pushPoint(lostData,   t, m.lostNodes    || 0);
				nmLast = t;
				chartNm.setOption({
					series: [{ data: activeData }, { data: lostData }],
					xAxis: { min: nmLast - WIN, max: nmLast }
				});

				// Memory
				var memTotal = m.totalMB     || 0;
				var memAvail = m.availableMB  || 0;
				var memAlloc = m.allocatedMB  || 0;
				var memRsv   = m.reservedMB   || 0;
				$('#memTotal').text(memTotal);
				$('#memAvailable').text(memAvail);
				$('#memAllocated').text(memAlloc);
				$('#memReserved').text(memRsv);
				pushPoint(memAvailData, t, memAvail);
				pushPoint(memAllocData, t, memAlloc);
				memLast = t;
				chartMem.setOption({
					series: [{ data: memAvailData }, { data: memAllocData }],
					xAxis: { min: memLast - WIN, max: memLast }
				});

				// CPU Cores
				var coreTotal = m.totalVirtualCores     || 0;
				var coreAvail = m.availableVirtualCores  || 0;
				var coreAlloc = m.allocatedVirtualCores  || 0;
				var coreRsv   = m.reservedVirtualCores   || 0;
				$('#coresTotal').text(coreTotal);
				$('#coresAvailable').text(coreAvail);
				$('#coresAllocated').text(coreAlloc);
				$('#coresReserved').text(coreRsv);
				pushPoint(coreAvailData, t, coreAvail);
				pushPoint(coreAllocData, t, coreAlloc);
				coreLast = t;
				chartCores.setOption({
					series: [{ data: coreAvailData }, { data: coreAllocData }],
					xAxis: { min: coreLast - WIN, max: coreLast }
				});
			}
		});
	}

	var timeAxisLabel = {
		formatter: function (val) {
			var d = new Date(val);
			var pad = function (n) { return n < 10 ? '0' + n : n; };
			return (d.getMonth()+1) + '/' + pad(d.getDate()) + '\n' +
				pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds());
		}
	};

	function makeDualOption(title, name1, color1, name2, color2) {
		return {
			title: { text: title, top: 5, left: 'center', textStyle: { fontSize: 13 } },
			tooltip: { trigger: 'axis' },
			legend: { data: [name1, name2], top: 30 },
			toolbox: { feature: { saveAsImage: {} }, right: 10, top: 5 },
			dataZoom: [{ type: 'inside' }],
			grid: { left: '3%', right: '4%', top: 70, bottom: '5%', containLabel: true },
			xAxis: { type: 'time', splitLine: { show: false }, boundaryGap: false, axisLabel: timeAxisLabel },
			yAxis: { type: 'value', minInterval: 1 },
			series: [
				{ name: name1, type: 'line', smooth: true, symbol: 'none', lineStyle: { color: color1 }, itemStyle: { color: color1 }, areaStyle: { color: color1 + '33' }, data: [] },
				{ name: name2, type: 'line', smooth: true, symbol: 'none', lineStyle: { color: color2 }, itemStyle: { color: color2 }, areaStyle: { color: color2 + '33' }, data: [] }
			]
		};
	}

	$(function () {
		chartNm    = echarts.init(document.getElementById('nmChart'));
		chartMem   = echarts.init(document.getElementById('memChart'));
		chartCores = echarts.init(document.getElementById('coresChart'));

		chartNm.setOption(makeDualOption('NodeManager Status', 'Active NMs', '#409EFF', 'Lost NMs', '#F56C6C'));
		chartMem.setOption(makeDualOption('YARN Memory (MB)', 'Available MB', '#67C23A', 'Allocated MB', '#E6A23C'));
		chartCores.setOption(makeDualOption('YARN CPU Cores', 'Available Cores', '#9B59B6', 'Allocated Cores', '#1ABC9C'));

		setTimeout(function () {
			chartNm.resize(); chartMem.resize(); chartCores.resize();
		}, 100);

		window.addEventListener('resize', function () {
			setTimeout(function () {
				chartNm.resize(); chartMem.resize(); chartCores.resize();
			}, 100);
		});

		$('a[data-toggle="tab"]').on('shown.bs.tab', function () {
			setTimeout(function () {
				chartNm.resize(); chartMem.resize(); chartCores.resize();
			}, 100);
		});

		fetchClusterMetrics();
		setInterval(fetchClusterMetrics, 5000);
	});

})();
