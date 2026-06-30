/** 保存了页面中的echart对象，当窗口大小变化时，遍历集合进行resize **/
var __chartsToResize = [];

$(window).resize(function () {
	$.each(__chartsToResize, function(i, chart){
		$(chart).resize();
	});
});