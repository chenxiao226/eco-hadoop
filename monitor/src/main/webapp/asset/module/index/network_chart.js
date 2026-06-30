(function () {

    var WIN = 3 * 60 * 1000;
    var networkChart = null;
    var bytesInData = [];
    var bytesOutData = [];
    var lastInTime = null;
    var lastOutTime = null;

    var timeAxisLabel = {
        formatter: function (val) {
            var d = new Date(val);
            var pad = function (n) { return n < 10 ? '0' + n : n; };
            return (d.getMonth()+1) + '/' + pad(d.getDate()) + '\n' +
                pad(d.getHours()) + ':' + pad(d.getMinutes()) + ':' + pad(d.getSeconds());
        }
    };

    function fetchNetwork() {
        var params = { limit: 5 };
        if (lastInTime)  params.bytesinLastFetchTime  = lastInTime;
        if (lastOutTime) params.bytesoutLastFetchTime = lastOutTime;

        $.ajax({
            type: 'POST',
            url: monitorWebBaseURL + 'io/fetchIOByte_Pkts',
            data: params,
            dataType: 'json',
            success: function (data) {
                if (!data.success) return;
                var t = new Date().getTime();

                if (data.dataObject.BYTES_IN) {
                    $.each(data.dataObject.BYTES_IN, function (_, item) {
                        var ts = item.processTime * 1000;
                        bytesInData.push([ts, parseFloat(item.sum)]);
                        if (bytesInData.length > 300) bytesInData.shift();
                        lastInTime = item.processTime;
                    });
                }
                if (data.dataObject.BYTES_OUT) {
                    $.each(data.dataObject.BYTES_OUT, function (_, item) {
                        var ts = item.processTime * 1000;
                        bytesOutData.push([ts, parseFloat(item.sum)]);
                        if (bytesOutData.length > 300) bytesOutData.shift();
                        lastOutTime = item.processTime;
                    });
                }

                var end = t;
                networkChart.setOption({
                    series: [{ data: bytesInData }, { data: bytesOutData }],
                    xAxis: { min: end - WIN, max: end }
                });
            }
        });
    }

    $(function () {
        var dom = document.getElementById('networkChart');
        if (!dom) return;

        networkChart = echarts.init(dom);
        window.addEventListener('resize', function () { networkChart.resize(); });

        networkChart.setOption({
            title: { text: 'Cluster Network Throughput', top: 5, left: 5, textStyle: { fontSize: 13 } },
            tooltip: { trigger: 'axis' },
            legend: { data: ['Bytes In', 'Bytes Out'], top: 30 },
            toolbox: { feature: { saveAsImage: {} }, right: 10, top: 5 },
            dataZoom: [{ type: 'inside' }],
            grid: { left: '3%', right: '4%', top: 70, bottom: '5%', containLabel: true },
            xAxis: { type: 'time', splitLine: { show: false }, boundaryGap: false, axisLabel: timeAxisLabel },
            yAxis: { type: 'value', name: 'KB/s' },
            series: [
                { name: 'Bytes In',  type: 'line', smooth: true, symbol: 'none',
                  lineStyle: { color: '#409EFF' }, itemStyle: { color: '#409EFF' },
                  areaStyle: { color: 'rgba(64,158,255,0.2)' }, data: [] },
                { name: 'Bytes Out', type: 'line', smooth: true, symbol: 'none',
                  lineStyle: { color: '#F56C6C' }, itemStyle: { color: '#F56C6C' },
                  areaStyle: { color: 'rgba(245,108,108,0.2)' }, data: [] }
            ]
        });

        $.ajax({
            type: 'POST',
            url: monitorWebBaseURL + 'io/fetchIOByte_Pkts',
            data: { limit: 100 },
            dataType: 'json',
            success: function (data) {
                if (!data.success) return;
                if (data.dataObject.BYTES_IN) {
                    $.each(data.dataObject.BYTES_IN, function (_, item) {
                        bytesInData.unshift([item.processTime * 1000, parseFloat(item.sum)]);
                        lastInTime = item.processTime;
                    });
                }
                if (data.dataObject.BYTES_OUT) {
                    $.each(data.dataObject.BYTES_OUT, function (_, item) {
                        bytesOutData.unshift([item.processTime * 1000, parseFloat(item.sum)]);
                        lastOutTime = item.processTime;
                    });
                }
                networkChart.setOption({
                    series: [{ data: bytesInData }, { data: bytesOutData }]
                });
                setInterval(fetchNetwork, 8000);
            }
        });
    });

})();
