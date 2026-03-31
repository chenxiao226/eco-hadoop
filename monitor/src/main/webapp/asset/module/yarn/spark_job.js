/**
 * Created by likeugo on 2017/6/4
 */

(function () {

    var appChart = null;
    var mapReduceRunningData = []; //运行
    var mapReduceKilledData=[]; //kill
    var mapReduceAcceptedData=[]; //接受
    var mapReduceNewData=[];//新的
    var mapReduceSubmittedData=[];//提交
    var mapReduceFinishedData=[];//已完成
    var mapReduceNew_savingData=[]; //
    var mapReduceFailedData=[]; //失败
    var mr_max = 0;
    
    
    
    var sparkRunningData = [];
    var monitorWebBaseURL = "/monitor/";
    // 获取应用运行数据
    function fetchAppDataInterval() {
        $.ajax({
            type: "POST",
            url: monitorWebBaseURL + "index/app",
            data: {},
            dataType: "json",
            success: function (data) {
                if (data != null) {

                    var mrRunning=0, mrKilled=0, mrAccepted=0, mrNew=0, mrSubmitted=0, mrFinished=0, mrNew_saving=0, mrFailed=0;
                    var spRunning=0, spKilled=0, spAccepted=0, spNew=0, spSubmitted=0, spFinished=0, spNew_saving=0, spFailed=0;

                    $.each(data.mapReduceApp.appStatInfo.statItem, function (i, item) {
                        switch (item.state) {
                            case 'RUNNING':
                                mrRunning = item.count; break;
                            case 'KILLED' :
                                mrKilled = item.count; break;
                            case 'ACCEPTED' :
                                mrAccepted = item.count; break;
                            case 'NEW' :
                                mrNew = item.count; break;
                            case 'SUBMITTED' :
                                mrSubmitted = item.count; break;
                            case 'FINISHED' :
                                mrFinished = item.count; break;
                            case 'NEW_SAVING' :
                                mrNew_saving = item.count; break;
                            case 'FAILED' :
                                mrFailed = item.count;
                        }
                    });

                    $.each(data.sparkApp.appStatInfo.statItem, function (i, item) {
                        switch (item.state) {
                            case 'RUNNING':
                                spRunning = item.count; break;
                            case 'KILLED' :
                                spKilled = item.count; break;
                            case 'ACCEPTED' :
                                spAccepted = item.count; break;
                            case 'NEW' :
                                spNew = item.count; break;
                            case 'SUBMITTED' :
                                spSubmitted = item.count; break;
                            case 'FINISHED' :
                                spFinished = item.count; break;
                            case 'NEW_SAVING' :
                                spNew_saving = item.count; break;
                            case 'FAILED' :
                                spFailed = item.count; 
                        }
                    });

                    var now = new Date().getTime();
                    if(mapReduceRunningData.length>=200){ //running
                        mapReduceRunningData.shift(); //删除第一个
                        mapReduceKilledData.shift(); //删除第一个
                        mapReduceAcceptedData.shift();
                        mapReduceNewData.shift(); //删除第一个
                        mapReduceSubmittedData.shift();//提交
                        mapReduceFinishedData.shift();//已完成
                        mapReduceNew_savingData.shift(); //
                        mapReduceFailedData.shift(); //失败
                    }
                    if(sparkRunningData.length>=200){
                    	sparkRunningData.shift();
                    }
                  
                   
                    
                    mapReduceRunningData.push([now,mrRunning]);
                    mapReduceKilledData.push([now,mrKilled]);
                    mapReduceAcceptedData.push([now,mrAccepted]);
                    mapReduceNewData.push([now,mrNew]);
                    mapReduceSubmittedData.push([now,mrSubmitted]);
                    mapReduceFinishedData.push([now,mrFinished]);
                    mapReduceNew_savingData.push([now,mrNew_saving]);
                    mapReduceFailedData.push([now,mrFailed]);
                    
                    //选择一个较大的值
                    if(mrRunning>mr_max){
                    	mr_max = mrRunning+2;
                    }
                    if(mrKilled>mr_max){
                    	mr_max = mrKilled+2;
                    }
                    if(mrAccepted>mr_max){
                    	mr_max = mrAccepted+2;
                    }
                    if(mrNew>mr_max){
                    	mr_max = mrNew+2;
                    }
                    if(mrSubmitted>mr_max){
                    	mr_max = mrSubmitted+2;
                    }
                    if(mrFinished>mr_max){
                    	mr_max = mrFinished+2;
                    }
                    if(mrNew_saving>mr_max){
                    	mr_max = mrNew_saving+2;
                    }
                    if(mrFailed>mr_max){
                    	mr_max = mrFailed+2;
                    }
          
                    
                    
                    
                    sparkRunningData.push([now,spRunning]);

                    // 填入数据
                    appChart.setOption({
                    	 yAxis: {
                             type: 'value',
                             min: 0,
                             minInterval: 1,
                             max :mr_max
                         },
                        series: [
                            {
                                name: '运行中',
                                data: mapReduceRunningData
                            },
                            {
                                name: '已完成',
                                data: mapReduceFinishedData
                            }
                            ,
                            {
                                name: '已杀掉',
                                data: mapReduceKilledData
                            }
                        ]
                    });
                }
            }
        });
    }

    $(function () {

        // 基于准备好的dom，初始化echarts实例
        appChart = echarts.init(document.getElementById('hadoopAppChart'));

        // 设置图表属性
        appChart.setOption({
            title: {
                text: 'Hadoop运行任务',
                top: 5,
                left: 5
            },
            grid:{
                height: 190,
                left: '3%',
                right: '4%',
                containLabel: true
           
            },
            tooltip : {
                trigger: 'axis',
                axisPointer: {
                    type: 'cross',
                    label: {
                        backgroundColor: '#6a7985'
                    }
                }
            },
            legend: {
                data:['运行中','已完成','已杀掉'],
                //bottom:'bottom',
                left:'50%',
                width:'200px',
                
            },
            toolbox: {
                feature: {
                    dataZoom: {
                        yAxisIndex: 'none'
                    },
                    restore: {},
                    saveAsImage: {},
                    dataView:{}
                }
            },
            dataZoom: [
                {   // 这个dataZoom组件，默认控制x轴。
                    type: 'slider', // 这个 dataZoom 组件是 slider 型 dataZoom 组件
                    start: 50,      // 左边在 10% 的位置。
                    end: 100,         // 右边在 60% 的位置。
                }
            ],
            xAxis: {
                type: 'time',
                splitLine: {
                    show: false
                },
                boundaryGap : false
            },
            yAxis: {
                type: 'value',
                min: 0,
                minInterval: 3
            },
            series: [
                {
                    name: '运行中',
                    type: 'line',
                   // areaStyle: {normal: {}},
                    data: []
                },
                {
                    name: '已完成',
                    type: 'line',
                   // areaStyle: {normal: {}},
                    data: []
                },
                {
                    name: '已杀掉',
                    type: 'line',
                   // areaStyle: {normal: {}},
                    data: []
                },
            ]
        });

        // 加载提示动画
        appChart.showLoading();

        fetchAppDataInterval();

        // 关闭提示动画
        appChart.hideLoading();

        setInterval(fetchAppDataInterval, 8000);
    });
})();

