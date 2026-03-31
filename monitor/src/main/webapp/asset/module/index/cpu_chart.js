/**
 * Created by liuyou on 2017/5/21.
 */

(function () {

	var cpuChart = null;
	var cpuData = [];

	// 获取CPU数据
	function fetchCpuDataInterval() {
		if (cpuData !== null && cpuData.length !== 0) {
			$.ajax({
				type: "POST",
				url: monitorWebBaseURL + "index/cpu",
				data: {
					limit: 2,
                    start: 0
				},
				dataType: "json",
				success: function (data) {
					if (data.success) {
//						data.dataObject.reverse();
						$.each(data.dataObject, function (i, item) {
							cpuData.shift();
							cpuData.push([
								item.processTime * 1000,
								(100 - item.sum / item.num).toFixed(1)
							]);
						});

						cpuChart.setOption({
							series: {
								name: 'Avg CPU Usage',
								data: cpuData
							}
						});
					}
				}
			});
		}
	}

	$(function () {


         const cpuChartDom = document.getElementById('cpuChart');
         if (!cpuChartDom) {
//             console.error('图表容器cpuChart不存在');
             return;
         }

         // 基于准备好的dom，初始化echarts实例
         cpuChart = echarts.init(cpuChartDom);
         window._cpuChart = cpuChart;
		// 设置图表属性
		  cpuChart.setOption({
			title: {
				text: 'Cluster Average CPU Utilization',
				top: 5,
				left: 5
			},
			grid: {
				height: 165
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
				top: 5,
				right: 20
			},
			dataZoom: [
				{
					type: 'slider',
					start: 50,
					end: 100
				}
			],
			visualMap: {
				top: 10,
				right: 10,
				pieces: [{
					gt: 0,
					lte: 20,
					color: '#660099'
				}, {
					gt: 20,
					lte: 50,
					color: '#ffde33'
				}, {
					gt: 50,
					lte: 70,
					color: '#ff9933'
				}, {
					gt: 70,
					lte: 100,
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
				boundaryGap: false,
			axisLabel: {
				formatter: function (val) {
					var d = new Date(val);
					var pad = function (n) { return n < 10 ? '0' + n : n; };
					return (d.getMonth()+1) + '/' + pad(d.getDate()) + '\n' + pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds());
				}
			}
		},
			yAxis: {
				type: 'value',
				min: 0,
				max: 100,
				splitLine: {
					show: false
				}
			},
			series: [{
				name: 'Avg CPU Usage',
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
						yAxis: 20
					}, {
						yAxis: 50
					}, {
						yAxis: 70
					}, {
						yAxis: 100
					}]
				}
			}]
		});

		// 加载提示动画
		cpuChart.showLoading();
		$.ajax({
			type: "POST",
			url: monitorWebBaseURL + "index/cpu",
			data: {
				limit: 100
			},
			dataType: "json",
			success: function (data) {
				if (data.success) {
					$.each(data.dataObject, function (i, item) {
						cpuData.unshift([
							item.processTime * 1000,
							(100 - item.sum / item.num).toFixed(1)
						]);
					});

					// 关闭提示动画
					cpuChart.hideLoading();
					// 填入数据
					cpuChart.setOption({
						series: {
							name: 'Avg CPU Usage',
							data: cpuData
						}
					});
				}
				setInterval(fetchCpuDataInterval, 5000);
			}
		});
	});
})();

