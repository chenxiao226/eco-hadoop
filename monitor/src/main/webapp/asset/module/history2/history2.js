$(document).ready(function() {
    var monitorWebBaseURL = "/history2/energyhistory/";
    FastClick.attach(document.body);

    $('.data-table').slimScroll({
        height: '500px'
    });

    // 扩展的模拟数据（更多数据用于测试过滤）
    const mockData = [
        {
            type: 'HadoopWordcount',
            date: '4/22/2024',
            time: '21:17:23',
            inputSize: 36452,
            duration: 29.19,
            throughput: 1248,
            throughputNode: 416,
            predicted: 0.0071,
            actual: 0.0050
        },
        {
            type: 'SparkWordcount',
            date: '4/22/2024',
            time: '21:18:45',
            inputSize: 42156,
            duration: 25.32,
            throughput: 1665,
            throughputNode: 555,
            predicted: 0.0065,
            actual: 0.0048
        },
        {
            type: 'HadoopSort',
            date: '4/22/2024',
            time: '21:20:12',
            inputSize: 38945,
            duration: 31.45,
            throughput: 1238,
            throughputNode: 413,
            predicted: 0.0082,
            actual: 0.0061
        },
        {
            type: 'SparkSort',
            date: '4/22/2024',
            time: '21:22:01',
            inputSize: 41235,
            duration: 27.89,
            throughput: 1478,
            throughputNode: 493,
            predicted: 0.0068,
            actual: 0.0052
        },
        {
            type: 'HadoopTerasort',
            date: '4/22/2024',
            time: '21:24:33',
            inputSize: 45632,
            duration: 35.67,
            throughput: 1279,
            throughputNode: 426,
            predicted: 0.0078,
            actual: 0.0059
        },
        {
            type: 'SparkTerasort',
            date: '4/22/2024',
            time: '21:26:55',
            inputSize: 47823,
            duration: 30.12,
            throughput: 1587,
            throughputNode: 529,
            predicted: 0.0063,
            actual: 0.0047
        },
        {
            type: 'HadoopKmeans',
            date: '4/22/2024',
            time: '21:29:14',
            inputSize: 39876,
            duration: 33.45,
            throughput: 1191,
            throughputNode: 397,
            predicted: 0.0084,
            actual: 0.0063
        },
        {
            type: 'SparkKmeans',
            date: '4/22/2024',
            time: '21:31:42',
            inputSize: 43567,
            duration: 28.93,
            throughput: 1506,
            throughputNode: 502,
            predicted: 0.0069,
            actual: 0.0051
        },
        {
            type: 'HadoopPagerank',
            date: '4/22/2024',
            time: '21:34:08',
            inputSize: 41234,
            duration: 34.56,
            throughput: 1193,
            throughputNode: 398,
            predicted: 0.0086,
            actual: 0.0064
        },
        {
            type: 'SparkPagerank',
            date: '4/22/2024',
            time: '21:36:29',
            inputSize: 44521,
            duration: 29.78,
            throughput: 1495,
            throughputNode: 498,
            predicted: 0.0070,
            actual: 0.0053
        },
        // 添加更多数据用于测试过滤
        {
            type: 'HadoopWordcount',
            date: '4/23/2024',
            time: '09:15:30',
            inputSize: 52148,
            duration: 42.15,
            throughput: 1237,
            throughputNode: 412,
            predicted: 0.0089,
            actual: 0.0067
        },
        {
            type: 'SparkWordcount',
            date: '4/23/2024',
            time: '10:22:45',
            inputSize: 61234,
            duration: 35.67,
            throughput: 1715,
            throughputNode: 572,
            predicted: 0.0058,
            actual: 0.0043
        },
        {
            type: 'HadoopSort',
            date: '4/23/2024',
            time: '11:30:20',
            inputSize: 48976,
            duration: 38.92,
            throughput: 1258,
            throughputNode: 419,
            predicted: 0.0079,
            actual: 0.0059
        }
    ];

    // 渲染表格到正确的表格ID
    function renderTable(data) {
        const tbody = $('#energyTable tbody'); // 修改为正确的表格ID
        tbody.empty();

        if (data.length === 0) {
            tbody.append(
                $('<tr>').append(
                    $('<td>').attr('colspan', 9)
                        .addClass('text-center text-muted')
                        .text('没有找到匹配的数据')
                )
            );
            return;
        }

        data.forEach(item => {
            const row = $('<tr>').append(
                $('<td>').text(item.type),
                $('<td>').text(item.date),
                $('<td>').text(item.time),
                $('<td>').text(item.inputSize.toLocaleString()),
                $('<td>').text(item.duration),
                $('<td>').text(item.throughput.toLocaleString()),
                $('<td>').text(item.throughputNode.toLocaleString()),
                $('<td>').text(item.predicted),
                $('<td>').text(item.actual)
            );
            tbody.append(row);
        });
    }

    // 本地数据查询函数（完全前端过滤）
    function queryLocalData(params = {}) {
        return new Promise((resolve) => {
            // 模拟网络延迟
            setTimeout(() => {
                let filteredData = mockData;

                // 类型过滤
                if (params.type && params.type !== '') {
                    filteredData = filteredData.filter(item => item.type === params.type);
                }

                // 时间过滤
                if (params.time && params.time !== '') {
                    filteredData = filteredData.filter(item => item.time === params.time);
                }

                resolve(filteredData);
            }, 300); // 300ms延迟模拟网络请求
        });
    }

    // 查询按钮事件
    $('#queryBtn').click(async function() {
        const type = $('#typeSelect').val();
        const time = $('#timeSelect').val();

        // 显示加载状态
        const $btn = $(this);
        const originalText = $btn.text();
        $btn.prop('disabled', true).text('查询中...');

        try {
            const data = await queryLocalData({ type, time });
            renderTable(data);

            // 更新统计信息
            updateStatistics(data);
        } finally {
            $btn.prop('disabled', false).text(originalText);
        }
    });

    // 重置按钮事件
    $('#resetBtn').click(function() {
        $('#typeSelect').val('');
        $('#timeSelect').val('');
        // 重置后显示所有数据
        $('#queryBtn').trigger('click');
    });

    // 分页事件（简化版）
    $('.pagination button').click(function(e) {
        e.preventDefault();
        $(this).parent().find('button').removeClass('active');
        $(this).addClass('active');

        // 这里可以添加分页逻辑，但根据需求先简单实现
        $('#queryBtn').trigger('click');
    });

    // 更新统计信息
    function updateStatistics(data) {
        const stats = {
            total: data.length,
            hadoopCount: data.filter(item => item.type.includes('Hadoop')).length,
            sparkCount: data.filter(item => item.type.includes('Spark')).length,
            avgThroughput: data.length > 0 ?
                Math.round(data.reduce((sum, item) => sum + item.throughput, 0) / data.length) : 0,
            avgDuration: data.length > 0 ?
                (data.reduce((sum, item) => sum + item.duration, 0) / data.length).toFixed(2) : 0
        };

        console.log('查询统计:', stats);
        // 可以在这里更新页面上的统计信息显示
    }

    // 页面加载时自动显示所有数据
    function initializePage() {
        // 默认显示所有数据
        renderTable(mockData);
        updateStatistics(mockData);

        console.log('页面初始化完成，显示', mockData.length, '条历史数据');
    }

    // 初始化页面
    initializePage();
});