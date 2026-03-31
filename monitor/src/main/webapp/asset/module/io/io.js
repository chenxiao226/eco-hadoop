var io = function(){
	
	function bulidDynamicChart(data){
		var myChart = echarts.init(document.getElementById(data.metricFlag));
		myChart.showLoading();
        var xAxisArray = [];
        var valueArray = [];
        var timeseries = data.timeseries;
        var processTime = timeseries[0].processTime;
        //processTime最大的放在队尾
        for(var i=0;i<timeseries.length;i++){
        	xAxisArray[timeseries.length-i-1] = timeseries[i].time;
        	valueArray[timeseries.length-i-1] = timeseries[i].value;
        }
        var option = {
        	    title : {
        	        text: data.metricName,
        	        subtext: data.metricNameZH
        	    },
        	    tooltip : {
        	        trigger: 'axis'
        	    },
        	    toolbox: {
        	        show : true,
        	        feature : {
        	            mark : {show: true},
        	            dataView : {show: true, readOnly: false},
        	            magicType : {show: true, type: ['line']},
        	            restore : {show: true},
        	            saveAsImage : {show: true}
        	        }
        	    },
        	    dataZoom : {
        	        show : false,
        	        start : 0,
        	        end : 100
        	    },
        	    xAxis : [
        	        {
        	            type : 'category',
        	            boundaryGap : true,
        	            data : xAxisArray
        	        }
        	    ],
        	    yAxis : [
        	        {
        	            type : 'value',
        	            scale: true,
        	            name : data.measurement,
        	            boundaryGap: [0.2, 0.2]
        	        }
        	    ],
        	    series : [
        	        {
        	            name:data.metricName,
        	            type:'line',
        	            data:valueArray
        	        }
        	    ]
        	};
        var lastData;
        var axisData;
        setInterval(function (){ 
        	myChart.hideLoading();
        	$.getJSON(getContextPath()+"/io/addData.do",{"metricFlag":data.metricFlag,"processTime":processTime},function(data){
        		if(data!=null && data!=""){
        			var data0 = option.series[0].data;
                	lastData = data.value;
                	axisData = data.time;
                	
                	data0.shift();
                    data0.push(lastData);
                    option.xAxis[0].data.shift();
                    option.xAxis[0].data.push(axisData);
                    
                    myChart.setOption(option);
                    
                    processTime = typeof(processTime)!="undefined"?data.processTime:processTime;
        		}
            });
        }, 10000);
       
	}
	
//	function bulidThreadsDynamicChart(data){
//		var myChart = echarts.init(document.getElementById("THREADS"));
//		myChart.showLoading();
//        var blocked_xAxisArray = [];
//        var waiting_xAxisArray = [];
//        var blocked_valueArray = [];
//        var waiting_valueArray = [];
//        var blocked_data = data[3];
//        var waiting_data = data[4];
//        var blocked_timeseries = blocked_data.timeseries;
//        var waiting_timeseries = waiting_data.timeseries;
//        var blocked_processTime = blocked_timeseries[0].processTime;
//        var waiting_processTime = waiting_timeseries[0].processTime;
//        //processTime最大的放在队尾
//        for(var i=0;i<blocked_timeseries.length;i++){
//        	blocked_xAxisArray[blocked_timeseries.length-i-1] = blocked_timeseries[i].time;
//        	blocked_valueArray[blocked_timeseries.length-i-1] = blocked_timeseries[i].value;
//        }
//        for(var i=0;i<waiting_timeseries.length;i++){
//        	waiting_xAxisArray[waiting_timeseries.length-i-1] = waiting_timeseries[i].time;
//        	waiting_valueArray[waiting_timeseries.length-i-1] = waiting_timeseries[i].value;
//        }
//        var option = {
//        	    title : {
//        	        text: "ThreadsBlocked/Waiting",
//        	        subtext: "线程阻塞、线程等待数量"
//        	    },
//        	    tooltip : {
//        	        trigger: 'axis'
//        	    },
//        	    legend: {
//        	        data:['ThreadsBlocked', 'ThreadsWaiting']
//        	    },
//        	    toolbox: {
//        	        show : true,
//        	        feature : {
//        	            mark : {show: true},
//        	            dataView : {show: true, readOnly: false},
//        	            magicType : {show: true, type: ['line']},
//        	            restore : {show: true},
//        	            saveAsImage : {show: true}
//        	        }
//        	    },
//        	    dataZoom : {
//        	        show : false,
//        	        start : 0,
//        	        end : 100
//        	    },
//        	    xAxis : [
//        	        {
//        	            type : 'category',
//        	            boundaryGap : true,
//        	            data : blocked_xAxisArray
//        	        },
//        	        {
//        	            type : 'category',
//        	            boundaryGap : true,
//        	            data : waiting_xAxisArray
//        	        }
//        	    ],
//        	    yAxis : [
//        	        {
//        	            type : 'value',
//        	            scale: true,
//        	            name : data.measurement,
//        	            boundaryGap: [0.2, 0.2]
//        	        },
//        	        {
//        	            type : 'value',
//        	            scale: true,
//        	            name : data.measurement,
//        	            boundaryGap: [0.2, 0.2]
//        	        }
//        	    ],
//        	    series : [
//        	        {
//        	            name:data[3].metricName,
//        	            type:'line',
//        	            data:blocked_valueArray
//        	        },
//        	        {
//        	            name:data[4].metricName,
//        	            type:'line',
//        	            data:waiting_valueArray
//        	        }
//        	    ]
//        	};
//        var blocked_lastData;
//        var blocked_axisData;
//        var waiting_lastData;
//        var waiting_axisData;
//        setInterval(function (){
//        	myChart.hideLoading();
//        	$.getJSON(getContextPath()+"/io/addThreadsData.do",{"waiting_processTime":waiting_processTime,"blocked_processTime":blocked_processTime},function(data){
//        		if(data!=null && data!=""){
//        			var data0 = option.series[0].data;
//        			var data1 = option.series[1].data;
//        			blocked_lastData = data[0].value;
//        			blocked_axisData = data[0].time;
//        			waiting_lastData = data[1].value;
//        	        waiting_axisData = data[1].time;
//                	
//                	data0.shift();
//                    data0.push(blocked_lastData);
//                    data1.shift();
//                    data1.push(waiting_lastData);
//                    option.xAxis[0].data.shift();
//                    option.xAxis[0].data.push(blocked_axisData);
//                    option.xAxis[1].data.shift();
//                    option.xAxis[1].data.push(waiting_axisData);
//                    
//                    myChart.setOption(option);
//                    
//                    blocked_processTime = typeof(blocked_processTime)!="undefined"?data[0].processTime:blocked_processTime;
//                    waiting_processTime = typeof(waiting_processTime)!="undefined"?data[1].processTime:waiting_processTime;
//        		}
//            });
//        }, 10000);
//	}
	
	function initChart(){
		$.getJSON(getContextPath()+"/io/getDisk", function(data){
			for(var i=0;i<data.length;i++)
				bulidDynamicChart(data[i]);
//			bulidThreadsDynamicChart(data);
		});
	}
	
	function initSquares(){
		$.getJSON(getContextPath()+"/io/getSquares", function(data){
			$("#BytesIn").html(data.BytesIn);
			$("#BytesOut").html(data.BytesOut);
			$("#PktsIn").html(data.PktsIn);
			$("#PktsOut").html(data.PktsOut);
			$("#PacketLossRate").html(data.PacketLossRate);
			$("#PercentUsed").html(data.PercentUsed+"%");
		});
	}
	
	function getContextPath(){   
	    var pathName = document.location.pathname;   
	    var index = pathName.substr(1).indexOf("/");   
	    var result = pathName.substr(0,index+1);   
	    return result;   
	}  
	
	return {
		init:function(){
			initChart();
//			initSquares();
		}
	}
}();
//最后的括号表示立即执行，否则相当于仅仅声明函数，没有赋值给变量