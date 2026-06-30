
(function () {

    var loadChart = null;
    var loadData = [];
    var loadDataProcessed = false; //初始载入的Load数据是否是经过处理的


	//获得Load严重性的HTML标签表示
	function getLoadSeverity(lo) {
		if(!isNaN(lo)){
			var str = '<span class="badge bg-blue">'+lo+'</span>';
			if(lo<=1){
				return str+' <span class="badge bg-green">Low</span>';
			} else if(lo>1 && lo<=2) {
				return str+' <span class="badge bg-yellow">Medium</span>';
			} else if(lo>2 && lo<3) {
				return str+' <span class="badge bg-orange">High</span>';
			} else if(lo>=3) {
				return str+' <span class="badge bg-red">Critical</span>';
			}
		}
	}

    // 获取Load数据
    function fetchLoadDataInterval() {
        if (loadData !== null && loadData.length !== 0) {
            $.ajax({
                type: "POST",
                url: monitorWebBaseURL + "index/load",
                data: {
                    limit: 2,
                    start: 0
//                    start: loadData[loadData.length - 1][0] / 1000 + 1
//                    start: Math.floor(Date.now() / 1000) - 720 * 3600
                },
                dataType: "json",
                success: function (data) {
//
//                    indexJS.cpuNum = -1;
                    if (data.success) {
//                        data.dataObject.reverse();
						if(true) {
							$.each(data.dataObject, function (i, item) {
								loadData.shift();
								loadData.push([
									item.processTime * 1000,
									parseFloat(parseFloat(item.sum).toFixed(3))
								]);
							});
                        loadChart.setOption({
                            series: {
                                name: 'Load',
                                data: loadData
                            }
                        });
                        $("#loadNum").html(getLoadSeverity(parseFloat(loadData[loadData.length-1][1]).toFixed(3)));
						}


                    }
                }
            });
        }
    }

    $(function () {

        // 基于准备好的dom，初始化echarts实例
//        loadChart = echarts.init(document.getElementById('loadChart'));
        const loadChartDom = document.getElementById('loadChart');
        loadChart = echarts.init(loadChartDom);
        window._loadChart = loadChart;
        // 设置图表属性
        loadChart.setOption({
            title: {
                text: 'Cluster Load',
                top: 5,
                left: 5
            },
            grid:{
                height: 185
            },
            tooltip: {
                trigger: 'axis'
            },
            toolbox: {
                feature: {
                    dataZoom: {
                        yAxisIndex: 'none'
                    },
                    restore: {},
                    saveAsImage: {}
                },
				right: 20,
				top: 5
            },
            dataZoom: [
                {   // 这个dataZoom组件，默认控制x轴。
                    type: 'slider', // 这个 dataZoom 组件是 slider 型 dataZoom 组件
                    start: 50,      // 左边在 10% 的位置。
                    end: 100         // 右边在 60% 的位置。
                }
            ],
            visualMap: {
                top: 10,
                right: 10,
                pieces: [{
                    gt: 0,
                    lte: 1,
                    color: '#660099'
                }, {
                    gt: 1,
                    lte: 1.5,
                    color: '#ffde33'
                }, {
                    gt: 1.5,
                    lte: 2,
                    color: '#ff9933'
                }, {
                    gt: 2,
                    lte: 2.5,
                    color: '#ff0000'
                }],
                outOfRange: {
                    color: '#999'
                },
                showLabel: false,
                show: false
            },
            xAxis: {
                type: 'time',
                splitLine: {
                    show: false
                },
                boundaryGap : false
            },
            yAxis: {
                type: 'value',
                min: 0,
				max: 3
            },
            series: [{
                name: 'Load',
                type: 'line',
                data: [],
                smooth: true,
                symbol: 'none',
                areaStyle: {
                    normal: {}
                },
                markLine: {
                    silent: true,
                    data: [{
                        yAxis: 1
                    }, {
                        yAxis: 1.5
                    }, {
                        yAxis: 2
                    }, {
                        yAxis: 2.5
                    }]
                }
            }]
        });

        // 加载提示动画
        loadChart.showLoading();
        $.ajax({
            type: "POST",
            url: monitorWebBaseURL + "index/load",
            data: {
                limit:100
            },
            dataType: "json",
            success: function (data) {
                if (data.success) {

                    $.each(data.dataObject, function (i, item) {
							loadData.unshift([
								item.processTime * 1000,
								parseFloat(parseFloat(item.sum).toFixed(3))
							]);
						});

						loadChart.hideLoading();


						// 填入数据
                        loadChart.setOption({
						series: {
							name: '平均使用率',
							data: loadData
						    }
                        });
						$("#loadNum").html(getLoadSeverity(parseFloat(loadData[loadData.length-1][1]).toFixed(3)));

                }//大的if
                setInterval(fetchLoadDataInterval, 5000);
            }
        });
    });
})();
