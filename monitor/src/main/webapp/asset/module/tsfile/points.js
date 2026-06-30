/**
 * Created by gaoyu on 2018/8/15
 * 
 */

(function() {
	
	var tsfile_point_chart = null;

	
	var pointsTotal = []; //total	success
	var ponitsPerSeconds = [];//PerSeconds success
	
	var pointsFail = [];//total fail
	var pointsFailperSeconds= [];
	
	var REQ_SUCCESS = [];
	var REQ_SUCCESS_perSeconds = [];
	
	var REQ_FAIL = [];
	var REQ_FAIL_perSeconds = [];
	
	
	// 获取应用运行数据
	function fetchProcDataInterval() {
		
		
		$.ajax({
			type : "POST",
			url : monitorWebBaseURL + "tsfile/totalponits",
			data : {
				/*'limit' : limit*/
			},
			dataType : "json",
			success : function(data) {
				if (data.dataObject != null) {

					
					
					//procInnodb.reverse();
					timestamp = (new Date()).getTime()
					pointsTotal.push(parseInt(data.dataObject.TOTAL_POINTS_SUCCESS));
					pointsFail.push(parseInt(data.dataObject.TOTAL_POINTS_FAIL));
					REQ_SUCCESS.push(parseInt(data.dataObject.REQ_SUCCESS));
					REQ_FAIL.push(parseInt(data.dataObject.REQ_FAIL));
					if(pointsTotal.length>=3){
						ponitsPerSeconds.push([timestamp,(pointsTotal[pointsTotal.length-1]-pointsTotal[pointsTotal.length-2])/2]);
						pointsFailperSeconds.push([timestamp,(pointsFail[pointsFail.length-1]-pointsFail[pointsFail.length-2])/2]);
						REQ_SUCCESS_perSeconds.push([timestamp,(REQ_SUCCESS[REQ_SUCCESS.length-1]-REQ_SUCCESS[REQ_SUCCESS.length-2])/2]);
						REQ_FAIL_perSeconds.push([timestamp,(REQ_FAIL[REQ_FAIL.length-1]-REQ_FAIL[REQ_FAIL.length-2])/2]);
						
						$("#pointpers").html((pointsTotal[pointsTotal.length-1]-pointsTotal[pointsTotal.length-2])/2);
						$("#failpointpers").html((pointsFail[pointsFail.length-1]-pointsFail[pointsFail.length-2])/2);
						$("#REQ_SUCCESS").html((REQ_SUCCESS[REQ_SUCCESS.length-1]-REQ_SUCCESS[REQ_SUCCESS.length-2])/2);
						$("#REQ_FAIL").html((REQ_FAIL[REQ_FAIL.length-1]-REQ_FAIL[REQ_FAIL.length-2])/2);
					}
					

					if (ponitsPerSeconds.length >= 200) {
						pointsTotal.shift(); // 删除第一个
						pointsFail.shift(); 
						REQ_SUCCESS.shift(); 
						REQ_FAIL.shift(); 
						
						ponitsPerSeconds.shift();
						pointsFailperSeconds.shift();
						REQ_SUCCESS_perSeconds.shift();
						REQ_FAIL_perSeconds.shift();
					}
					// 更新当前状态

					
					// 设置最大值
					// 填入数据
					tsfile_point_chart.setOption({
						series : [ {
							name : 'tps/s',
							data : ponitsPerSeconds,
						},{
							name : 'tpf/s',
							data : pointsFailperSeconds,
						},{
							name : 'trs/s',
							data : REQ_SUCCESS_perSeconds,
						},{
							name : 'trf/s',
							data : REQ_FAIL_perSeconds,
						} ]
					});
				}
			}
		});
	}

	// 定时获得数据，追加到原有的数据之后
	function fetchInnodbBufferDataIntervalByone() {
		
		$.ajax({
					type : "POST",
					url : monitorWebBaseURL + "tsfile/totalponits",
					data : {
						//'limit' : 1
					},
					dataType : "json",
					success : function(data) {
						if (data != null) {

							//var procRuns = data.dataObject.procRun;
							timestamp = (new Date()).getTime()
							pointsTotal.push(parseInt(data.dataObject.TOTAL_POINTS_SUCCESS));
							pointsFail.push(parseInt(data.dataObject.TOTAL_POINTS_FAIL));
							REQ_SUCCESS.push(parseInt(data.dataObject.REQ_SUCCESS));
							REQ_FAIL.push(parseInt(data.dataObject.REQ_FAIL));
							//alert(parseInt(data.dataObject));
							//alert((pointsTotal[pointsTotal.length-1]-pointsTotal[pointsTotal.length-2])/2);
							if(pointsTotal.length>=3){
								ponitsPerSeconds.push([timestamp,(pointsTotal[pointsTotal.length-1]-pointsTotal[pointsTotal.length-2])/2]);
								pointsFailperSeconds.push([timestamp,(pointsFail[pointsFail.length-1]-pointsFail[pointsFail.length-2])/2]);
								REQ_SUCCESS_perSeconds.push([timestamp,(REQ_SUCCESS[REQ_SUCCESS.length-1]-REQ_SUCCESS[REQ_SUCCESS.length-2])/2]);
								REQ_FAIL_perSeconds.push([timestamp,(REQ_FAIL[REQ_FAIL.length-1]-REQ_FAIL[REQ_FAIL.length-2])/2]);
								
								$("#pointpers").html((pointsTotal[pointsTotal.length-1]-pointsTotal[pointsTotal.length-2])/2);
								$("#failpointpers").html((pointsFail[pointsFail.length-1]-pointsFail[pointsFail.length-2])/2);
								$("#REQ_SUCCESS").html((REQ_SUCCESS[REQ_SUCCESS.length-1]-REQ_SUCCESS[REQ_SUCCESS.length-2])/2);
								$("#REQ_FAIL").html((REQ_FAIL[REQ_FAIL.length-1]-REQ_FAIL[REQ_FAIL.length-2])/2);
							}
							//alert(ponitsPerSeconds.length);
							//alert(ponitsPerSeconds[ponitsPerSeconds.length-1]);

							if (ponitsPerSeconds.length >= 200) {
								pointsTotal.shift(); // 删除第一个
								pointsFail.shift(); 
								REQ_SUCCESS.shift(); 
								REQ_FAIL.shift(); 
								
								ponitsPerSeconds.shift();
								pointsFailperSeconds.shift();
								REQ_SUCCESS_perSeconds.shift();
								REQ_FAIL_perSeconds.shift();
							}
							//alert(ponitsPerSeconds[ponitsPerSeconds.length-1]);
							// 设置最大值
							// 填入数据
							tsfile_point_chart.setOption({
								series : [  {
									name : 'tps/s',
									data : ponitsPerSeconds,
								},{
									name : 'tpf/s',
									data : pointsFailperSeconds,
								},{
									name : 'trs/s',
									data : REQ_SUCCESS_perSeconds,
								},{
									name : 'trf/s',
									data : REQ_FAIL_perSeconds,
								}  ]
							});
						}
					}
				});
	}

	$(function() {

		// 基于准备好的dom，初始化echarts实例
		tsfile_point_chart = echarts.init(document.getElementById('tsfile_point_chart'));

		// 设置图表属性
		tsfile_point_chart.setOption({
			title : {
				text : 'IoTDB写入数据统计',
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
				data : [ 'tps/s' ,'tpf/s','trs/s','trf/s'],
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
				name : '点数/秒',
				type : 'value',
				boundaryGap: false,				
				min : 0,
				minInterval : 3,
			},
			series : [  {
				name : 'tps/s',
				type : 'line',
				smooth: true,
				symbol: 'none',
				data : []
			},{
				name : 'tpf/s',
				type : 'line',
				smooth: true,
				symbol: 'none',
				data : []
			},{
				name : 'trs/s',
				type : 'line',
				smooth: true,
				symbol: 'none',
				data : []
			},{
				name : 'trf/s',
				type : 'line',
				smooth: true,
				symbol: 'none',
				data : []
			}

			]
		});

		// 加载提示动画
		tsfile_point_chart.showLoading();

		fetchProcDataInterval();
		// 关闭提示动画
		tsfile_point_chart.hideLoading();

		setInterval(fetchInnodbBufferDataIntervalByone,2000);
	});
})();
