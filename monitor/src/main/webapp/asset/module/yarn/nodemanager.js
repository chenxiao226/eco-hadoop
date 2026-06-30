/**
 * Created by likeugo on 2017/6/4
 */

(function() {

	var nodeManagerChart = null;

	var NUM_ACTIVENMS = [];// 已用
	var NUM_LOSTNMS = []; // 挂掉

	var _max = 0;
    var monitorWebBaseURL = "/monitor/";
	// 获取应用运行数据
	function fetchmemDataInterval(limit) {
		if(limit==null){
			limit=1;
		}
		$.ajax({
			type : "POST",
			url : monitorWebBaseURL + "yarn/getNodeManagerActiveAndLost",
			data : {
				'limit' : limit
			},
			dataType : "json",
			success : function(data) {
				if (data != null) {
					var temp_NUM_ACTIVENMS = data.NUM_ACTIVENMS;
					temp_NUM_ACTIVENMS.reverse();// 数组反转
					$.each(temp_NUM_ACTIVENMS, function(i, item) {
						NUM_ACTIVENMS
								.push([ item.processTime * 1000, item.sum ]);
						if (_max < item.sum) {
							_max = parseInt(item.sum)+2;
						}
					});
					var temp_NUM_LOSTNMS = data.NUM_LOSTNMS;
					temp_NUM_LOSTNMS.reverse();// 数组反转
					$.each(temp_NUM_LOSTNMS, function(i, item) {
						NUM_LOSTNMS.push([
								temp_NUM_ACTIVENMS[i].processTime * 1000,
								item.sum ]);// 这个位置为了时间的同步性
						if (_max < item.sum) {
							_max = parseInt(item.sum)+2;
						}
					});

					if (NUM_LOSTNMS.length >= 20) {
						NUM_LOSTNMS.shift(); // 删除第一个
						NUM_ACTIVENMS.shift(); // 删除第一个
					}

					// 填入数据
					nodeManagerChart.setOption({
						yAxis : {
							max : _max

						},
						series : [ {
							name : '活跃',
							data : NUM_ACTIVENMS
						}, {
							name : '挂掉',
							data : NUM_LOSTNMS
						} ]
					});
				}
			}
		});
	}

	$(function() {

		// 基于准备好的dom，初始化echarts实例
		nodeManagerChart = echarts.init(document
				.getElementById('nodeManagerChart'));

		// 设置图表属性
		nodeManagerChart.setOption({
			title : {
				text : 'Node Manager的状态',
				top : 5,
				left : 5
			},
			grid : {
				height : 300,
				left : '3%',
				right : '4%',
				containLabel : true

			},
			tooltip : {
				trigger : 'axis',
				axisPointer : {
					type : 'cross',
					label : {
						backgroundColor : '#6a7985'
					}
				}
			},
			legend : {
				data : [ '活跃', '挂掉' ],
				// bottom:'bottom',
				top : '10px',
				left : '50%',
				width : '200px',

			},
			toolbox : {
				feature : {
					dataZoom : {
						yAxisIndex : 'none'
					},
					restore : {},
					saveAsImage : {},
					dataView : {}
				}
			},
			dataZoom : [ { // 这个dataZoom组件，默认控制x轴。
				type : 'slider', // 这个 dataZoom 组件是 slider 型 dataZoom 组件
				start : 50, // 左边在 10% 的位置。
				end : 100, // 右边在 60% 的位置。
			} ],
			xAxis : {
				type : 'time',
				splitLine : {
					show : false
				},
				boundaryGap : false
			},
			yAxis : {
				type : 'value',
				min : 0,
				minInterval : 3
			},
			series : [ {
				name : '活跃',
				type : 'line',
				// areaStyle: {normal: {}},
				data : []
			}, {
				name : '挂掉',
				type : 'line',
				// areaStyle: {normal: {}},
				data : []
			} ]
		});

		// 加载提示动画
		nodeManagerChart.showLoading();

		fetchmemDataInterval(8);

		// 关闭提示动画
		nodeManagerChart.hideLoading();

		setInterval(fetchmemDataInterval, 8000);
		//setInterval(fetchmemDataInterval(kg),8000);
	});
})();
