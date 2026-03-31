/**
 * Created by gaoyu on 2018/8/9
 * 
 */

(function() {

	var cache_ratio = null;

	var hitrates = [];// 
	var onemintehitrate = [];// 
	var fivemintehitrate = [];
	var timestamp= null;


	// 获取线程池运行数据
	function fetchKeyCacheData() {
		
		$.ajax({
			type : "POST",
			url : monitorWebBaseURL + "cassandra/CacheHits",
			data : {
			},
			dataType : "json",
			success : function(data) {
				if (data != null) {

					var OneMinuteRate = data.dataObject.OneMinuteRate;
					var HitRate = data.dataObject.HitRate;
					var Hitcounts = data.dataObject.Hitcounts;
					var FiveMinuteRate = data.dataObject.FiveMinuteRate;
					if(parseFloat(data.dataObject.OneMinuteRate)*100>=100){
						OneMinuteRate =0;
					}
					else {
						OneMinuteRate = (parseFloat(data.dataObject.OneMinuteRate*100)).toFixed(2);
							}
					timestamp = (new Date()).getTime();

					
					hitrates.push([ timestamp, (parseFloat(HitRate*100)).toFixed(2)]);
					onemintehitrate.push([  timestamp, OneMinuteRate]);
					fivemintehitrate.push([  timestamp, (parseFloat(FiveMinuteRate*100)).toFixed(2)]);
					
					if (hitrates.length >= 200) {
						hitrates.shift(); // 删除第一个
						onemintehitrate.shift(); // 删除第一个
						fivemintehitrate.shift(); // 删除第一个
						
					}
					// 更新当前状态
					$("#OneMinuteRate").html(OneMinuteRate+'%');
					$("#HitRate").html((parseFloat(HitRate*100)).toFixed(2)+'%');
					$("#Hitcounts").html(Hitcounts);
					$("#FiveMinuteRate").html((parseFloat(FiveMinuteRate*100)).toFixed(2)+'%');

					// 设置最大值
					// 填入数据
					cache_ratio.setOption({
						series : [ {
							name : '1min缓存命中率',
							data : onemintehitrate
						}, {
							name : '5min缓存命中率',
							data : fivemintehitrate
						}, {
							name : '缓存总命中率',
							data : hitrates
						}, ]
					});
				}
			}
		});
	}

	// 定时获得数据，追加到原有的数据之后
	function fetchKeyCacheDataByone() {
		$
				.ajax({
					type : "POST",
					url : monitorWebBaseURL + "cassandra/CacheHits",
					data : {
					},
					dataType : "json",
					success : function(data) {
						if (data != null) {

							var OneMinuteRate = data.dataObject.OneMinuteRate;
							var HitRate = data.dataObject.HitRate;
							var Hitcounts = data.dataObject.Hitcounts;
							var FiveMinuteRate = data.dataObject.FiveMinuteRate;
							timestamp = (new Date()).getTime();
							if(parseFloat(data.dataObject.OneMinuteRate)*100>=100){
								OneMinuteRate =0;
							}
							else {
								OneMinuteRate = (parseFloat(data.dataObject.OneMinuteRate*100)).toFixed(2);
									}
							timestamp = (new Date()).getTime();

							
							hitrates.push([ timestamp, (parseFloat(HitRate*100)).toFixed(2)]);
							onemintehitrate.push([  timestamp, OneMinuteRate]);
							fivemintehitrate.push([  timestamp, (parseFloat(FiveMinuteRate*100)).toFixed(2)]);
							
							if (hitrates.length >= 200) {
								hitrates.shift(); // 删除第一个
								onemintehitrate.shift(); // 删除第一个
								fivemintehitrate.shift(); // 删除第一个
								
							}
							// 更新当前状态

							$("#OneMinuteRate").html(OneMinuteRate+'%');
							$("#HitRate").html((parseFloat(HitRate*100)).toFixed(2)+'%');
							$("#Hitcounts").html(Hitcounts);
							$("#FiveMinuteRate").html((parseFloat(FiveMinuteRate*100)).toFixed(2)+'%');

							// 设置最大值
							// 填入数据
							cache_ratio.setOption({
								series : [ {
									name : '1min缓存命中率',
									data : onemintehitrate
								}, {
									name : '5min缓存命中率',
									data : fivemintehitrate
								}, {
									name : '缓存总命中率',
									data : hitrates
								}, ]
							});
						}
					}
				});
	}

	$(function() {

		// 基于准备好的dom，初始化echarts实例
		cache_ratio = echarts.init(document.getElementById('cache_ratio'));

		// 设置图表属性
		cache_ratio.setOption({
			title : {
				text : 'KeyCache指标',
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
				data : [ '1min缓存命中率', '5min缓存命中率' ,'缓存总命中率'],
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
				name : '%',
				type : 'value',
				boundaryGap: false,
				max : 100,
				min : 0,
				minInterval : 3,
			},
			series : [ {
				name : '1min缓存命中率',
				type : 'line',
				smooth: true,
				symbol: 'none',
				data : []
			}, {
				name : '5min缓存命中率',
				type : 'line',
				smooth: true,
				symbol: 'none',
				data : []
			}, {
				name : '缓存总命中率',
				type : 'line',
				smooth: true,
				symbol: 'none',
				data : []
			}

			]
		});

		// 加载提示动画
		cache_ratio.showLoading();

		fetchKeyCacheData();
		// 关闭提示动画
		cache_ratio.hideLoading();

		setInterval(fetchKeyCacheDataByone, 4000);
	});
})();
