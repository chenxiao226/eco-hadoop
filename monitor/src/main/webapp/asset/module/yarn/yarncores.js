/**
 * YARN CPU Cores Monitoring - TeraSort Task
 * CPU核心使用情况监控 (2025-10-31 20:00-21:00)
 */

var YarnCores = (function() {
    'use strict';

    console.log("🚀 Loading YARN Cores Monitor...");

    // TeraSort任务CPU核心静态数据
    const staticData = {
        coresTotal: "32",       // 总核心数
        coresAvailable: "16",   // 可用核心
        coresAllocated: "8",    // 已分配核心
        coresReserved: "4"      // 预留核心
    };

    // 动态核心使用数据
    const dynamicData = {
        total: generateTimeSeriesData(60, 30, 32, '2025-10-31T20:00:00', 0),
        available: generateTimeSeriesData(60, 12, 20, '2025-10-31T20:00:00', -0.1),
        allocated: generateTimeSeriesData(60, 6, 10, '2025-10-31T20:00:00', 0.15),
        reserved: generateTimeSeriesData(60, 2, 6, '2025-10-31T20:00:00', 0.08)
    };

    // 生成时间序列数据
    function generateTimeSeriesData(points, min, max, startTime, trend) {
        const data = [];
        const baseTime = new Date(startTime).getTime();
        let currentValue = (min + max) / 2;

        for (let i = 0; i < points; i++) {
            const change = trend * (max - min) * 0.01;
            const randomValue = Math.min(max, Math.max(min, currentValue + change + (Math.random() - 0.5) * 2));
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
        console.log("⚡ Initializing CPU cores static data...");

        setTextSafely('coresTotal', staticData.coresTotal);
        setTextSafely('coresAvailable', staticData.coresAvailable);
        setTextSafely('coresAllocated', staticData.coresAllocated);
        setTextSafely('coresReserved', staticData.coresReserved);

        console.log("✅ CPU cores static data initialized");
    }

    // 初始化CPU核心图表
    function initCoresChart() {
        const chartElement = document.getElementById('coresChart');
        if (!chartElement || typeof echarts === 'undefined') {
            console.warn("Cores chart element or ECharts not available");
            return;
        }

        const chart = echarts.init(chartElement);
        const option = {
            title: {
                text: 'YARN CPU Cores Usage - TeraSort Task (20:00-21:00)',
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
                        result += `${item.marker} ${item.seriesName}: ${item.value[1]} cores<br/>`;
                    });
                    return result;
                }
            },
            legend: {
                data: ['Total Cores', 'Available', 'Allocated', 'Reserved'],
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
                name: 'CPU Cores',
                min: 0
            },
            series: [
                {
                    name: 'Total Cores',
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
                    lineStyle: { color: '#91c7ae', width: 2 },
                    areaStyle: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                            { offset: 0, color: 'rgba(145, 199, 174, 0.3)' },
                            { offset: 1, color: 'rgba(145, 199, 174, 0.1)' }
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
        console.log("✅ CPU cores chart initialized");
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
        console.log("🔄 CPU cores dynamic updates started");
    }

    function updateChartData() {
        updateDataset(dynamicData.available, 12, 20);
        updateDataset(dynamicData.allocated, 6, 10);
        updateDataset(dynamicData.reserved, 2, 6);

        console.log("📈 CPU cores data updated");
    }

    function updateDataset(dataset, min, max) {
        if (dataset.length === 0) return;

        const lastPoint = dataset[dataset.length - 1];
        const newValue = Math.max(min, Math.min(max, lastPoint.value + (Math.random() - 0.5) * 2));
        const newTime = new Date(lastPoint.time.getTime() + 60000);

        dataset.push({ time: newTime, value: Math.round(newValue) });
        if (dataset.length > 60) dataset.shift();
    }

    // 主初始化
    function init() {
        console.log("🎯 Initializing YARN Cores Monitor...");

        try {
            initStaticData();
            initCoresChart();
            startDynamicUpdates();

            console.log("✅ YARN Cores Monitor initialized successfully");
        } catch (error) {
            console.error("❌ YARN Cores Monitor initialization failed:", error);
        }
    }

    return {
        init: init,
        getCoreUsage: function() {
            return {
                total: staticData.coresTotal + ' cores',
                allocated: staticData.coresAllocated + ' cores',
                utilization: Math.round((parseInt(staticData.coresAllocated) / parseInt(staticData.coresTotal)) * 100) + '%'
            };
        }
    };
})();

if (typeof window !== 'undefined') {
    window.YarnCores = YarnCores;
}

console.log("✅ YARN Cores module loaded");