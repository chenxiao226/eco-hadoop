(function () {

	var BASE = monitorWebBaseURL + 'namenode/';

	// Chart instances
	var chartGC      = null;
	var chartHeap    = null;
	var chartRpc     = null;
	var chartHDFS    = null;

	// Time-series data arrays [timestamp_ms, value]
	var gcData      = [];
	var heapData    = [];
	var rpcData     = [];
	var hdfsUsedData = [];

	// Last-seen processTime for incremental fetching
	var gcLastTime     = 0;
	var heapLastTime   = 0;
	var rpcLastTime    = 0;
	var hdfsLastTime   = 0;

	// ----------------------------------------------------------------
	// Chart factory
	// ----------------------------------------------------------------
	var WIN = 3 * 60 * 1000;

	var timeAxisLabel = {
		formatter: function (val) {
			var d = new Date(val);
			var pad = function (n) { return n < 10 ? '0' + n : n; };
			return (d.getMonth()+1) + '/' + pad(d.getDate()) + '\n' +
				pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds());
		}
	};

	function hexToRgb(hex) {
		var r = parseInt(hex.slice(1,3),16);
		var g = parseInt(hex.slice(3,5),16);
		var b = parseInt(hex.slice(5,7),16);
		return r + ',' + g + ',' + b;
	}

	function makeAreaOption(title, yName, color) {
		return {
			title: { text: title, top: 5, left: 'center', textStyle: { fontSize: 13 } },
			tooltip: { trigger: 'axis' },
			toolbox: { feature: { saveAsImage: {} }, right: 10, top: 5 },
			dataZoom: [{ type: 'inside' }],
			grid: { left: '3%', right: '4%', top: 50, bottom: '8%', containLabel: true },
			xAxis: { type: 'time', splitLine: { show: false }, boundaryGap: false, axisLabel: timeAxisLabel },
			yAxis: { type: 'value', name: yName, nameGap: 8, minInterval: 1 },
			series: [{
				name: title,
				type: 'line',
				smooth: true,
				symbol: 'none',
				areaStyle: { color: 'rgba(' + hexToRgb(color) + ',0.2)' },
				lineStyle: { color: color },
				itemStyle: { color: color },
				data: []
			}]
		};
	}

	// ----------------------------------------------------------------
	// Parse raw list from /namenode/dynamic into [ms, value] pairs
	// ----------------------------------------------------------------
	function parseList(list, lastTime) {
		if (!list || list.length === 0) return { points: [], newLastTime: lastTime };
		var pts = [];
		var newLast = lastTime;
		// list comes back desc; reverse to asc
		var asc = list.slice().reverse();
		$.each(asc, function (_, item) {
			var t = item.processTime * 1000;
			if (t <= lastTime) return;
			pts.push([t, item.sum]);
			if (t > newLast) newLast = t;
		});
		return { points: pts, newLastTime: newLast };
	}

	function pushPoints(arr, pts) {
		$.each(pts, function (_, p) { arr.push(p); });
		while (arr.length > 300) arr.shift();
	}

	// ----------------------------------------------------------------
	// Info-box refresh
	// ----------------------------------------------------------------
	function updateSquares(d) {
		if (d.capacityTotal != null) {
			$('#hdfsTotal').text(d.capacityTotal.toFixed(1) + ' GB');
		}
		if (d.capacityRemaining != null) {
			$('#hdfsRemaining').text(d.capacityRemaining.toFixed(1) + ' GB');
		}
		if (d.capacityTotal != null && d.capacityRemaining != null) {
			var used = d.capacityTotal - d.capacityRemaining;
			$('#hdfsUsed').text(used.toFixed(1) + ' GB');
			if (d.capacityTotal > 0) {
				$('#hdfsPercent').text((used / d.capacityTotal * 100).toFixed(1) + '%');
			}
		}
	}

	// ----------------------------------------------------------------
	// Fetch & update
	// ----------------------------------------------------------------
	function fetchData(limit) {
		var params = { limit: limit };
		if (gcLastTime   > 0) params.gcTimeStart   = Math.floor(gcLastTime   / 1000) + 1;
		if (heapLastTime > 0) params.heapUsedStart = Math.floor(heapLastTime / 1000) + 1;
		if (rpcLastTime  > 0) params.rpcStart      = Math.floor(rpcLastTime  / 1000) + 1;
		if (hdfsLastTime > 0) params.hdfsUsedStart = Math.floor(hdfsLastTime / 1000) + 1;

		$.ajax({
			type: 'GET',
			url: BASE + 'dynamic',
			data: params,
			dataType: 'json',
			success: function (resp) {
				if (!resp || !resp.success) return;
				var d = resp.dataObject;

				// GC Time
				var gcRes = parseList(d.gcTime, gcLastTime);
				pushPoints(gcData, gcRes.points);
				gcLastTime = gcRes.newLastTime;

				// Heap Used
				var heapRes = parseList(d.heapUsed, heapLastTime);
				pushPoints(heapData, heapRes.points);
				heapLastTime = heapRes.newLastTime;

				// RPC
				var rpcRes = parseList(d.rpcTime, rpcLastTime);
				pushPoints(rpcData, rpcRes.points);
				rpcLastTime = rpcRes.newLastTime;

				// HDFS Used (time series from m_disk_free — remaining capacity)
				var hdfsRes = parseList(d.hdfsUsed, hdfsLastTime);
				pushPoints(hdfsUsedData, hdfsRes.points);
				hdfsLastTime = hdfsRes.newLastTime;

				// Update charts
				var now = new Date().getTime();
				chartGC.setOption({ series: [{ data: gcData }], xAxis: { min: now - WIN, max: now } });
				chartHeap.setOption({ series: [{ data: heapData }], xAxis: { min: now - WIN, max: now } });
				chartRpc.setOption({ series: [{ data: rpcData }], xAxis: { min: now - WIN, max: now } });
				chartHDFS.setOption({ series: [{ data: hdfsUsedData }], xAxis: { min: now - WIN, max: now } });

				updateSquares(d);
			}
		});
	}

	// ----------------------------------------------------------------
	// Init
	// ----------------------------------------------------------------
	$(function () {
		chartGC   = echarts.init(document.getElementById('gcTimeChart'));
		chartHeap = echarts.init(document.getElementById('heapUsedChart'));
		chartRpc  = echarts.init(document.getElementById('rpcChart'));
		chartHDFS = echarts.init(document.getElementById('hdfsUsedChart'));

		chartGC.setOption(makeAreaOption('NameNode GC Time (ms)', 'ms', '#E6A23C'));
		chartHeap.setOption(makeAreaOption('Heap Memory Used (MB)', 'MB', '#409EFF'));
		chartRpc.setOption(makeAreaOption('RPC Processing Avg Time (ms)', 'ms', '#67C23A'));
		chartHDFS.setOption(makeAreaOption('HDFS Disk Free (GB)', 'GB', '#F56C6C'));

		// Ensure charts fill their containers after layout settles
		setTimeout(function () {
			chartGC.resize(); chartHeap.resize();
			chartRpc.resize(); chartHDFS.resize();
		}, 100);

		window.addEventListener('resize', function () {
			chartGC.resize(); chartHeap.resize();
			chartRpc.resize(); chartHDFS.resize();
		});

		fetchData(50);
		setInterval(function () { fetchData(10); }, 5000);
	});

})();

// Keep backward compat: old inline script calls Namenode.init()
var Namenode = { init: function () {} };
