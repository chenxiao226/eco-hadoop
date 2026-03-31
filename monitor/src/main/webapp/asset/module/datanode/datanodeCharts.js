(function () {

	var DN_BASE = '/monitor/datanode/';

	var chartBytes  = null;
	var chartBlocks = null;
	var chartAvg    = null;
	var chartBar    = null;

	var bytesReadData     = [];
	var bytesWrittenData  = [];
	var blocksReadData    = [];
	var blocksWrittenData = [];
	var readAvgData       = [];
	var writeAvgData      = [];

	var bytesReadLast     = 0;
	var bytesWrittenLast  = 0;
	var blocksReadLast    = 0;
	var blocksWrittenLast = 0;
	var readAvgLast       = 0;
	var writeAvgLast      = 0;

	function makeDualOption(title, name1, color1, name2, color2) {
		return {
			title: { text: title, top: 5, left: 'center', textStyle: { fontSize: 13 } },
			tooltip: { trigger: 'axis' },
			legend: { data: [name1, name2], top: 30 },
			toolbox: { feature: { saveAsImage: {} }, right: 10, top: 5 },
			dataZoom: [{ type: 'inside' }],
			grid: { left: '3%', right: '4%', top: 70, bottom: '5%', containLabel: true },
			xAxis: { type: 'time', splitLine: { show: false }, boundaryGap: false },
			yAxis: { type: 'value', minInterval: 1 },
			series: [
				{
					name: name1, type: 'line', smooth: true, symbol: 'none',
					areaStyle: { color: color1 + '33' },
					lineStyle: { color: color1 }, itemStyle: { color: color1 },
					data: []
				},
				{
					name: name2, type: 'line', smooth: true, symbol: 'none',
					areaStyle: { color: color2 + '33' },
					lineStyle: { color: color2 }, itemStyle: { color: color2 },
					data: []
				}
			]
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

	function fetchDbData(limit) {
		var params = { limit: limit };
		if (bytesReadLast     > 0) params.bytesReadStart     = Math.floor(bytesReadLast     / 1000) + 1;
		if (bytesWrittenLast  > 0) params.bytesWrittenStart  = Math.floor(bytesWrittenLast  / 1000) + 1;
		if (blocksReadLast    > 0) params.blocksReadStart    = Math.floor(blocksReadLast    / 1000) + 1;
		if (blocksWrittenLast > 0) params.blocksWrittenStart = Math.floor(blocksWrittenLast / 1000) + 1;
		if (readAvgLast       > 0) params.readAvgStart       = Math.floor(readAvgLast       / 1000) + 1;
		if (writeAvgLast      > 0) params.writeAvgStart      = Math.floor(writeAvgLast      / 1000) + 1;

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

				var r5 = parseList(d.readAvgTime,  readAvgLast);
				pushPoints(readAvgData, r5.points);
				readAvgLast = r5.newLastTime;

				var r6 = parseList(d.writeAvgTime, writeAvgLast);
				pushPoints(writeAvgData, r6.points);
				writeAvgLast = r6.newLastTime;

				var WIN = 1 * 60 * 1000;
				var endB  = bytesReadLast  || bytesWrittenLast;
				var endBl = blocksReadLast || blocksWrittenLast;
				var endA  = readAvgLast    || writeAvgLast;
				chartBytes.setOption({ series: [{ data: bytesReadData }, { data: bytesWrittenData }], xAxis: { min: endB  - WIN, max: endB  } });
				chartBlocks.setOption({ series: [{ data: blocksReadData }, { data: blocksWrittenData }], xAxis: { min: endBl - WIN, max: endBl } });
				chartAvg.setOption({ series: [{ data: readAvgData }, { data: writeAvgData }], xAxis: { min: endA  - WIN, max: endA  } });
			}
		});
	}

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

					$('#dn-live').text(Object.keys(liveNodes).length);
					$('#dn-dead').text(Object.keys(deadNodes).length);
					$('#dn-decom').text(Object.keys(decomNodes).length);
					$('#dn-pct').text(inf.PercentUsed != null ? inf.PercentUsed.toFixed(2) + '%' : '--');

					var labels = [], usedVals = [], remainVals = [];
					var toGB = function (b) { return parseFloat((b / (1024 * 1024 * 1024)).toFixed(1)); };
					$.each(liveNodes, function (key, val) {
						var cap    = val.capacity || 0;
						var usedB  = val.usedSpace || val.used || 0;
						var remB   = val.remaining || (cap - usedB);
						labels.push(key + '\n(' + toGB(cap) + ' GB)');
						usedVals.push(toGB(usedB));
						remainVals.push(toGB(remB));
					});

					if (labels.length > 0) {
						chartBar.setOption({
							yAxis: [{ data: labels }],
							series: [{ data: usedVals }, { data: remainVals }]
						});
					}
				} catch (e) { console.error('fetchNodeInf parse error', e); }
			}
		});
	}

	$(function () {
		chartBytes  = echarts.init(document.getElementById('dnBytesChart'));
		chartBlocks = echarts.init(document.getElementById('dnBlocksChart'));
		chartAvg    = echarts.init(document.getElementById('dnAvgChart'));
		chartBar    = echarts.init(document.getElementById('dnBarChart'));

		chartBytes.setOption(makeDualOption('DataNode Bytes Read / Written', 'Bytes Read', '#409EFF', 'Bytes Written', '#67C23A'));
		chartBlocks.setOption(makeDualOption('DataNode Blocks Read / Written', 'Blocks Read', '#E6A23C', 'Blocks Written', '#F56C6C'));
		chartAvg.setOption(makeDualOption('Block Op Avg Time (ms)', 'Read Avg (ms)', '#9B59B6', 'Write Avg (ms)', '#1ABC9C'));

		chartBar.setOption({
			title: { text: 'Cluster Disk Usage per DataNode (GB)', top: 5, left: 'center', textStyle: { fontSize: 13 } },
			tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
			legend: { data: ['Used (GB)', 'Remaining (GB)'], top: 30 },
			grid: { left: '3%', right: '8%', top: 65, bottom: '5%', containLabel: true },
			xAxis: { type: 'value', name: 'GB' },
			yAxis: [{ type: 'category', data: ['loading...'] }],
			series: [
				{
					name: 'Used (GB)', type: 'bar', stack: 'disk',
					itemStyle: { color: '#F56C6C' },
					label: { show: true, position: 'inside', formatter: '{c} GB' },
					data: [0]
				},
				{
					name: 'Remaining (GB)', type: 'bar', stack: 'disk',
					itemStyle: { color: '#67C23A' },
					label: { show: true, position: 'inside', formatter: '{c} GB' },
					data: [0]
				}
			]
		});

		setTimeout(function () {
			chartBytes.resize(); chartBlocks.resize();
			chartAvg.resize(); chartBar.resize();
		}, 100);

		window.addEventListener('resize', function () {
			setTimeout(function () {
				chartBytes.resize(); chartBlocks.resize();
				chartAvg.resize(); chartBar.resize();
			}, 100);
		});

		fetchDbData(50);
		fetchNodeInf();

		setInterval(function () { fetchDbData(10); }, 5000);
		setInterval(fetchNodeInf, 15000);
	});

})();
