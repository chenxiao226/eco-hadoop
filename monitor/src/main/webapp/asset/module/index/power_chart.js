(function () {
    var powerChart = null;
    var masterPowerData = [];
    var slave1PowerData = [];
    var slave2PowerData = [];
    var avgPowerData = [];

    function fetchInitial(url, dataArray, callback) {
        $.ajax({
            type: "POST",
            url: monitorWebBaseURL + url,
            data: { limit: 600 },
            dataType: "json",
            success: function (data) {
                if (data.success && data.dataObject) {
                    dataArray.length = 0;
                    $.each(data.dataObject, function (i, item) {
                        dataArray.unshift([item.processTime * 1000, item.sum]);
                    });
                }
                if (callback) callback();
            }
        });
    }

    function fetchLatest(url, dataArray, callback) {
        $.ajax({
            type: "POST",
            url: monitorWebBaseURL + url,
            data: { limit: 10, start: 0 },
            dataType: "json",
            success: function (data) {
                if (data.success && data.dataObject) {
                    $.each(data.dataObject, function (i, item) {
                        dataArray.shift();
                        dataArray.push([item.processTime * 1000, item.sum]);
                    });
                }
                if (callback) callback();
            }
        });
    }

    // 按时间戳对齐，计算三节点Avg Power
    function calcAvg() {
        avgPowerData = [];
        var len = Math.min(masterPowerData.length, slave1PowerData.length, slave2PowerData.length);
        for (var i = 0; i < len; i++) {
            var ts = masterPowerData[i][0];
            var avg = (masterPowerData[i][1] + slave1PowerData[i][1] + slave2PowerData[i][1]) / 3;
            avgPowerData.push([ts, parseFloat(avg.toFixed(2))]);
        }
    }

    function updatePowerChart() {
        if (!powerChart) return;
        calcAvg();
        var last = avgPowerData.length > 0 ? avgPowerData[avgPowerData.length - 1][1] : null;
        if (last !== null) {
            $("#avgPowerNum").html(last + ' <small>KW</small>');
        }
        powerChart.setOption({
            series: { name: 'Avg Power', data: avgPowerData }
        });
    }

    function fetchLatestAll() {
        if (masterPowerData.length === 0) return;
        fetchLatest("index/masterpower", masterPowerData, function () {
            fetchLatest("index/slave1power", slave1PowerData, function () {
                fetchLatest("index/slave2power", slave2PowerData, function () {
                    updatePowerChart();
                });
            });
        });
    }

    $(function () {
        var powerChartDom = document.getElementById('powerChart');
        if (!powerChartDom) return;

        powerChart = echarts.init(powerChartDom);
        window._powerChart = powerChart;
        window.addEventListener('resize', function () { powerChart.resize(); });

        powerChart.setOption({
            title: {
                text: 'Cluster Average Power',
                top: 5,
                left: 5
            },
            grid: {
                height: 165
            },
            tooltip: {
                trigger: 'axis'
            },
            toolbox: {
                feature: {
                    dataZoom: { yAxisIndex: 'none' },
                    restore: {},
                    saveAsImage: {}
                },
                top: 5,
                right: 20
            },
            dataZoom: [
                { type: 'slider', start: 50, end: 100 }
            ],
            visualMap: {
                top: 10,
                right: 10,
                pieces: [{
                    gt: 0,
                    lte: 18,
                    color: '#660099'
                }, {
                    gt: 18,
                    lte: 22,
                    color: '#ffde33'
                }, {
                    gt: 22,
                    lte: 26,
                    color: '#ff9933'
                }, {
                    gt: 26,
                    color: '#ff0000'
                }],
                outOfRange: { color: '#999' },
                showLabel: false,
                show: false
            },
            xAxis: {
                type: 'time',
                splitLine: { show: false },
                boundaryGap: false
            },
            yAxis: {
                type: 'value',
                name: 'KW',
                min: 14,
                max: 30,
                splitLine: { show: false }
            },
            series: [{
                name: 'Avg Power',
                type: 'line',
                data: [],
                smooth: true,
                symbol: 'none',
                areaStyle: { normal: {} },
                markLine: {
                    silent: true,
                    data: [
                        { yAxis: 18 },
                        { yAxis: 22 },
                        { yAxis: 26 },
                        { yAxis: 30 }
                    ]
                }
            }]
        });

        powerChart.showLoading();

        fetchInitial("index/masterpower", masterPowerData, function () {
            fetchInitial("index/slave1power", slave1PowerData, function () {
                fetchInitial("index/slave2power", slave2PowerData, function () {
                    powerChart.hideLoading();
                    updatePowerChart();
                    setInterval(fetchLatestAll, 8000);
                });
            });
        });
    });
})();