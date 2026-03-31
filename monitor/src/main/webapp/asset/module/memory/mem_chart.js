(function () {

	var monitorWebBaseURL = "/monitor/memory/";

	// ── 雷达图 ──────────────────────────────────────────────
	var chartRadar = null;

	function initRadar() {
		chartRadar = echarts.init(document.getElementById('chart_mem_radar'));
		chartRadar.setOption({
			title: { text: 'Memory Overview (GB)', top: 5, left: 5, textStyle: { fontSize: 14 } },
			tooltip: {
				trigger: 'item',
				formatter: function(p) {
					var v = p.value;
					var names = ['Free', 'Swap', 'Buffer', 'Cache', 'Shared'];
					var s = p.name + '<br/>';
					for (var i = 0; i < names.length; i++) {
						s += names[i] + ': ' + v[i] + ' GB<br/>';
					}
					return s;
				}
			},
			legend: { data: ['Memory'], bottom: 5 },
			radar: {
				indicator: [
					{ name: 'Free',   max: 10 },
					{ name: 'Swap',   max: 5  },
					{ name: 'Buffer', max: 5  },
					{ name: 'Cache',  max: 50 },
					{ name: 'Shared', max: 2  }
				],
				center: ['50%', '52%'],
				radius: '62%',
				axisName: { color: '#333', fontSize: 12 },
				splitArea: { areaStyle: { color: ['rgba(114,172,209,0.1)', 'rgba(114,172,209,0.2)', 'rgba(114,172,209,0.3)', 'rgba(114,172,209,0.4)'] } },
				axisLine: { lineStyle: { color: 'rgba(114,172,209,0.5)' } },
				splitLine: { lineStyle: { color: 'rgba(114,172,209,0.5)' } }
			},
			series: [{
				name: 'Memory',
				type: 'radar',
				data: [{ value: [0, 0, 0, 0, 0], name: 'Memory',
					areaStyle: { color: 'rgba(64,158,255,0.3)' },
					lineStyle: { color: '#409EFF', width: 2 },
					itemStyle: { color: '#409EFF' }
				}]
			}]
		});
	}

	function fetchRadar() {
		$.ajax({
			url: monitorWebBaseURL + 'dynamic',
			data: { limit: 1 },
			dataType: 'json',
			success: function (response) {
				if (!response.success) return;
				var d = response.dataObject;
				var free   = d.memFree   && d.memFree.length   ? (d.memFree[0].sum   / 1024).toFixed(2) : 0;
				var swap   = d.swapFree  && d.swapFree.length  ? (d.swapFree[0].sum  / 1024).toFixed(2) : 0;
				var buf    = d.memBuffer && d.memBuffer.length  ? (d.memBuffer[0].sum / 1024).toFixed(2) : 0;
				var cache  = d.memCache  && d.memCache.length   ? (d.memCache[0].sum  / 1024).toFixed(2) : 0;
				var shared = d.memShared && d.memShared.length  ? (d.memShared[0].sum / 1024).toFixed(2) : 0;

				getTotalMemMB(function(totalMemMB) {
					var freeVal = parseFloat(free);
					var freeMax = totalMemMB > 0
						? Math.ceil(totalMemMB / 1024)
						: Math.ceil(freeVal * 1.5 / 10) * 10;
					if (freeMax < freeVal + 1) freeMax = Math.ceil(freeVal * 1.5 / 10) * 10;
					chartRadar.setOption({
						radar: { indicator: [
							{ name: 'Free',   max: freeMax },
							{ name: 'Swap',   max: 5  },
							{ name: 'Buffer', max: 5  },
							{ name: 'Cache',  max: 50 },
							{ name: 'Shared', max: 2  }
						]},
						series: [{ data: [{ value: [free, swap, buf, cache, shared], name: 'Memory' }] }]
					});
				});

				// 更新顶部 memUsedInfo 进度条
				var totalMemGB = parseFloat($("#totalMem").data("totalMemValue") || 0);
				if (totalMemGB > 0) {
					var freeGB = parseFloat(free);
					var usedGB = (totalMemGB - freeGB).toFixed(1);
					var pct    = (usedGB / totalMemGB * 100).toFixed(1);
					$("#memUsedInfo").html(usedGB + "GB / " + totalMemGB.toFixed(1) + "GB");
					$("#memUsedProgressBar").css("width", pct + "%");
				}
			}
		});
	}

	// ── 内存使用率趋势折线图 ──────────────────────────────────────
	var chartDisk = null;
	var diskUsedData = [];
	var diskLastTime = 0;
	var cachedTotalMemMB = 0;

	function getTotalMemMB(callback) {
		if (cachedTotalMemMB > 0) { callback(cachedTotalMemMB); return; }
		$.ajax({
			url: monitorWebBaseURL + 'basic',
			dataType: 'json',
			success: function(r) {
				if (r.success && r.dataObject && r.dataObject.totalMem) {
					cachedTotalMemMB = parseFloat(r.dataObject.totalMem.sum) || 0;
				}
				callback(cachedTotalMemMB);
			},
			error: function() { callback(0); }
		});
	}

	function initDisk() {
		chartDisk = echarts.init(document.getElementById('chart_disk_usage'));
		chartDisk.setOption({
			title: { text: 'Memory Used (%)', top: 5, left: 5, textStyle: { fontSize: 14 } },
			grid: { height: 260, left: '3%', right: '4%', bottom: '15%', containLabel: true },
			tooltip: { trigger: 'axis', formatter: function(p) { return p[0].axisValueLabel + '<br/>Used: ' + p[0].data[1] + '%'; } },
			toolbox: { feature: { saveAsImage: {} } },
			dataZoom: [{ type: 'slider', start: 0, end: 100 }],
			xAxis: { type: 'time', splitLine: { show: false }, boundaryGap: false },
			yAxis: { type: 'value', name: '(%)', min: 0, max: 100, minInterval: 1 },
			series: [{ name: 'Memory Used', type: 'line', smooth: true, symbol: 'none', areaStyle: { color: 'rgba(103,194,58,0.3)' }, lineStyle: { color: '#67C23A' }, itemStyle: { color: '#67C23A' }, data: [] }]
		});
	}

	function fetchDiskInitial(limit) {
		$.ajax({
			url: monitorWebBaseURL + 'dynamic',
			data: { limit: limit },
			dataType: 'json',
			success: function (response) {
				if (!response.success) return;
				var d = response.dataObject;
				if (!d.memFree) return;
				var freeList = d.memFree.slice().reverse();
				getTotalMemMB(function(totalMemMB) {
					if (totalMemMB <= 0) return;
					for (var i = 0; i < freeList.length; i++) {
						var t   = freeList[i].processTime * 1000;
						var pct = parseFloat((100 * (totalMemMB - freeList[i].sum) / totalMemMB).toFixed(2));
						if (!t || pct < 0) continue;
						diskUsedData.push([t, pct]);
						if (diskLastTime < t) diskLastTime = t;
					}
					chartDisk.setOption({ series: [{ data: diskUsedData }] });
				});
			}
		});
	}

	function fetchDiskLatest() {
		$.ajax({
			url: monitorWebBaseURL + 'dynamic',
			data: { limit: 5, memFreeLastFetchTime: Math.floor(diskLastTime / 1000) + 1 },
			dataType: 'json',
			success: function (response) {
				if (!response.success) return;
				var d = response.dataObject;
				if (!d.memFree) return;
				var freeList = d.memFree.slice().reverse();
				getTotalMemMB(function(totalMemMB) {
					if (totalMemMB <= 0) return;
					for (var i = 0; i < freeList.length; i++) {
						var t = freeList[i].processTime * 1000;
						if (!t || t <= diskLastTime) continue;
						diskLastTime = t;
						var pct = parseFloat((100 * (totalMemMB - freeList[i].sum) / totalMemMB).toFixed(2));
						if (pct < 0) continue;
						diskUsedData.push([t, pct]);
						if (diskUsedData.length > 200) diskUsedData.shift();
					}
					chartDisk.setOption({ series: [{ data: diskUsedData }] });
				});
			}
		});
	}

	// ── 初始化 ──────────────────────────────────────────────
	$(function () {
		initRadar();
		initDisk();
		fetchRadar();
		fetchDiskInitial(50);
		setInterval(fetchRadar,      5000);
		setInterval(fetchDiskLatest, 5000);
	});

})();
