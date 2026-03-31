var DataNode = function(){
	
	function bulidDynamicChart(data){
		var myChart = echarts.init(document.getElementById('namenodeGC'));
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
        	    dataZoom : [
                    {
                        type: 'slider',    //支持鼠标滚轮缩放
                        start: 0,            //默认数据初始缩放范围为10%到90%
                        end: 100
                    },
                    {
                        type: 'inside',    //支持单独的滑动条缩放
                        start: 0,            //默认数据初始缩放范围为10%到90%
                        end: 100
                    }
                ],
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
        myChart.hideLoading();
        myChart.setOption(option);
        var lastData;
        var axisData;
        setInterval(function (){
        	
        	$.get(monitorWebBaseURL+"/DataNode/addData",{"processTime":processTime},function(data){
        		if(data!=null && data!=""){
        			//var data0 = option.series[0].data;
                	//lastData = data.value;
                	//axisData = data.time;
                	
                	//data0.shift();
                	option.series[0].data.push(data.value);
                    //option.xAxis[0].data.shift();
                    option.xAxis[0].data.push(data.time);
                    
                    myChart.setOption(option);
                    if(typeof(processTime)=="undefined"){
                    	process = process;
                    }
                    
                    processTime = typeof(processTime)!="undefined"?data.processTime:processTime;
        		}
//        		else{
//        			alert("null");
//        		}
            });
        }, 10000);
        
	}
	
	function initChart(){
		$.get(monitorWebBaseURL+"DataNode/getReceivedBytes", function(data){
			bulidDynamicChart(data);
		});
	}
	
	function initSquares(){
		$.get(monitorWebBaseURL+"namenode/getSquares.do", function(data){
			$("#TotalFiles").html(data.TotalFiles);
			$("#TotalBlocks").html(data.TotalBlocks);
			$("#CorruptBlocks").html(data.CorruptBlocks);
			$("#MissingBlocks").html(data.MissingBlocks);
			$("#PercentUsed").html(data.PercentUsed+"%");
		});
	}
	
//	function getContextPath(){   
//	    var pathName = document.location.pathname;   
//	    var index = pathName.substr(1).indexOf("/");   
//	    var result = pathName.substr(0,index+1);   
//	    return result;   
//	}  
	
	return {
		init:function(){
			initChart();
			//initSquares();
		}
	}
}();