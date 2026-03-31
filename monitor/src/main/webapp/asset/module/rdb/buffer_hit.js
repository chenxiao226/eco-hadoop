/**
 * Created by gaoyu on 2018/8/5
 * 
 */

(function() {
	
	var rdb_proc = null;

	var procInnodb = [];// Innodb
	
	// 获取应用运行数据
	function fetchProcDataInterval() {
		
		
		$.ajax({
			type : "POST",
			url : monitorWebBaseURL + "RDB/InnoDB_Buffer",
			data : {
				/*'limit' : limit*/
			},
			dataType : "json",
			success : function(data) {
				if (data != null) {

					var procInnodbs = data.dataObject;
					
					//procInnodb.reverse();


					procInnodb.push([procInnodbs.time*1000,(procInnodbs.Innodb*100).toFixed(5)]);

					if (procInnodb.length >= 200) {
						procInnodb.shift(); // 删除第一个
					}
					// 更新当前状态

					$("#Innodb_Buffer").html((procInnodbs.Innodb*100).toFixed(5)+'%');
					// 设置最大值
					// 填入数据
					rdb_proc.setOption({
						series : [ {
							name : 'Innodb',
							data : procInnodb
						} ]
					});
				}
			}
		});
	}

	// 定时获得CPU数据，追加到原有的数据之后
	function fetchInnodbBufferDataIntervalByone() {
		
		$.ajax({
					type : "POST",
					url : monitorWebBaseURL + "RDB/InnoDB_Buffer",
					data : {
						//'limit' : 1
					},
					dataType : "json",
					success : function(data) {
						if (data != null) {

							//var procRuns = data.dataObject.procRun;
							var procInnodbs = data.dataObject;
							
							//procInnodb.reverse();

							procInnodb.push([procInnodbs.time*1000,(procInnodbs.Innodb*100).toFixed(5)]);

							if (procInnodb.length >= 200) {
								procInnodb.shift(); // 删除第一个
								
							}

							/*for ( var i in procInnodb){
									alert(procInnodb[i]);
								}*/
							// 更新当前状态

							$("#Innodb_Buffer").html((procInnodbs.Innodb*100).toFixed(5)+'%');
							
							// 设置最大值
							// 填入数据
							rdb_proc.setOption({
								series : [  {
									name : 'Innodb',
									data : procInnodb,
								} ]
							});
						}
					}
				});
	}

	$(function() {

		// 基于准备好的dom，初始化echarts实例
		rdb_proc = echarts.init(document.getElementById('rdb_proc'));

		// 设置图表属性
		rdb_proc.setOption({
			title : {
				text : 'Buffer命中率',
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
				data : [ 'Innodb' ],
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
				name : '（%）',
				type : 'value',
				boundaryGap: false,			
				min : 0,
				minInterval : 3,
			},
			series : [  {
				name : 'Innodb',
				type : 'line',
				smooth: true,
				symbol: 'none',
				data : []
			}

			]
		});

		// 加载提示动画
		rdb_proc.showLoading();

		fetchProcDataInterval(20);
		// 关闭提示动画
		rdb_proc.hideLoading();

		setInterval(fetchInnodbBufferDataIntervalByone,3000);
	});
})();
