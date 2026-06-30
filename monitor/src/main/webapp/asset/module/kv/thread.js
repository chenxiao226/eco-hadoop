/**
 * Created by gaoyu on 2018/8/9
 * 
 */

(function() {

	var Cassandra_thread = null;

	var Active = [];// 活跃任务数
	var Pending = [];// 等待任务数
	var timestamp = null;
/*	alert(timestamp);*/

	// 获取线程池运行数据
	function fetchthreadDataInterval() {
		
		$.ajax({
			type : "POST",
			url : monitorWebBaseURL + "cassandra/Thread",
			data : {
			},
			dataType : "json",
			success : function(data) {
				if (data != null) {

					var ActiveTasks = data.dataObject.ActiveTasks;
					var PendingTasks = data.dataObject.PendingTasks;
					var MaxPoolSize = data.dataObject.MaxPoolSize;
					var CompletedTasks = data.dataObject.CompletedTasks;
					timestamp = (new Date()).getTime();

					
					Active.push([ timestamp, parseInt(ActiveTasks) ]);
					Pending.push([  timestamp, parseInt(PendingTasks)]);
					
					if (Active.length >= 200) {
						Active.shift(); // 删除第一个
						Pending.shift(); // 删除第一个
					}
					// 更新当前状态

					$("#ActiveTasks").html(ActiveTasks);
					$("#PendingTasks").html(PendingTasks);
					$("#MaxPoolSize").html(MaxPoolSize);
					$("#CompletedTasks").html(CompletedTasks);

					// 设置最大值
					// 填入数据
					Cassandra_thread.setOption({
						series : [ {
							name : '正在运行',
							data : Active
						}, {
							name : '排队等待',
							data : Pending
						}, ]
					});
				}
			}
		});
	}

	// 定时获得数据，追加到原有的数据之后
	function fetchthreadDataIntervalByone() {
		$
				.ajax({
					type : "POST",
					url : monitorWebBaseURL + "cassandra/Thread",
					data : {
					},
					dataType : "json",
					success : function(data) {
						if (data != null) {

							var ActiveTasks = data.dataObject.ActiveTasks;
							var PendingTasks = data.dataObject.PendingTasks;
							var MaxPoolSize = data.dataObject.MaxPoolSize;
							var CompletedTasks = data.dataObject.CompletedTasks;
							timestamp = (new Date()).getTime();

							
							Active.push([ timestamp, parseInt(ActiveTasks) ]);
							Pending.push([  timestamp, parseInt(PendingTasks)]);
							
							if (Active.length >= 200) {
								Active.shift(); // 删除第一个
								Pending.shift(); // 删除第一个
							}
							// 更新当前状态

							$("#ActiveTasks").html(ActiveTasks);
							$("#PendingTasks").html(PendingTasks);
							$("#MaxPoolSize").html(MaxPoolSize);
							$("#CompletedTasks").html(CompletedTasks);

							// 设置最大值
							// 填入数据
							Cassandra_thread.setOption({
								series : [ {
									name : '正在运行',
									data : Active
								}, {
									name : '排队等待',
									data : Pending
								}, ]
							});
						}
					}
				});
	}

	$(function() {

		// 基于准备好的dom，初始化echarts实例
		Cassandra_thread = echarts.init(document.getElementById('cassandra_thread'));

		// 设置图表属性
		Cassandra_thread.setOption({
			title : {
				text : '线程池状态',
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
				data : [ '正在运行', '排队等待' ],
				top : '10px',

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
				name : '（个）',
				type : 'value',
				boundaryGap: false,				
				min : 0,
				minInterval : 3,
			},
			series : [ {
				name : '正在运行',
				type : 'line',
				smooth: true,
				symbol: 'none',
				data : []
			}, {
				name : '排队等待',
				type : 'line',
				smooth: true,
				symbol: 'none',
				data : []
			}

			]
		});

		// 加载提示动画
		Cassandra_thread.showLoading();

		fetchthreadDataInterval();
		// 关闭提示动画
		Cassandra_thread.hideLoading();

		setInterval(fetchthreadDataIntervalByone, 8000);
	});
})();
