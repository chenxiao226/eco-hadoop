//定义了一个indexJS对象
var indexJS = {

	func: function () {
		var lastRpcFetchTime = 0;
		var lastDiskFetchTime = 0;
		var lastMemFetchTime = 0;
		var diskTotal = 0;
		var memTotal = 0;


        function getNodes(){
            $.ajax({
            		type: "POST",

            		url:monitorWebBaseURL + "index/cluster/nodes",
            		data: {},
            		dataType: "json",
            	    success: function(data) {
                        // 添加多层空值检查
                        if (!data || data === undefined || data === null) {
                            console.error("API返回空数据");
                            return;
                        }

                        // 检查totalNodes是否存在
                        var totalNodes = data.totalNodes || 0;  // 提供默认值
                        var aliveNodes = data.aliveNodes || 0;
                    $("#yarnNodeField").html(totalNodes);
                    }
            });
         }
		var lastBytesInTime = 0;
		var lastBytesOutTime = 0;

		function fetchIOAndNetwork() {
			$.ajax({
				type: "POST",
				url: monitorWebBaseURL + "io/fetchIOByte_Pkts",
				data: {
					limit: 1,
					bytesinLastFetchTime: lastBytesInTime + 1,
					bytesoutLastFetchTime: lastBytesOutTime + 1,
					pktsinLastFetchTimeStr: 0,
					pktsoutLastFetchTimeStr: 0
				},
				dataType: "json",
				success: function (data) {
					if (data.success && data.dataObject) {
						var bytesIn = data.dataObject.BYTES_IN[0];
						var bytesOut = data.dataObject.BYTES_OUT[0];
						if (bytesIn && !isNaN(bytesIn.sum)) {
							$("#diskReadField").html((bytesIn.sum / 1024).toFixed(1) + ' <small>MB</small>');
							lastBytesInTime = bytesIn.processTime;
						}
						if (bytesOut && !isNaN(bytesOut.sum)) {
							$("#networkInField").html((bytesOut.sum / 1024).toFixed(1) + ' <small>MB</small>');
							lastBytesOutTime = bytesOut.processTime;
						}
					}
				}
			});
		}

		// 获取RPC、磁盘、内存数据
		function fetchMassDataInterval() {
			$.ajax({
				type: "POST",
				url: monitorWebBaseURL + "index/mass",
				data: {
					lastRpcFetchTime: lastRpcFetchTime + 1,
					lastDiskFetchTime: lastDiskFetchTime + 1,
					lastMemFetchTime: lastMemFetchTime + 1,
				},
				dataType: "json",
				success: function (data) {
					if (data.success) {
						var rpcSum = data.dataObject.rpc.sum;
						var rpcNum = data.dataObject.rpc.num;
						var diskFree = data.dataObject.diskFree.sum;
						var memFree = data.dataObject.memFree.sum;

						//更新页面指标信息
//						if (!isNaN(rpcSum) && rpcNum !== 0) {
//							$("#rpcDelayField").html((rpcSum / rpcNum).toFixed(1) + ' <small>ms</small>');
//						}
						if (!isNaN(diskFree) && diskTotal !== 0) {
							$("#diskRateField").html((100.0 * (diskTotal - diskFree) / diskTotal).toFixed(1) + '%');
						}
						if (!isNaN(memFree) && memTotal !== 0) {
							$("#memRateField").html((100.0 * (memTotal - memFree) / memTotal).toFixed(1) + '%');
						}

						//更新数据获取时间
						lastRpcFetchTime = data.dataObject.rpc.processTime;
						lastDiskFetchTime = data.dataObject.diskFree.processTime;
						lastMemFetchTime = data.dataObject.memFree.processTime;
					}
				}
			});
		}

       var diskTotal = 0, memTotal = 0;
		$(function () {
			// 获取一些一段时间内基本不会改变的指标
			$.ajax({
				type: "POST",
				url: monitorWebBaseURL + "index/stable",
				data: {},
				dataType: "json",
				success: function (data) {
					if (data.success) {
						if (!isNaN(data.dataObject.diskTotal.sum)) {
							diskTotal = data.dataObject.diskTotal.sum;
						}
						if (!isNaN(data.dataObject.memTotal.sum)) {
							memTotal = data.dataObject.memTotal.sum;
						}
//						if (!isNaN(data.dataObject.cpuNum.sum)) {
//							indexJS.cpuNum = data.dataObject.cpuNum.sum;
//						}

						//先执行一次，否则会有一定的延迟
						fetchMassDataInterval();
                        getNodes();
						fetchIOAndNetwork();
						//开始此页面的定时任务
						setTimeout(function () {
							setInterval(getNodes, 8000);
						},4000);
						setInterval(fetchMassDataInterval, 3000);
						setInterval(fetchIOAndNetwork, 3000);
					}
				}
			});

			$.ajax({
				type: "POST",
				url: monitorWebBaseURL + "alert/trigger/fetch",
				data: {
					limit:10
				},
				dataType: "json",
				success: function (data) {
					if (data.success) {
						var result = data.result;
						var triggerAlertL = "";
						$.each(result, function (i, item) {
							var alertTime = new Date(item.time).customFormat("#YYYY# / #MM# / #DD#");

							var severityColor;
							if(item.severity.toUpperCase() == 'LOW')
								severityColor = "bg-yellow";
							else if(item.severity.toUpperCase() == 'AVERAGE')
								severityColor = "bg-orange";
							else
								severityColor = "bg-red";

							var itemDes = item.triggerDes;
							if(itemDes.length>30){
								itemDes = itemDes.substring(0,30) + '...';
							}

							triggerAlertL += `
								<li class="list-group-item" data-id="1" data-des="'+item.triggerDes+'" data-host="'+item.host+'" data-detail="'+item.info+'">\
									<span class="badge">'+alertTime+'</span>'+itemDes+'\
									<span class="badge '+severityColor+'">'+item.severity+'</span>\
								</li>`;
						});
						$("#triggerAlertList").html(triggerAlertL);
						$("#alertBox").find(".overlay").hide();
					}
				}
			});

			$(".alertBlock .list-group").on("click", ".list-group-item", function () {
				var alertContent = $(this).data("detail");
				var host = $(this).data("host");
				var des = $(this).data("des");
				$("#alertDetailContent").html(alertContent);
				$("#alertContentModal").find(".modal-title").html(host+": "+des);
				$('#alertContentModal').modal({
					"backdrop": false
				});
			});
		});

		$("#yarnNodeInfoBox").click(function () {
			window.location = monitorAssetBaseURL + "module/neo4j/neo4j.html";
		});

		$("#yarnAppInfoBox").click(function () {
			window.location = monitorAssetBaseURL + "module/yarn/yarn.html";

		});

		$("#diskInfoBox").click(function () {
			window.location = monitorAssetBaseURL + "module/io/io.html";
		});

		$("#memInfoBox").click(function () {
			window.location = monitorAssetBaseURL + "module/memory/memory.html";
		});
	}
};
indexJS.func();

// 联动三个图表的 dataZoom
$(window).on('load', function () {
    setTimeout(function () {
        var charts = [window._cpuChart, window._loadChart, window._powerChart].filter(Boolean);
        if (charts.length > 1) echarts.connect(charts);
    }, 3000);
});