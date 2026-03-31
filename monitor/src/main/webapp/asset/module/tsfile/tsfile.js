/**
 * Created by likeugo on 2017/7/24
 * 
 */

(function() {

	var tsfile_mem_chart = null;
	var timeGap = 7.5;

	var NonHeapMemoryUsage_init = [];// 非堆内存初始化
	var NonHeapMemoryUsage_used = [];// 非堆内存使用
	var NonHeapMemoryUsage_max = [];// 非堆内存最大使用
	var NonHeapMemoryUsage_committed = [];// 非堆内存提交

	var HeapMemoryUsage_init = [];// 堆内存初始化
	var HeapMemoryUsage_used = [];// 堆内存使用
	var HeapMemoryUsage_max = [];// 堆内存使用最大
	var HeapMemoryUsage_committed = [];// 堆内存提交

	var FreePhysicalMemorySize = [];// 物理空闲内存

	var LastFetchTime = 0;// 最后的时间

	// 获取应用运行数据
	function fetchMemDataInterval(limit) {
		if (limit == null) {
			limit = 1;
		}
		$
				.ajax({
					type : "POST",
					url : monitorWebBaseURL + "tsfile/memory",
					data : {
						'limit' : limit
					},
					dataType : "json",
					success : function(data) {
						if (data != null) {
							data = data.dataObject;
							var non_init = data.nonHeapMemoryUsageInit.module;
							var non_used = data.nonHeapMemoryUsageUsed.module;
							var non_max = data.nonHeapMemoryUsageMax.module;
							var non_committed = data.nonHeapMemoryUsageCommitted.module;

							var heap_init = data.heapMemoryUsageInit.module;
							var heap_used = data.heapMemoryUsageUsed.module;
							var heap_max = data.heapMemoryUsageMax.module;
							var heap_committed = data.heapMemoryUsageCommitted.module;

							var freephysicalmemory = data.freePhysicalMemorySize.module;

							non_init.reverse();
							non_used.reverse();
							non_max.reverse();
							non_committed.reverse();

							heap_init.reverse();
							heap_used.reverse();
							heap_max.reverse();
							heap_committed.reverse();

							/*freephysicalmemory.reverse();*/

							if (non_init != null) {
								$.each(non_init, function(i, item) {
									NonHeapMemoryUsage_init.push([
											heap_used[i].processTime*1000,
											((item.sum)/1048576).toFixed(2) ]);
								});
								if (non_init[non_init.length - 1].processTime > LastFetchTime) {
									LastFetchTime = non_init[non_init.length - 1].processTime;
								}
							}
							if (non_used != null) {
								$.each(non_used, function(i, item) {
									NonHeapMemoryUsage_used.push([
											heap_used[i].processTime*1000,
											((item.sum)/1048576).toFixed(2) ]);
								});
								if (non_used[non_used.length - 1].processTime > LastFetchTime) {
									LastFetchTime = non_used[non_used.length - 1].processTime;
								}
							}
							if (non_max != null) {
								$.each(non_max, function(i, item) {
									NonHeapMemoryUsage_max.push([
											heap_used[i].processTime*1000,
											((item.sum)/1048576).toFixed(2) ]);
								});
								if (non_max[non_max.length - 1].processTime > LastFetchTime) {
									LastFetchTime = non_max[non_max.length - 1].processTime;
								}
							}
							if (non_committed != null) {
								$.each(non_committed, function(i, item) {
									NonHeapMemoryUsage_committed.push([
											heap_used[i].processTime*1000,
											((item.sum)/1048576).toFixed(2) ]);
								});
								if (non_committed[non_committed.length - 1].processTime > LastFetchTime) {
									LastFetchTime = non_committed[non_committed.length - 1].processTime;
								}
							}

							if (heap_init != null) {
								$.each(heap_init, function(i, item) {
									HeapMemoryUsage_init.push([
											heap_used[i].processTime*1000,
											((item.sum)/1048576).toFixed(2) ]);
								});
								if (heap_init[heap_init.length - 1].processTime > LastFetchTime) {
									LastFetchTime = heap_init[heap_init.length - 1].processTime;
								}
							}

							if (heap_used != null) {
								$.each(heap_used, function(i, item) {
									HeapMemoryUsage_used.push([
											heap_used[i].processTime*1000,
											((item.sum)/1048576).toFixed(2) ]);
								});
								if (heap_used[heap_used.length - 1].processTime > LastFetchTime) {
									LastFetchTime = heap_used[heap_used.length - 1].processTime;
								}
							}
							if (heap_max != null) {
								$.each(heap_max, function(i, item) {
									HeapMemoryUsage_max.push([
											heap_used[i].processTime*1000,
											((item.sum)/1048576).toFixed(2) ]);
								});
								if (heap_max[heap_max.length - 1].processTime > LastFetchTime) {
									LastFetchTime = heap_max[heap_max.length - 1].processTime;
								}
							}
							if (heap_committed != null) {
								$.each(heap_committed, function(i, item) {
									HeapMemoryUsage_committed.push([
											heap_used[i].processTime*1000,
											((item.sum)/1048576).toFixed(2) ]);
								});
								if (heap_committed[heap_committed.length - 1].processTime > LastFetchTime) {
									LastFetchTime = heap_committed[heap_committed.length - 1].processTime;
								}
							}

							if (heap_committed != null) {
								$.each(heap_committed, function(i, item) {
									HeapMemoryUsage_committed.push([
											heap_used[i].processTime*1000,
											((item.sum)/1048576).toFixed(2) ]);
								});
								if (heap_committed[heap_committed.length - 1].processTime > LastFetchTime) {
									LastFetchTime = heap_committed[heap_committed.length - 1].processTime;
								}
							}

							/*if (freephysicalmemory != null) {
								$.each(freephysicalmemory, function(i, item) {
									FreePhysicalMemorySize.push([heap_used[i].processTime*1000,((item.sum)/1048576).toFixed(2) ]);
								});
								if (freephysicalmemory[freephysicalmemory.length - 1].processTime > LastFetchTime) {
									LastFetchTime = freephysicalmemory[freephysicalmemory.length - 1].processTime;
								}
							}

							if (FreePhysicalMemorySize.length >= 200) {

							}*/
							// 更新当前状态
							// 初始化的两个大小
							var d = HeapMemoryUsage_init[HeapMemoryUsage_init.length - 1][1];
							var s = NonHeapMemoryUsage_init[NonHeapMemoryUsage_init.length - 1][1];

							$("#heapInit")
									.html(
											getDataSize(HeapMemoryUsage_init[HeapMemoryUsage_init.length - 1][1]));

							$("#nonHeapInit")
									.html(
											getDataSize(NonHeapMemoryUsage_init[NonHeapMemoryUsage_init.length - 1][1]));

							$("#heapUsed")
									.html(
											HeapMemoryUsage_used[HeapMemoryUsage_used.length - 1][1]);
							$("#heapMax")
									.html(
											HeapMemoryUsage_max[HeapMemoryUsage_max.length - 1][1]);
							$("#nonUsed")
									.html(
											NonHeapMemoryUsage_used[NonHeapMemoryUsage_used.length - 1][1]);
							/*$("#freeMem")
									.html(
											FreePhysicalMemorySize[FreePhysicalMemorySize.length - 1][1]);
*/
							// 填入数据
							tsfile_mem_chart.setOption({
								series : [ {
									name : '堆使用',									
									data : HeapMemoryUsage_used
								}, {
									name : '堆最大使用',									
									data : HeapMemoryUsage_max
								}, {
									name : '非堆使用',									
									data : NonHeapMemoryUsage_used
								}
//								, {
//									name : '物理空闲内存',
//									type : 'line',
//									smooth : true,
//									symbol : 'none',
//									// areaStyle: {normal: {}},
//									data : FreePhysicalMemorySize
//								},

								]
							});
						}
					}
				});
	}

	// 定时获得数据，追加到原有的数据之后
	function fetchMemDataIntervalByone() {
		// 加载提示动画
		tsfile_mem_chart.showLoading();
		$
		.ajax({
			type : "POST",
			url : monitorWebBaseURL + "tsfile/memory",
			data : {
				'start' : LastFetchTime+1
			},
			dataType : "json",
			success : function(data) {
				if (data != null) {
					data = data.dataObject;
					var non_init = data.nonHeapMemoryUsageInit.module;
					var non_used = data.nonHeapMemoryUsageUsed.module;
					var non_max = data.nonHeapMemoryUsageMax.module;
					var non_committed = data.nonHeapMemoryUsageCommitted.module;

					var heap_init = data.heapMemoryUsageInit.module;
					var heap_used = data.heapMemoryUsageUsed.module;
					var heap_max = data.heapMemoryUsageMax.module;
					var heap_committed = data.heapMemoryUsageCommitted.module;

					var freephysicalmemory = data.freePhysicalMemorySize.module;

					if(heap_used.length==0 && heap_max.length==0 && non_used.length && freephysicalmemory.length==0){
						return;
					}
					//因为只有一条数据，所以不用逆序
					non_init.reverse();
					non_used.reverse();
					non_max.reverse();
					non_committed.reverse();

					heap_init.reverse();
					heap_used.reverse();
					heap_max.reverse();
					heap_committed.reverse();

					freephysicalmemory.reverse();
					
					//遍历所有的数组，去找最大时间
					var flag = 0;//判断是否都有数据
					//先push数据，后更新时间

					if (non_init != null && non_init.length!=0) {
						NonHeapMemoryUsage_init.push([
							non_init[0].processTime*1000,
								((non_init[0].sum)/1048576).toFixed(2) ]);
						flag = 1;
						
						if (non_init[0].processTime > LastFetchTime) {
							LastFetchTime = non_init[0].processTime;
						}					
						
					}else{
						NonHeapMemoryUsage_init.push(
							NonHeapMemoryUsage_init[NonHeapMemoryUsage_init.length-1]);
					}
					
					//同理，都来一遍
					if (non_used != null && non_used.length!=0) {
						NonHeapMemoryUsage_used.push([
							non_used[0].processTime*1000,
								((non_used[0].sum)/1048576).toFixed(2) ]);
						flag = 1;
						
						if (non_used[0].processTime > LastFetchTime) {
							LastFetchTime = non_used[0].processTime;
						}					
						
					}else{
						NonHeapMemoryUsage_used.push(
							NonHeapMemoryUsage_used[NonHeapMemoryUsage_used.length-1]);
					}
					
					//non_max
					if (non_max != null && non_max.length!=0) {
						NonHeapMemoryUsage_max.push([
							non_max[0].processTime*1000,
								((non_max[0].sum)/1048576).toFixed(2) ]);
						flag = 1;
						
						if (non_max[0].processTime > LastFetchTime) {
							LastFetchTime = non_max[0].processTime;
						}					
						
					}else{
						NonHeapMemoryUsage_max.push(
								NonHeapMemoryUsage_max[NonHeapMemoryUsage_max.length-1]);
					}
					//non_committed
					if (non_committed != null && non_committed.length!=0) {
						NonHeapMemoryUsage_committed.push([
							non_committed[0].processTime*1000,
								((non_committed[0].sum)/1048576).toFixed(2) ]);
						flag = 1;
						
						if (non_committed[0].processTime > LastFetchTime) {
							LastFetchTime = non_committed[0].processTime;
						}					
						
					}else{
						NonHeapMemoryUsage_committed.push(
								NonHeapMemoryUsage_committed[NonHeapMemoryUsage_committed.length-1]);
					}
					//heap_init
					if (heap_init != null && heap_init.length!=0) {
						HeapMemoryUsage_init.push([
							heap_init[0].processTime*1000,
								((heap_init[0].sum)/1048576).toFixed(2) ]);
						flag = 1;
						
						if (heap_init[0].processTime > LastFetchTime) {
							LastFetchTime = heap_init[0].processTime;
						}					
						
					}else{
						HeapMemoryUsage_init.push(
								HeapMemoryUsage_init[HeapMemoryUsage_init.length-1]);
					}
					//heap_used
					if (heap_used != null && heap_used.length!=0) {
						HeapMemoryUsage_used.push([
							heap_used[0].processTime*1000,
								((heap_used[0].sum)/1048576).toFixed(2) ]);
						flag = 1;
						
						if (heap_used[0].processTime > LastFetchTime) {
							LastFetchTime = heap_used[0].processTime;
						}					
						
					}else{
						HeapMemoryUsage_used.push(
								HeapMemoryUsage_used[HeapMemoryUsage_used.length-1]);
					}
					//heap_max
					if (heap_max != null && heap_max.length!=0) {
						HeapMemoryUsage_max.push([
							heap_max[0].processTime*1000,
								((heap_max[0].sum)/1048576).toFixed(2) ]);
						flag = 1;
						
						if (heap_max[0].processTime > LastFetchTime) {
							LastFetchTime = heap_max[0].processTime;
						}					
						
					}else{
						HeapMemoryUsage_max.push(
								HeapMemoryUsage_max[HeapMemoryUsage_max.length-1]);
					}
					//heap_committed
					if (heap_committed != null && heap_committed.length!=0) {
						HeapMemoryUsage_committed.push([
							heap_committed[0].processTime*1000,
								((heap_committed[0].sum)/1048576).toFixed(2) ]);
						flag = 1;
						
						if (heap_committed[0].processTime > LastFetchTime) {
							LastFetchTime = heap_committed[0].processTime;
						}					
						
					}else{
						HeapMemoryUsage_committed.push(
								HeapMemoryUsage_committed[HeapMemoryUsage_committed.length-1]);
					}
					//freephysicalmemory
					/*if (freephysicalmemory != null && freephysicalmemory.length!=0) {
						FreePhysicalMemorySize.push([
							freephysicalmemory[0].processTime*1000,
								((freephysicalmemory[0].sum)/1048576).toFixed(2) ]);
						flag = 1;
						
						if (freephysicalmemory[0].processTime > LastFetchTime) {
							LastFetchTime = freephysicalmemory[0].processTime;
						}					
						
					}else{
						FreePhysicalMemorySize.push(
								FreePhysicalMemorySize[FreePhysicalMemorySize.length-1]);
					}*/
					
//	
					//最最重要的一步，就是同步时间
					if(flag ==1){
						//重置时间，删除一个
						
						NonHeapMemoryUsage_init.shift();// 非堆内存初始化
						NonHeapMemoryUsage_used.shift();// 非堆内存使用
						NonHeapMemoryUsage_max.shift();;// 非堆内存最大使用
						NonHeapMemoryUsage_committed.shift();// 非堆内存提交

						HeapMemoryUsage_init.shift();// 堆内存初始化
						HeapMemoryUsage_used.shift();// 堆内存使用
						HeapMemoryUsage_max.shift();// 堆内存使用最大
						HeapMemoryUsage_committed.shift();// 堆内存提交
					/*	FreePhysicalMemorySize.shift();// 物理空闲内存
*/						
						NonHeapMemoryUsage_init[NonHeapMemoryUsage_init.length-1][0]=LastFetchTime*1000;
						NonHeapMemoryUsage_used[NonHeapMemoryUsage_used.length-1][0]=LastFetchTime*1000;
						NonHeapMemoryUsage_max[NonHeapMemoryUsage_max.length-1][0]=LastFetchTime*1000;
						NonHeapMemoryUsage_committed[NonHeapMemoryUsage_committed.length-1][0]=LastFetchTime*1000;
						HeapMemoryUsage_init[HeapMemoryUsage_init.length-1][0]=LastFetchTime*1000;
						HeapMemoryUsage_used[HeapMemoryUsage_used.length-1][0]=LastFetchTime*1000;
						HeapMemoryUsage_max[HeapMemoryUsage_max.length-1][0]=LastFetchTime*1000;
						HeapMemoryUsage_committed[HeapMemoryUsage_committed.length-1][0]=LastFetchTime*1000;
/*						FreePhysicalMemorySize[FreePhysicalMemorySize.length-1][0]=LastFetchTime*1000;
*/					}
					else
					{
						NonHeapMemoryUsage_init.pop();;// 非堆内存初始化
						NonHeapMemoryUsage_used.pop();// 非堆内存使用
						NonHeapMemoryUsage_max.pop();;// 非堆内存最大使用
						NonHeapMemoryUsage_committed.pop();// 非堆内存提交
						
						HeapMemoryUsage_init.pop();// 堆内存初始化
						HeapMemoryUsage_used.pop();// 堆内存使用
						HeapMemoryUsage_max.pop();// 堆内存使用最大
						HeapMemoryUsage_committed.pop();// 堆内存提交
						/*FreePhysicalMemorySize.pop();// 物理空闲内存
*/						//alert("没有数据！");
					}
					
					
					
					
					
					
					
					
					
					
					
					
					

					// 更新当前状态
					// 初始化的两个大小
					
					
					$("#heapInit")
							.html(
									getDataSize(HeapMemoryUsage_init[HeapMemoryUsage_init.length - 1][1]));

					$("#nonHeapInit")
							.html(
									getDataSize(NonHeapMemoryUsage_init[NonHeapMemoryUsage_init.length - 1][1]));

					$("#heapUsed")
							.html(
									HeapMemoryUsage_used[HeapMemoryUsage_used.length - 1][1]);
					$("#heapMax")
							.html(
									HeapMemoryUsage_max[HeapMemoryUsage_max.length - 1][1]);
					$("#nonUsed")
							.html(
									NonHeapMemoryUsage_used[NonHeapMemoryUsage_used.length - 1][1]);
					/*$("#freeMem")
							.html(
									FreePhysicalMemorySize[FreePhysicalMemorySize.length - 1][1]);
*/
					// 填入数据
					tsfile_mem_chart.setOption({
						series : [ {
							name : '堆使用',							
							data : HeapMemoryUsage_used
						}, {
							name : '堆最大使用',
							/*type : 'line',
							smooth : true,
							symbol : 'none',
							// areaStyle: {normal: {}},
*/							data : HeapMemoryUsage_max
						}, {
							name : '非堆使用',
							/*type : 'line',
							smooth : true,
							symbol : 'none',
							// areaStyle: {normal: {}},
*/							data : NonHeapMemoryUsage_used
						}
//						, {
//							name : '物理空闲内存',
//							type : 'line',
//							smooth : true,
//							symbol : 'none',
//							// areaStyle: {normal: {}},
//							data : FreePhysicalMemorySize
//						},

						]
					});
				}
			}
		});
			tsfile_mem_chart.hideLoading();
	}

	$(function() {

		// 基于准备好的dom，初始化echarts实例
		tsfile_mem_chart = echarts.init(document
				.getElementById('tsfile_mem_chart'));

		

		// 设置图表属性
		tsfile_mem_chart.setOption({
			title : {
				text : 'IoTDB 内存使用状态',
				top : 5,
				left : 5
			},
			grid : {
				height : 300,
				left : '6%',
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
				// data : [ 'CPU使用率', '内核', '用户', 'I/O', '用户进程' ],
				data : [ '堆使用', '堆最大使用', '非堆使用'],
				// bottom:'bottom',
				top : '10px',

			},
			toolbox : {
				feature : {
					dataZoom : {
						yAxisIndex : 'none'
					},
					restore : {},
					saveAsImage : {},
					dataView : {show: true, readOnly: false},
				},
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
				name : '（MB）',
				type : 'value',
				boundaryGap : false,
				min : 0,
				minInterval : 3
			},
			series : [ {
				name : '堆使用',
				type : 'line',
				smooth : true,
				symbol : 'none',
				// areaStyle: {normal: {}},
				data : []
			}, {
				name : '堆最大使用',
				type : 'line',
				smooth : true,
				symbol : 'none',
				// areaStyle: {normal: {}},
				data : []
			}, {
				name : '非堆使用',
				type : 'line',
				smooth : true,
				symbol : 'none',
				// areaStyle: {normal: {}},
				data : []
			},

//			{
//				name : '物理空闲内存',
//				type : 'line',
//				smooth : true,
//				symbol : 'none',
//				// areaStyle: {normal: {}},
//				data : []
//			},

			]
		});

		fetchMemDataInterval(200);
		// 关闭提示动画
		 setInterval(fetchMemDataIntervalByone, 3000);
	});
	function getDataSize(dataFileSize) {
		//dataFileSize传入的时候就是MB
		if (dataFileSize < 1024) {
			return dataFileSize + "<small>MB</small>";
		}
		dataFileSize = (dataFileSize / 1024).toFixed(2);
		if (dataFileSize < 1024) {
			return dataFileSize + "<small>GB</small>";
		}
		dataFileSize = (dataFileSize / 1024).toFixed(2);
		if (dataFileSize < 1024) {
			return dataFileSize + "<small>PB</small>";
		}
	}
})();
