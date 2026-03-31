/**
 * Hadoop Job Monitoring - TeraSort Task
 * 静态数据 + 动态图表 (2025-10-31 20:00-21:00)
 */
// 在您的JS文件开头添加这个修复代码

var HadoopJob = (function() {
    'use strict';

    console.log("🚀 Loading Hadoop Job Monitor...");

    // TeraSort任务静态数据
    const staticData = {
        mrRunningNum: "3",    // 运行中的MapReduce任务
        mrFailedNum: "0",     // 失败的任务数
        mrKilledNum: "0",     // 被终止的任务数
        mrFinishedNum: "12",  // 已完成的任务数
        appCount: "1"         // 当前应用数 (TeraSort)
    };

    // 动态数据 - TeraSort任务时间序列
    const dynamicData = {
        // Map任务进度 (20:00-21:00)
        mapProgress: generateTimeSeriesData(60, 0, 100, '2025-10-31T20:00:00', 1.5),
        // Reduce任务进度
        reduceProgress: generateTimeSeriesData(60, 0, 100, '2025-10-31T20:00:00', 1.2),
        // 任务吞吐量 (records/sec)
        throughput: generateTimeSeriesData(60, 1000, 5000, '2025-10-31T20:00:00', 0.8)
    };

    // 生成时间序列数据
    function generateTimeSeriesData(points, min, max, startTime, trend) {
        const data = [];
        const baseTime = new Date(startTime).getTime();
        let currentValue = min;

        for (let i = 0; i < points; i++) {
            // 模拟TeraSort任务进度趋势
            const progress = (i / points) * (max - min) * trend;
            const randomValue = Math.min(max, Math.max(min, currentValue + progress + (Math.random() - 0.5) * (max - min) * 0.1));
            currentValue = randomValue;

            data.push({
                time: new Date(baseTime + i * 60000), // 每分钟一个点
                value: Math.round(currentValue * 100) / 100
            });
        }
        return data;
    }

    // 初始化静态数据
    function initStaticData() {
        console.log("📊 Initializing Hadoop Job static data...");

        try {
            // 设置MapReduce任务统计
            setTextSafely('mrRunningNum', staticData.mrRunningNum);
            setTextSafely('mrFailedNum', staticData.mrFailedNum);
            setTextSafely('mrKilledNum', staticData.mrKilledNum);
            setTextSafely('mrFinishedNum', staticData.mrFinishedNum);
            setTextSafely('hadoopAppCount', staticData.appCount);

            console.log("✅ Hadoop Job static data initialized");
        } catch (error) {
            console.error("❌ Hadoop Job static data error:", error);
        }
    }

    // 初始化Hadoop作业图表
    function initHadoopChart() {
        const chartElement = document.getElementById('hadoopAppChart');
        if (!chartElement || typeof echarts === 'undefined') {
            console.warn("Hadoop chart element or ECharts not available");
            return;
        }

        const chart = echarts.init(chartElement);
        const option = {
            title: {
                text: 'TeraSort Task Progress (2025-10-31 20:00-21:00)',
                left: 'center',
                textStyle: { fontSize: 14, fontWeight: 'bold' }
            },
            tooltip: {
                trigger: 'axis',
                formatter: function(params) {
                    const date = new Date(params[0].axisValue);
                    const timeStr = date.getHours() + ':' + (date.getMinutes()<10?'0':'') + date.getMinutes();
                    let result = `Time: ${timeStr}<br/>`;
                    params.forEach(item => {
                        result += `${item.marker} ${item.seriesName}: ${item.value[1].toFixed(1)}%<br/>`;
                    });
                    return result;
                }
            },
            legend: {
                data: ['Map Progress', 'Reduce Progress', 'Throughput'],
                top: 30
            },
            grid: {
                left: '3%',
                right: '4%',
                bottom: '10%',
                containLabel: true
            },
            xAxis: {
                type: 'time',
                name: 'Time'
            },
            yAxis: [{
                type: 'value',
                name: 'Progress (%)',
                min: 0,
                max: 100,
                axisLabel: {
                    formatter: '{value}%'
                }
            }, {
                type: 'value',
                name: 'Throughput (rec/sec)',
                min: 0
            }],
            series: [
                {
                    name: 'Map Progress',
                    type: 'line',
                    smooth: true,
                    data: dynamicData.mapProgress.map(item => [item.time, item.value]),
                    lineStyle: { color: '#c23531', width: 2 },
                    areaStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: 'rgba(194, 53, 49, 0.3)' },
                            { offset: 1, color: 'rgba(194, 53, 49, 0.1)' }
                        ])
                    }
                },
                {
                    name: 'Reduce Progress',
                    type: 'line',
                    smooth: true,
                    data: dynamicData.reduceProgress.map(item => [item.time, item.value]),
                    lineStyle: { color: '#61a0a8', width: 2 },
                    areaStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: 'rgba(97, 160, 168, 0.3)' },
                            { offset: 1, color: 'rgba(97, 160, 168, 0.1)' }
                        ])
                    }
                },
                {
                    name: 'Throughput',
                    type: 'line',
                    yAxisIndex: 1,
                    smooth: true,
                    data: dynamicData.throughput.map(item => [item.time, item.value]),
                    lineStyle: { color: '#d48265', width: 2 }
                }
            ],
            dataZoom: [{
                type: 'inside',
                start: 0,
                end: 100
            }]
        };

        chart.setOption(option);
        console.log("✅ Hadoop job chart initialized");
        return chart;
    }

    // 安全设置文本
    function setTextSafely(elementId, text) {
        const element = document.getElementById(elementId);
        if (element) {
            element.textContent = text;
        }
    }

    // 动态更新数据
    function startDynamicUpdates() {
        setInterval(updateChartData, 10000);
        console.log("🔄 Hadoop job dynamic updates started (10s interval)");
    }

    function updateChartData() {
        // 模拟数据更新
        updateDataset(dynamicData.mapProgress, 0, 100);
        updateDataset(dynamicData.reduceProgress, 0, 100);
        updateDataset(dynamicData.throughput, 1000, 5000);

        console.log("📈 Hadoop job data updated");
    }

    function updateDataset(dataset, min, max) {
        if (dataset.length === 0) return;

        const lastPoint = dataset[dataset.length - 1];
        const newValue = Math.max(min, Math.min(max, lastPoint.value + (Math.random() - 0.5) * (max - min) * 0.1));
        const newTime = new Date(lastPoint.time.getTime() + 60000);

        dataset.push({
            time: newTime,
            value: Math.round(newValue * 100) / 100
        });

        if (dataset.length > 60) dataset.shift();
    }

    // 主初始化函数
    function init() {
        console.log("🎯 Initializing Hadoop Job Monitor...");

        try {
            initStaticData();
            initHadoopChart();
            startDynamicUpdates();

            console.log("✅ Hadoop Job Monitor initialized successfully");
        } catch (error) {
            console.error("❌ Hadoop Job Monitor initialization failed:", error);
        }
    }

    // 公共API
    return {
        init: init,
        getStatus: function() {
            return {
                static: staticData,
                lastUpdate: new Date().toISOString()
            };
        }
    };
})();

// 全局可用性
if (typeof window !== 'undefined') {
    window.HadoopJob = HadoopJob;
}

console.log("✅ Hadoop Job module loaded");