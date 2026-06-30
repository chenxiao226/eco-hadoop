/**
 * YARN Memory Monitoring - TeraSort Task
 * 内存使用情况监控 (2025-10-31 20:00-21:00)
 */
 // 在您的JS文件开头添加这个修复代码

var YarnMemory = (function() {
    'use strict';

    console.log("🚀 Loading YARN Memory Monitor...");

    // TeraSort任务内存使用静态数据
    const staticData = {
        memTotal: "16384",      // 总内存 16GB
        memAvailable: "8192",   // 可用内存 8GB
        memAllocated: "4096",    // 已分配内存 4GB
        memReserved: "2048"     // 预留内存 2GB
    };

    // 动态内存使用数据
    const dynamicData = {
        total: generateTimeSeriesData(60, 15000, 16384, '2025-10-31T20:00:00', 0),
        available: generateTimeSeriesData(60, 6000, 9000, '2025-10-31T20:00:00', -0.1),
        allocated: generateTimeSeriesData(60, 3000, 5000, '2025-10-31T20:00:00', 0.2),
        reserved: generateTimeSeriesData(60, 1000, 3000, '2025-10-31T20:00:00', 0.1)
    };

    // 生成时间序列数据
    function generateTimeSeriesData(points, min, max, startTime, trend) {
        const data = [];
        const baseTime = new Date(startTime).getTime();
        let currentValue = (min + max) / 2;

        for (let i = 0; i < points; i++) {
            const change = trend * (max - min) * 0.01;
            const randomValue = Math.min(max, Math.max(min, currentValue + change + (Math.random() - 0.5) * (max - min) * 0.05));
            currentValue = randomValue;

            data.push({
                time: new Date(baseTime + i * 60000),
                value: Math.round(currentValue)
            });
        }
        return data;
    }

    // 初始化静态数据
    function initStaticData() {
        console.log("💾 Initializing memory static data...");

        setTextSafely('memTotal', staticData.memTotal + ' MB');
        setTextSafely('memAvailable', staticData.memAvailable + ' MB');
        setTextSafely('memAllocated', staticData.memAllocated + ' MB');
        setTextSafely('memReserved', staticData.memReserved + ' MB');

        console.log("✅ Memory static data initialized");
    }

    // 初始化内存图表
    function initMemoryChart() {
        const chartElement = document.getElementById('memChart');
        if (!chartElement || typeof echarts === 'undefined') {
            console.warn("Memory chart element or ECharts not available");
            return;
        }

        const chart = echarts.init(chartElement);
        const option = {
            title: {
                text: 'YARN Memory Usage - TeraSort Task (20:00-21:00)',
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
                        result += `${item.marker} ${item.seriesName}: ${(item.value[1]/1024).toFixed(1)} GB<br/>`;
                    });
                    return result;
                }
            },
            legend: {
                data: ['Total Memory', 'Available', 'Allocated', 'Reserved'],
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
            yAxis: {
                type: 'value',
                name: 'Memory (MB)'
            },
            series: [
                {
                    name: 'Total Memory',
                    type: 'line',
                    smooth: true,
                    data: dynamicData.total.map(item => [item.time, item.value]),
                    lineStyle: { color: '#2f4554', width: 3 }
                },
                {
                    name: 'Available',
                    type: 'line',
                    smooth: true,
                    data: dynamicData.available.map(item => [item.time, item.value]),
                    lineStyle: { color: '#61a0a8', width: 2 },
                    areaStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: 'rgba(97, 160, 168, 0.3)' },
                            { offset: 1, color: 'rgba(97, 160, 168, 0.1)' }
                        ])
                    }
                },
                {
                    name: 'Allocated',
                    type: 'line',
                    smooth: true,
                    data: dynamicData.allocated.map(item => [item.time, item.value]),
                    lineStyle: { color: '#c23531', width: 2 }
                },
                {
                    name: 'Reserved',
                    type: 'line',
                    smooth: true,
                    data: dynamicData.reserved.map(item => [item.time, item.value]),
                    lineStyle: { color: '#d48265', width: 2 }
                }
            ],
            dataZoom: [{
                type: 'inside'
            }]
        };

        chart.setOption(option);
        console.log("✅ Memory chart initialized");
        return chart;
    }

    // 安全设置文本
    function setTextSafely(elementId, text) {
        const element = document.getElementById(elementId);
        if (element) element.textContent = text;
    }

    // 动态更新
    function startDynamicUpdates() {
        setInterval(updateChartData, 10000);
        console.log("🔄 Memory dynamic updates started");
    }

    function updateChartData() {
        updateDataset(dynamicData.available, 6000, 9000);
        updateDataset(dynamicData.allocated, 3000, 5000);
        updateDataset(dynamicData.reserved, 1000, 3000);

        console.log("📈 Memory data updated");
    }

    function updateDataset(dataset, min, max) {
        if (dataset.length === 0) return;

        const lastPoint = dataset[dataset.length - 1];
        const newValue = Math.max(min, Math.min(max, lastPoint.value + (Math.random() - 0.5) * 200));
        const newTime = new Date(lastPoint.time.getTime() + 60000);

        dataset.push({ time: newTime, value: Math.round(newValue) });
        if (dataset.length > 60) dataset.shift();
    }

    // 主初始化
    function init() {
        console.log("🎯 Initializing YARN Memory Monitor...");

        try {
            initStaticData();
            initMemoryChart();
            startDynamicUpdates();

            console.log("✅ YARN Memory Monitor initialized successfully");
        } catch (error) {
            console.error("❌ YARN Memory Monitor initialization failed:", error);
        }
    }

    return {
        init: init,
        getMemoryUsage: function() {
            return {
                total: staticData.memTotal + ' MB',
                available: staticData.memAvailable + ' MB',
                usage: Math.round((parseInt(staticData.memAllocated) / parseInt(staticData.memTotal)) * 100) + '%'
            };
        }
    };
})();

if (typeof window !== 'undefined') {
    window.YarnMemory = YarnMemory;
}

console.log("✅ YARN Memory module loaded");