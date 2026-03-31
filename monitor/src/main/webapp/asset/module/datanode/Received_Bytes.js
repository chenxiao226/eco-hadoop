/**
 * 
 */
(function(){
	var result='\
		<div class="col-md-12 col-sm-12 col-xs-12" id="displayECharts">\
		</div>';
	document.write(result);
	var myChart=echarts.init(document.getElementById('displayECharts'));
	myChart.showLoading();
	var data=null;
	var dates=[];
	var reference=null;
	Monitor=function(){
		$.ajax({
			async: true,
			url:monitorWebBaseURL+"datanode",
			type:"POST",
			dataType:"json",
			data:{
				"time":(new Date().getTime())/1000
			},success: function(result){
				//请求成功时执行该函数内容，result即为服务器返回的json对象
	            if (result.dataObject.exist) {
	            	if(data==null){
	            		reference=result;
	                	data=[];
//	                	var options;
//	                	for(var i=0,l=result.total;i<l;i++){
//	                		data[i]=[];
//	                	} 
//	                	var i=0;
//	                	while(i+2<result.total){
//	                		options+="<tr><td><label><input type=\"checkbox\" value="+i+" name=\"options\" />" + result.metrics[i++].metric + "</ label></td>";
//	                		options+="<td><label><input type=\"checkbox\" value="+i+" name=\"options\" />" + result.metrics[i++].metric + "</ label></td>";
//	                		options+="<td><label><input type=\"checkbox\" value="+i+" name=\"options\" />" + result.metrics[i++].metric + "</ label></td></ tr>";
//	                	}
//	                	$("tbody#options").html(options);
//	                	$("input[value='0']").attr("checked",true);
//	                	$("input[value='1']").attr("checked",true);
//	                	$("input[value='2']").attr("checked",true);
	                }	
	            	for(var i=0,l=result.total;i<l;i++){
	            		data[i].push(result.metrics[i].value);
	            	} 
					dates.push(result.metrics[1].sampleTime);
	                myChart.hideLoading();    //隐藏加载动画         
	                  	
	                  	
	                var check=[];
//	    		    $.each($("input[name='options']"), function(idx,obj) {
//	    				if(obj.checked){
//	    					check.push(idx);
//	    				}
//	    			})
	    			var seriesOp="{title: { text: '实时监控数据' }, tooltip: { trigger: 'axis'},dataZoom: [{ type: 'slider', start: 0,   end: 100},{type: 'inside', start: 0,end: 100 }],";
	    			seriesOp+="toolbox: {  show: true,feature: { saveAsImage: {}}},xAxis: {  type : 'category',data : [] },";
	    			seriesOp+="yAxis : [{type : 'value',name : 's',axisLabel : { formatter: '{value} #' } }],";
	    			seriesOp+="xAxis:{data: dates},series:[";
	    			$.each(check, function(idx,obj) {
	    				seriesOp+="{ name:'"+reference.metrics[obj].metric+"', type:'line',symbol:'emptycircle',data:data["+obj+"]},";
	    			});
	    			seriesOp=seriesOp.substring(0,seriesOp.length-1);
	    			seriesOp+="],legend: {show:true, data:[";
	    			$.each(check, function(idx,obj) {
	    				seriesOp+="'"+reference.metrics[obj].metric+"',";
	    			});
	    			seriesOp=seriesOp.substring(0,seriesOp.length-1);
	    			seriesOp+="]}, color:[";
	    			$.each(check, function(idx,obj) {
	    				seriesOp+="'"+getRandomColor()+"',";
	    			});
	    			seriesOp=seriesOp.substring(0,seriesOp.length-1);
	    			seriesOp+="]}";
	    			var sOption=eval('('+seriesOp+')');
	    			myChart.setOption(sOption,true);
	            }
	            else {
	                //返回的数据为空时显示提示信息
	                alert("图表请求数据为空，可能服务器暂未录入近五天的观测数据，您可以稍后再试！");
	                myChart.hideLoading();
	            }
			},
			error:function(){
				alert("失败");	
			}
		});
		
		
	}
	Monitor();
	setInterval(Monitor,20000);
	
})();