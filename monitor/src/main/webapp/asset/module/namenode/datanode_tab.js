(function () {

	var NAMENODE_JMX = 'http://10.168.1.101:9870';
	var DN_BASE = '/monitor/datanode/';

	var chartBytesRead    = null;
	var chartBytesWritten = null;
	var chartBlocksRead   = null;
	var chartBlocksWritten = null;
	var chartRadar        = null;

	var bytesReadData    = [];
	var bytesWrittenData = [];
	var blocksReadData   = [];
	var blocksWrittenData = [];

	var bytesReadLast    = 0;
	var bytesWrittenLast = 0;
	var blocksReadLast   = 0;
	var blocksWrittenLast = 0;

	function makeAreaOption(title, yName, color) {
		return {
			title: { text: title, top: 5, left: 5, textStyle: { fontSize: 13 } },
			tooltip: { trigger: 'axis' },
			toolbox: { feature: { saveAsImage: {} } },
			dataZoom: [{ type: 'slider', start: 50, end: 100 }],
			grid: { height: 220, left: '3%', right: '4%', bottom: '20%', containLabel: true },
			xAxis: { type: 'time', splitLine: { show: false }, boundaryGap: false },
			yAxis: { type: 'value', name: yName, minInterval: 1 },
			series: [{
				name: title,
				type: 'line',
				smooth: true,
				symbol: 'none',
				areaStyle: { color: color + '33' },
				lineStyle: { color: color },
				itemStyle: { color: color },
				data: []
			}]
		};
	}

	function parseList(list, lastTime) {
		if (!list || list.length === 0) return { points: [], newLastTime: lastTime };
		var pts = [];
		var newLast = lastTime;
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

	// Fetch DataNode time-series from DB (bytes/blocks via DataNodeController)
	function fetchDbData(limit) {
		var params = { limit: limit };
		if (bytesReadLast    > 0) params.bytesReadStart    = Math.floor(bytesReadLast    / 1000) + 1;
		if (bytesWrittenLast > 0) params.bytesWrittenStart = Math.floor(bytesWrittenLast / 1000) + 1;
		if (blocksReadLast   > 0) params.blocksReadStart   = Math.floor(blocksReadLast   / 1000) + 1;
		if (blocksWrittenLast > 0) params.blocksWrittenStart = Math.floor(blocksWrittenLast / 1000) + 1;

		$.ajax({
			type: 'GET',
			url: DN_BASE + 'dynamic',
			data: params,
			dataType: 'json',
			success: function (resp) {
				if (!resp || !resp.success) return;
				var d = resp.dataObject;

				var r1 = parseList(d.bytesRead,    bytesReadLast);
				pushPoints(bytesReadData, r1.points);
				bytesReadLast = r1.newLastTime;

				var r2 = parseList(d.bytesWritten, bytesWrittenLast);
				pushPoints(bytesWrittenData, r2.points);
				bytesWrittenLast = r2.newLastTime;

				var r3 = parseList(d.blocksRead,   blocksReadLast);
				pushPoints(blocksReadData, r3.points);
				blocksReadLast = r3.newLastTime;

				var r4 = parseList(d.blocksWritten, blocksWrittenLast);
				pushPoints(blocksWrittenData, r4.points);
				blocksWrittenLast = r4.newLastTime;

				chartBytesRead.setOption({ series: [{ data: bytesReadData }] });
				chartBytesWritten.setOption({ series: [{ data: bytesWrittenData }] });
				chartBlocksRead.setOption({ series: [{ data: blocksReadData }] });
				chartBlocksWritten.setOption({ series: [{ data: blocksWrittenData }] });
			}
		});
	}

	// Fetch DataNode cluster info from NameNode JMX (live/dead counts + radar)
	function fetchNodeInf() {
		$.ajax({
			type: 'GET',
			url: DN_BASE + 'dataNodeInf',
			dataType: 'json',
			success: function (data) {
				if (!data || !data.beans || data.beans.length === 0) return;
				var inf = data.beans[0];
				try {
					var liveNodes  = typeof inf.LiveNodes  === 'string' ? JSON.parse(inf.LiveNodes)  : (inf.LiveNodes  || {});
					var deadNodes  = typeof inf.DeadNodes  === 'string' ? JSON.parse(inf.DeadNodes)  : (inf.DeadNodes  || {});
					var decomNodes = typeof inf.DecomNodes === 'string' ? JSON.parse(inf.DecomNodes) : (inf.DecomNodes || {});

					var liveCount  = Object.keys(liveNodes).length;
					var deadCount  = Object.keys(deadNodes).length;
					var decomCount = Object.keys(decomNodes).length;
					var pct        = inf.PercentUsed != null ? (Math.floor(inf.PercentUsed * 100) / 100) + '%' : '--';

					$('#dn-live').text(liveCount);
					$('#dn-dead').text(deadCount);
					$('#dn-decom').text(decomCount);
					$('#dn-pct').text(pct);

					// Build radar
					var indicator = [];
					var used = [];
					$.each(liveNodes, function (key, val) {
						var cap = val.capacity || 1;
						var gb = function(b) { return (b / (1024*1024*1024)).toFixed(1) + ' GB'; };
						indicator.push({ name: key + ' (' + gb(cap) + ')', max: cap });
						used.push(val.used || 0);
					});

					if (indicator.length > 0) {
						chartRadar.setOption({
							radar: [{ indicator: indicator, shape: 'circle' }],
							series: [{ data: [{ value: used, name: 'Used' }] }]
						});
					}
				} catch (e) {}
			}
		});
	}

	var _initialized = false;

	window.DatanodeTab = {
		init: function () {
			if (_initialized) return;
			_initialized = true;

			chartBytesRead    = echarts.init(document.getElementById('dnBytesReadChart'));
			chartBytesWritten = echarts.init(document.getElementById('dnBytesWrittenChart'));
			chartBlocksRead   = echarts.init(document.getElementById('dnBlocksReadChart'));
			chartBlocksWritten = echarts.init(document.getElementById('dnBlocksWrittenChart'));
			chartRadar        = echarts.init(document.getElementById('dnRadarChart'));

			chartBytesRead.setOption(makeAreaOption('DataNode Bytes Read (cumulative)', 'bytes', '#409EFF'));
			chartBytesWritten.setOption(makeAreaOption('DataNode Bytes Written (cumulative)', 'bytes', '#67C23A'));
			chartBlocksRead.setOption(makeAreaOption('DataNode Blocks Read (cumulative)', 'count', '#E6A23C'));
			chartBlocksWritten.setOption(makeAreaOption('DataNode Blocks Written (cumulative)', 'count', '#F56C6C'));

			chartRadar.setOption({
				title: { text: 'Cluster Disk Usage (per DataNode)', top: 5, left: 5, textStyle: { fontSize: 13 } },
				tooltip: { trigger: 'item' },
				legend: { data: ['Used'], bottom: 5 },
				radar: [{ indicator: [], shape: 'circle' }],
				series: [{
					name: 'Disk Usage',
					type: 'radar',
					itemStyle: { normal: { areaStyle: { type: 'default' } } },
					data: [{ value: [], name: 'Used' }]
				}]
			});

			fetchDbData(50);
			fetchNodeInf();

			setInterval(function () { fetchDbData(10); }, 5000);
			setInterval(fetchNodeInf, 15000);
		}
	};

})();
