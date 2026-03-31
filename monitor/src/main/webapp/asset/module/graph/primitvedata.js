/**
 * Created by gaoyu on 2018/8/5
 * 
 */

(function() {
	
	var primidata = null;

	var procInnodb = [];// Innodb
	var NumberOfNodeIdsInUse= null;
	var NumberOfPropertyIdsInUse= null;
	var NumberOfRelationshipIdsInUse= null;
	var NumberOfRelationshipTypeIdsInUse= null;
	
	var primitives = null;
	
	// 获取应用运行数据
	function fetchProcDataInterval() {		
		
		$.ajax({
			type : "POST",
			url : monitorWebBaseURL + "Neo4j/primitive",
			data : {
				/*'limit' : limit*/
			},
			dataType : "json",
			success : function(data) {
				if (data != null) {
				
					 NumberOfNodeIdsInUse = data.dataObject.NumberOfNodeIdsInUse;
					 NumberOfPropertyIdsInUse = data.dataObject.NumberOfPropertyIdsInUse;
					 NumberOfRelationshipIdsInUse = data.dataObject.NumberOfRelationshipIdsInUse;
					 NumberOfRelationshipTypeIdsInUse = data.dataObject.NumberOfRelationshipTypeIdsInUse;
					
					//procInnodb.reverse();
		/*			primitive.push("",(procInnodbs.Innodb*100).toFixed(5)]);*/

					// 更新当前状态
					$("#title_nodes").html(NumberOfNodeIdsInUse);
					$("#NumberOfNodeIdsInUse").html(NumberOfNodeIdsInUse);
					$("#NumberOfPropertyIdsInUse").html(NumberOfPropertyIdsInUse);
					$("#NumberOfRelationshipIdsInUse").html(NumberOfRelationshipIdsInUse);
					$("#NumberOfRelationshipTypeIdsInUse").html(NumberOfRelationshipTypeIdsInUse);
					// 设置最大值
					// 填入数据
					primidata.setOption({
						 series : [  {
							data : [NumberOfNodeIdsInUse,NumberOfPropertyIdsInUse,NumberOfRelationshipIdsInUse,NumberOfRelationshipTypeIdsInUse],						
						}]
					});
				}
			}
		});
	}

	// 定时获得CPU数据，追加到原有的数据之后
	function fetchInnodbBufferDataIntervalByone() {
		
		$.ajax({
					type : "POST",
					url : monitorWebBaseURL + "Neo4j/primitive",
					data : {
						//'limit' : 1
					},
					dataType : "json",
					success : function(data) {
						if (data != null) {

							var primitives = data.dataObject;
							var NumberOfNodeIdsInUse = data.dataObject.NumberOfNodeIdsInUse;
							var NumberOfPropertyIdsInUse = data.dataObject.NumberOfPropertyIdsInUse;
							var NumberOfRelationshipIdsInUse = data.dataObject.NumberOfRelationshipIdsInUse;
							var NumberOfRelationshipTypeIdsInUse = data.dataObject.NumberOfRelationshipTypeIdsInUse;

							
							// 设置最大值
							// 填入数据
							primidata.setOption({
								series : [  {
									data : [NumberOfNodeIdsInUse,NumberOfPropertyIdsInUse,NumberOfRelationshipIdsInUse,NumberOfRelationshipTypeIdsInUse],						
								}]
							});
						}
					}
				});
	}

	$(function() {

		// 基于准备好的dom，初始化echarts实例
		primidata = echarts.init(document.getElementById('PrimitiveData'));

		// 设置图表属性
		primidata.setOption({
			title : {
				text : 'PrimitiveData',
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
	
			toolbox : {
				feature : {					
					saveAsImage : {},
					dataView : {}
				}
			},		
			
			xAxis : {
				type : 'category',
				splitLine : {
					show : false
				},	
				data:['节点数量','属性数量','关系数量','关系类型数量' ],
				
			},
			yAxis : {
				name : '个',
				type : 'value',
				boundaryGap : false,
				min : 0,
			},
			series : [  {				
				type : 'bar',
				barWidth : 60,
				data : [NumberOfNodeIdsInUse,NumberOfPropertyIdsInUse,NumberOfRelationshipIdsInUse,NumberOfRelationshipTypeIdsInUse],
			}

			]
		});

		// 加载提示动画
		primidata.showLoading();

		fetchProcDataInterval(20);
		// 关闭提示动画
		primidata.hideLoading();

		setInterval(fetchInnodbBufferDataIntervalByone,8000);
	});
})();
