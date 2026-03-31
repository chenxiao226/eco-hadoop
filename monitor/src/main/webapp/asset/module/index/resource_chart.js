/**
 * Created by ElimsY on 2017/5/21.
 */

(function () {

	var resourceChart = null;
	var loadData = [];

	// 获取异构资源分配信息
	function fetchResourceAllocation() {
		$.ajax({
			type: "POST",
			url: monitorWebBaseURL + "index/cluster/resource",
			data: {},
			dataType: "json",
			success: function (data) {
				if (data !== null) {
					var indicatorCpu = [];
					var indicatorMem = [];
					var cpuData = [];
					var memData = [];
					var cpuMax =  0;
					var memMax =  0;
					$.each(data,function(i, item){
						cpuMax += item.cpus;
						memMax += item.mem;
					});
					$.each(data,function(i, item){
						if(item.name.indexOf("/") != -1){
							item.name = item.name.substring(1);
						}
						indicatorCpu.push({
							text: item.name,
							max:cpuMax
						});
						indicatorMem.push({
							text: item.name,
							max:memMax
						});
						cpuData.push(item.cpus);
						memData.push(item.mem);
					});

					// 填入数据
					resourceChart.setOption({
						radar: [
							{
								indicator:indicatorCpu,
								shape: 'circle',
								center: ['27%', '50%'],
								radius: 80
							},
							{
								indicator: indicatorMem,
								shape: 'circle',
								radius: 80,
								center: ['73%', '50%']
							}
						],
						series: [
							{
								name:"CPU",
								data: [
									{
										value: cpuData,
										name: 'CPU'
									}
								]
							},
							{
								name:"Memory",
								data: [
									{
										value: memData,
										name: '内存 (MB)',
									}
								]
							}
						]
					});

					// 关闭提示动画
					resourceChart.hideLoading();
				}
			}
		});
	}

	$(function () {

		// 基于准备好的dom，初始化echarts实例
//		resourceChart = echarts.init(document.getElementById('resourceDistChart'));
        var resourceChart = document.getElementById('resourceDistChart');
		// 设置图表属
		if(!resourceChart){console.error("错误找resourceDistChart")
		resourceChart.setOption({
			title: [
				{
					text: '异构资源分配',
					top: 5,
					left: 5
				},
				{
					text: 'CPU分配',
					bottom: 30,
					left: '25%',
					textStyle: {
						color: '#aaa',
						fontSize: 14
					}
				},
				{
					text: '内存分配',
					bottom: 30,
					right: '25%',
					textStyle: {
						color: '#aaa',
						fontSize: 14
					}
				}
			],
			tooltip: {
				trigger: 'axis'
			},
			toolbox: {
				feature: {
					dataView:{
						readOnly: true
					},
					saveAsImage: {}
				},
				right: 20,
				top: 5
			},
			radar: [],
			series: [
				{
					name:"CPU",
					type: 'radar',
					tooltip: {
						trigger: 'item'
					},
					itemStyle: {normal: {areaStyle: {type: 'default'}}},
					data: [
					]
				},
				{
					name:"Memory",
					type: 'radar',
					radarIndex: 1,
					tooltip: {
						trigger: 'item'
					},
					itemStyle: {normal: {areaStyle: {type: 'default'}}},
					data: []
				}
			]
		});

		// 加载提示动画
		resourceChart.showLoading();
		fetchResourceAllocation();
		setInterval(fetchResourceAllocation,20000);
	});
})();

