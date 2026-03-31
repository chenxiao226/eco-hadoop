(function () {

	var BASE = '/monitor/';

	function fetchStable() {
		$.ajax({
			type: 'GET', url: BASE + 'index/stable', dataType: 'json',
			success: function (resp) {
				if (!resp || !resp.success) return;
				var d = resp.dataObject;
				if (d.cpuNum)   $('#co-cpu-num').text(d.cpuNum.sum);
				if (d.memTotal) $('#co-mem-total').text((d.memTotal.sum / 1024).toFixed(1) + ' GB');
				if (d.diskTotal) $('#co-disk-total').text(d.diskTotal.sum.toFixed(1) + ' GB');
			}
		});
	}

	function fetchLive() {
		$.ajax({
			type: 'GET', url: BASE + 'index/mass', dataType: 'json',
			success: function (resp) {
				if (!resp || !resp.success) return;
				var d = resp.dataObject;
				if (d.rpc)      $('#co-rpc').text(d.rpc.sum.toFixed(2) + ' ms');
				if (d.diskFree) $('#co-disk-free').text(d.diskFree.sum.toFixed(1) + ' GB');
			}
		});

		$.ajax({
			type: 'GET', url: BASE + 'cpu/fetchCPU_Ratio', dataType: 'json',
			success: function (resp) {
				if (!resp || !resp.success) return;
				var d = resp.dataObject;
				var latest = function (arr) { return (arr && arr.length) ? arr[0].sum : null; };
				var idle = latest(d.cpuIdle);
				var user = latest(d.cpuUser);
				var sys  = latest(d.cpuSystem);
				var wio  = latest(d.cpuWio);
				if (idle != null) $('#co-cpu-idle').text(idle.toFixed(1) + '%');
				if (user != null) $('#co-cpu-user').text(user.toFixed(1) + '%');
				if (sys  != null) $('#co-cpu-sys').text(sys.toFixed(1) + '%');
				if (wio  != null) $('#co-cpu-wio').text(wio.toFixed(1) + '%');
			}
		});

		$.ajax({
			type: 'GET', url: BASE + 'index/load', dataType: 'json',
			success: function (resp) {
				if (!resp || !resp.success) return;
				var arr = resp.dataObject;
				if (arr && arr.length) $('#co-load1').text(arr[0].sum.toFixed(2));
			}
		});

		$.ajax({
			type: 'GET', url: BASE + 'memory/dynamic?limit=1', dataType: 'json',
			success: function (resp) {
				if (!resp || !resp.success) return;
				var d = resp.dataObject;
				var memFree  = (d.memFree  && d.memFree.length)  ? d.memFree[0].sum  : null;
				var diskFree = (d.diskFree && d.diskFree.length) ? d.diskFree[0].sum : null;
				var diskTotal= (d.diskTotal&& d.diskTotal.length)? d.diskTotal[0].sum: null;
				if (memFree  != null) $('#co-mem-free').text(memFree.toFixed(1) + ' MB');
				if (diskFree != null) {
					$('#co-disk-free').text(diskFree.toFixed(1) + ' GB');
					if (diskTotal != null) {
						$('#co-disk-used').text((diskTotal - diskFree).toFixed(1) + ' GB');
					}
				}
			}
		});

		$.ajax({
			type: 'GET', url: BASE + 'memory/basic', dataType: 'json',
			success: function (resp) {
				if (!resp || !resp.success) return;
				var d = resp.dataObject;
				if (d.totalMem) {
					var total = d.totalMem.sum;
					$('#co-mem-total').text((total / 1024).toFixed(1) + ' GB');
					var freeText = $('#co-mem-free').text();
					var freeMB = parseFloat(freeText);
					if (!isNaN(freeMB)) {
						$('#co-mem-used').text(((total - freeMB) / 1024).toFixed(1) + ' GB');
					}
				}
			}
		});

		$.ajax({
			type: 'GET', url: BASE + 'cpu/fetchCPU_Proc', dataType: 'json',
			success: function (resp) {
				if (!resp || !resp.success) return;
				var d = resp.dataObject;
				var run   = (d.procRun   && d.procRun.length)   ? d.procRun[0].sum   : null;
				var total = (d.procTotal && d.procTotal.length) ? d.procTotal[0].sum : null;
				if (run   != null) $('#co-proc-run').text(run);
				if (total != null) $('#co-proc-total').text(total);
			}
		});
	}

	function fetchNodes() {
		$.ajax({
			type: 'GET', url: BASE + 'index/cluster/nodes', dataType: 'json',
			success: function (resp) {
				if (!resp || !resp.success) return;
				var d = resp.dataObject;
				if (d.totalNodes != null) $('#co-total-nodes').text(d.totalNodes);
				if (d.aliveNodes != null) $('#co-alive-nodes').text(d.aliveNodes);
			}
		});

		$.ajax({
			type: 'GET', url: BASE + 'datanode/dataNodeInf', dataType: 'json',
			success: function (data) {
				if (!data || !data.beans || !data.beans.length) return;
				var inf = data.beans[0];
				try {
					var live  = typeof inf.LiveNodes  === 'string' ? JSON.parse(inf.LiveNodes)  : (inf.LiveNodes  || {});
					var dead  = typeof inf.DeadNodes  === 'string' ? JSON.parse(inf.DeadNodes)  : (inf.DeadNodes  || {});
					var decom = typeof inf.DecomNodes === 'string' ? JSON.parse(inf.DecomNodes) : (inf.DecomNodes || {});
					$('#co-dn-live').text(Object.keys(live).length);
					$('#co-dn-dead').text(Object.keys(dead).length);
					$('#co-dn-decom').text(Object.keys(decom).length);
					$('#co-dn-pct').text(inf.PercentUsed != null ? inf.PercentUsed.toFixed(2) + '%' : '--');
				} catch (e) { console.error('dataNodeInf parse error', e); }
			}
		});
	}

	function fetchApps() {
		$.ajax({
			type: 'GET', url: BASE + 'index/app', dataType: 'json',
			success: function (resp) {
				if (!resp || !resp.success) return;
				var d = resp.dataObject;
				$('#co-running-apps').text(d.runningCount  != null ? d.runningCount  : '--');
				$('#co-app-running').text(d.runningCount   != null ? d.runningCount  : '--');
				$('#co-app-finished').text(d.finishedCount != null ? d.finishedCount : '--');
				$('#co-app-failed').text(d.failedCount     != null ? d.failedCount   : '--');
				$('#co-app-killed').text(d.killedCount     != null ? d.killedCount   : '--');
			}
		});
	}

	$(function () {
		fetchStable();
		fetchLive();
		fetchNodes();
		fetchApps();

		setInterval(fetchLive,  5000);
		setInterval(fetchNodes, 15000);
		setInterval(fetchApps,  15000);
	});

})();
