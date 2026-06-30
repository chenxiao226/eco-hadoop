(function () {

	var chartDiskRead  = null;
	var chartDiskWrite = null;
	var chartNetIn     = null;
	var chartNetOut    = null;

	var diskReadData  = [];
	var diskWriteData = [];
	var netInData     = [];
	var netOutData    = [];

	var diskLastTime = 0;
	var netLastTime  = 0;

	var WIN = 3 * 60 * 1000;

	var timeAxisLabel = {
		formatter: function (val) {
			var d = new Date(val);
			var pad = function (n) { return n < 10 ? '0' + n : n; };
			return (d.getMonth()+1) + '/' + pad(d.getDate()) + '\n' +
				pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds());
		}
	};

	function makeOption(title, color) {
		return {
			title: { text: title, top: 5, left: 5, textStyle: { fontSize: 14 } },
			tooltip: { trigger: 'axis' },
			toolbox: { feature: { saveAsImage: {} } },
			dataZoom: [{ type: 'inside' }],
			grid: { height: 260, left: '3%', right: '4%', bottom: '10%', containLabel: true },
			xAxis: { type: 'time', splitLine: { show: false }, boundaryGap: false, axisLabel: timeAxisLabel },
			yAxis: { type: 'value', name: 'bytes/s', minInterval: 1 },
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

	function hexToRgb(hex) {
		var r = parseInt(hex.slice(1,3),16);
		var g = parseInt(hex.slice(3,5),16);
		var b = parseInt(hex.slice(5,7),16);
		return r + ',' + g + ',' + b;
	}

	function updateCharts() {
		var now = new Date().getTime();
		chartDiskRead.setOption({ series: [{ data: diskReadData }], xAxis: { min: now - WIN, max: now } });
		chartDiskWrite.setOption({ series: [{ data: diskWriteData }], xAxis: { min: now - WIN, max: now } });
		chartNetIn.setOption({ series: [{ data: netInData }], xAxis: { min: now - WIN, max: now } });
		chartNetOut.setOption({ series: [{ data: netOutData }], xAxis: { min: now - WIN, max: now } });
	}

	function processDiskData(d) {
		var newTime = diskLastTime;
		if (d.DISK_READ) {
			d.DISK_READ.reverse();
			$.each(d.DISK_READ, function(_, item) {
				var t = item.processTime * 1000;
				if (t <= diskLastTime) return;
				diskReadData.push([t, item.sum]);
				if (diskReadData.length > 200) diskReadData.shift();
				if (t > newTime) newTime = t;
			});
		}
		if (d.DISK_WRITE) {
			d.DISK_WRITE.reverse();
			$.each(d.DISK_WRITE, function(_, item) {
				var t = item.processTime * 1000;
				if (t <= diskLastTime) return;
				diskWriteData.push([t, item.sum]);
				if (diskWriteData.length > 200) diskWriteData.shift();
				if (t > newTime) newTime = t;
			});
		}
		diskLastTime = newTime;
	}

	function processNetData(d) {
		var newTime = netLastTime;
		if (d.BYTES_IN) {
			d.BYTES_IN.reverse();
			$.each(d.BYTES_IN, function(_, item) {
				var t = item.processTime * 1000;
				if (t <= netLastTime) return;
				netInData.push([t, item.sum]);
				if (netInData.length > 200) netInData.shift();
				if (t > newTime) newTime = t;
			});
		}
		if (d.BYTES_OUT) {
			d.BYTES_OUT.reverse();
			$.each(d.BYTES_OUT, function(_, item) {
				var t = item.processTime * 1000;
				if (t <= netLastTime) return;
				netOutData.push([t, item.sum]);
				if (netOutData.length > 200) netOutData.shift();
				if (t > newTime) newTime = t;
			});
		}
		netLastTime = newTime;
	}

	function fetchDiskInitial() {
		$.ajax({
			type: 'POST',
			url: monitorWebBaseURL + 'io/fetchDiskIO',
			data: { limit: 50 },
			dataType: 'json',
			success: function(data) {
				if (!data.success) return;
				processDiskData(data.dataObject);
				updateCharts();
			}
		});
	}

	function fetchDiskLatest() {
		$.ajax({
			type: 'POST',
			url: monitorWebBaseURL + 'io/fetchDiskIO',
			data: {
				limit: 5,
				readLastFetchTime:  Math.floor(diskLastTime / 1000) + 1,
				writeLastFetchTime: Math.floor(diskLastTime / 1000) + 1
			},
			dataType: 'json',
			success: function(data) {
				if (!data.success) return;
				processDiskData(data.dataObject);
				updateCharts();
			}
		});
	}

	function fetchNetInitial() {
		$.ajax({
			type: 'POST',
			url: monitorWebBaseURL + 'io/fetchIOByte_Pkts',
			data: { limit: 50 },
			dataType: 'json',
			success: function(data) {
				if (!data.success) return;
				processNetData(data.dataObject);
				updateCharts();
			}
		});
	}

	function fetchNetLatest() {
		$.ajax({
			type: 'POST',
			url: monitorWebBaseURL + 'io/fetchIOByte_Pkts',
			data: {
				limit: 5,
				bytesinLastFetchTime:  Math.floor(netLastTime / 1000) + 1,
				bytesoutLastFetchTime: Math.floor(netLastTime / 1000) + 1
			},
			dataType: 'json',
			success: function(data) {
				if (!data.success) return;
				processNetData(data.dataObject);
				updateCharts();
			}
		});
	}

	$(function() {
		chartDiskRead  = echarts.init(document.getElementById('diskReadChart'));
		chartDiskWrite = echarts.init(document.getElementById('diskWriteChart'));
		chartNetIn     = echarts.init(document.getElementById('netInChart'));
		chartNetOut    = echarts.init(document.getElementById('netOutChart'));

		chartDiskRead.setOption(makeOption('Disk Read (bytes/s)', '#409EFF'));
		chartDiskWrite.setOption(makeOption('Disk Write (bytes/s)', '#67C23A'));
		chartNetIn.setOption(makeOption('Network In (bytes/s)', '#E6A23C'));
		chartNetOut.setOption(makeOption('Network Out (bytes/s)', '#F56C6C'));

		fetchDiskInitial();
		fetchNetInitial();
		setInterval(fetchDiskLatest, 5000);
		setInterval(fetchNetLatest, 5000);
	});

})();
