/**
 * Created by liuyou on 2017/5/21.
 */

(function () {

	var appChart = null;
	var mapReduceRunningData = [];
	var sparkRunningData = [];

	// 获取应用运行数据
	function fetchAppDataInterval() {
		 $.ajax({
            type: "POST",
            url: monitorWebBaseURL + "index/app",
            data: {},
            dataType: "json",
            success: function (data) {
                if (data && data.dataObject) {
                    var now = new Date().getTime();
                    var mrRunning = data.dataObject.runningCount || 0;
                    var mrFinished = data.dataObject.finishedCount || 0;
                    var mrFailed = data.dataObject.failedCount || 0;
                    var mrKilled = data.dataObject.killedCount || 0;

                    // 更新MapReduce数据点
                    if (mapReduceRunningData.length >= 200) {
                        mapReduceRunningData.shift();
                    }
                    mapReduceRunningData.push([now, mrRunning]);

                    // 更新图表
                    appChart.setOption({
                        series: [{
                            name: 'Running Map Reduce Application',
                            data: mapReduceRunningData
                        }]
                    });

                    // 更新统计数字
                    $("#mrRunningNum").html(mrRunning);
                    $("#mrFinishedNum").html(mrFinished);
                    $("#mrFailedNum").html(mrFailed);
                    $("#mrKilledNum").html(mrKilled);

                }
            }
        });
	}

	$(function () {

		// 基于准备好的dom，初始化echarts实例
		const appChartDom = document.getElementById('applicationChart');
		appChart=echarts.init(appChartDom);


		// 设置图表属性
		appChart.setOption({
			title: {
				text: 'Running Map Reduce Application',
				top: 5,
				left: 5
			},
			grid: {
				height: 300,
				left: '3%',
				right: '4%',
				containLabel: true
			},
			tooltip: {
				trigger: 'axis',
				axisPointer: {
					type: 'cross',
					label: {
						backgroundColor: '#6a7985'
					}
				}
			},
			legend: {
				data: ['Running Map Reduce Application', 'Running Spark Application'],
				top: 10
			},
			toolbox: {
				feature: {
					dataZoom: {
						yAxisIndex: 'none'
					},
					restore: {},
					dataView: {},
					saveAsImage: {}
				},
				right: 30,
				top: 5
			},
			dataZoom: [
				{   // 这个dataZoom组件，默认控制x轴。
					type: 'slider', // 这个 dataZoom 组件是 slider 型 dataZoom 组件
					start: 0,      // 左边在 10% 的位置。
					end: 100,         // 右边在 60% 的位置。
				}
			],
			xAxis: {
				type: 'time',
				splitLine: {
					show: false
				},
				boundaryGap: false
			},
			yAxis: {
				type: 'value',
				min: 0,
				max:20,
				minInterval: 1
			},
			series: [
				{
					name: 'Running Map Reduce Application',
					type: 'line',
					areaStyle: {normal: {}},
					data: [],
					symbol: 'none'
				},
				{
					name: 'Running Spark Application',
					type: 'line',
					areaStyle: {normal: {}},
					data: [],
					symbol: 'none'
				},
			]
		});

		// 加载提示动画
		appChart.showLoading();

		fetchAppDataInterval();

		// 关闭提示动画
		appChart.hideLoading();

		setInterval(fetchAppDataInterval, 8000);
	});
})();

