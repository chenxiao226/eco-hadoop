(function () {

    var BASE = '/monitor/rl/';
    var chartLoss        = null;
    var chartEnergy      = null;
    var chartEpochEnergy = null;

    var lossData = [];
    var pollTimer  = null;
    var pollCount  = 0;
    var currentTask    = null;
    var currentNode    = null;
    var currentLambda  = null;
    var currentEpsilon = null;
    var currentTheta   = null;

    // ── placeholder graphic overlay ──────────────────────────────────────────
    var PLACEHOLDER_GRAPHIC = [{
        type: 'text', left: 'center', top: 'middle',
        style: { text: 'Available after training', fill: '#bbb', fontSize: 12 }
    }];

    // ── init charts ──────────────────────────────────────────────────────────
    function initCharts() {

        // Training Loss Curve (green)
        chartLoss = echarts.init(document.getElementById('lossChart'));
        chartLoss.setOption({
            title: { text: 'Loss', left: 'center', top: 2, textStyle: { fontSize: 11, color: '#666' } },
            tooltip: { trigger: 'axis' },
            toolbox: { feature: { saveAsImage: {} }, right: 4, top: 2, iconStyle: { borderColor: '#67C23A' } },
            dataZoom: [{ type: 'inside' }],
            grid: { left: '6%', right: '5%', top: 28, bottom: 36, containLabel: true },
            xAxis: { type: 'value', name: 'Iter', nameTextStyle: { fontSize: 10 }, nameLocation: 'end', boundaryGap: false },
            yAxis: { type: 'value', name: 'Loss', nameTextStyle: { fontSize: 10 } },
            series: [{ name: 'Loss', type: 'line', smooth: true, symbol: 'none',
                areaStyle: { color: 'rgba(103,194,58,0.1)' },
                lineStyle: { color: '#67C23A', width: 1.5 },
                itemStyle: { color: '#67C23A' }, data: [] }]
        });

        // Energy Before vs After (teal) — empty with axes + placeholder
        chartEnergy = echarts.init(document.getElementById('energyChart'));
        chartEnergy.setOption({
            graphic: PLACEHOLDER_GRAPHIC,
            tooltip: { trigger: 'axis' },
            legend: { bottom: 4, itemHeight: 10, textStyle: { fontSize: 10 }, data: ['Before', 'After'] },
            grid: { left: '6%', right: '4%', top: 20, bottom: 46, containLabel: true },
            xAxis: { type: 'category', data: [], name: 'λ', nameTextStyle: { fontSize: 10 }, axisLabel: { fontSize: 10 } },
            yAxis: { type: 'value', name: 'Energy (J)', nameLocation: 'middle', nameRotate: 90, nameGap: 46, nameTextStyle: { fontSize: 10 }, scale: true },
            series: [
                { name: 'Before', type: 'bar', data: [], itemStyle: { color: '#E6A23C' } },
                { name: 'After',  type: 'bar', data: [], itemStyle: { color: '#23C6C8' } }
            ]
        });

        // Energy per Epoch (red) — empty with axes + placeholder
        chartEpochEnergy = echarts.init(document.getElementById('epochEnergyChart'));
        chartEpochEnergy.setOption({
            graphic: PLACEHOLDER_GRAPHIC,
            tooltip: { trigger: 'axis' },
            legend: { bottom: 4, itemHeight: 10, textStyle: { fontSize: 10 } },
            dataZoom: [{ type: 'inside' }],
            grid: { left: '6%', right: '4%', top: 20, bottom: 46, containLabel: true },
            xAxis: { type: 'value', name: 'Epoch', nameLocation: 'middle', nameGap: 20, nameTextStyle: { fontSize: 10 } },
            yAxis: { type: 'value', name: 'Energy (J)', nameLocation: 'middle', nameRotate: 90, nameGap: 46, nameTextStyle: { fontSize: 10 }, scale: true },
            series: []
        });

        window.addEventListener('resize', function () {
            chartLoss        && chartLoss.resize();
            chartEnergy      && chartEnergy.resize();
            chartEpochEnergy && chartEpochEnergy.resize();
        });
    }

    // ── start training ───────────────────────────────────────────────────────
    function startTraining() {
        currentTask    = $('#taskSelect').val();
        currentNode    = $('#nodeSelect').val();
        currentLambda  = $('#lambdaSelect').val();
        currentEpsilon = $('#epsilonInput').val() || '0.01';
        currentTheta   = $('#thetaInput').val()   || '5';

        if (!currentTask) { alert('Please select a task.'); return; }

        lossData = [];
        chartLoss.setOption({ series: [{ data: [] }] });
        resetEnergyCharts();
        $('#summaryBox').hide();
        $('#debugBox').hide().text('');

        $.ajax({
            url: BASE + 'train', type: 'POST', dataType: 'json',
            data: { taskName: currentTask, nodeName: currentNode, lambda: currentLambda,
                    epsilon: currentEpsilon, theta: currentTheta },
            success: function (data) {
                if (!data.success) { showStatus('error', data.error || 'Failed to start training'); return; }
                showStatus('info', 'Training started...');
                $('#btnStart').hide();
                $('#btnStop').show();
                $('#btnDebug').show();
                $('#trainingBadge').show();
                startPolling();
            },
            error: function () { showStatus('error', 'Request failed'); }
        });
    }

    // ── stop training ────────────────────────────────────────────────────────
    function stopTraining() {
        $.ajax({
            url: BASE + 'train/stop', type: 'POST', dataType: 'json',
            data: { taskName: currentTask, nodeName: currentNode, lambda: currentLambda },
            success: function () {
                stopPolling();
                showStatus('warning', 'Training stopped.');
                resetButtons();
            }
        });
    }

    // ── polling ──────────────────────────────────────────────────────────────
    function startPolling() {
        pollCount = 0;
        pollTimer = setInterval(function () {
            pollCount++;
            fetchCurves();
            if (pollCount % 3 === 0) checkStatus();
        }, 3000);
    }

    function stopPolling() {
        if (pollTimer) { clearInterval(pollTimer); pollTimer = null; }
    }

    function checkStatus() {
        $.ajax({
            url: BASE + 'train/status', type: 'GET', dataType: 'json',
            data: { taskName: currentTask, nodeName: currentNode, lambda: currentLambda },
            success: function (data) {
                if (!data.running) {
                    var st = data.status || 'idle';
                    if (st === 'done') {
                        stopPolling();
                        showStatus('success', 'Training completed.');
                        fetchCurves();
                        fetchAnalysis();
                        resetButtons();
                    } else if (st === 'stopped') {
                        stopPolling();
                        resetButtons();
                    } else if (st.indexOf('error') === 0) {
                        stopPolling();
                        fetchDebugAndShow();
                        showStatus('error', 'Training error (exit code ' + st.split(':')[1] + '). See debug info below.');
                        resetButtons();
                    }
                }
            },
            error: function () {}
        });
    }

    function fetchCurves() {
        $.ajax({
            url: BASE + 'training-curves', type: 'GET', dataType: 'json',
            data: { taskName: currentTask, nodeName: currentNode },
            success: function (data) {
                if (!data.success) {
                    showStatus('info', 'Waiting for log data... (poll #' + pollCount + ')');
                    return;
                }
                if (!data.curve || data.curve.length === 0) {
                    showStatus('info', 'Log found, waiting for first iteration... (poll #' + pollCount + ')');
                    return;
                }
                var existing = lossData.length > 0 ? lossData[lossData.length - 1][0] : -1;
                $.each(data.curve, function (_, pt) {
                    if (pt.iter > existing) {
                        lossData.push([pt.iter, pt.loss]);
                        existing = pt.iter;
                    }
                });
                chartLoss.setOption({ series: [{ data: lossData }] });
                showStatus('info', 'Training — ' + lossData.length + ' iters logged (poll #' + pollCount + ')');
            },
            error: function (xhr) { showStatus('warning', 'Poll #' + pollCount + ' failed: ' + xhr.status); }
        });
    }

    function fetchAnalysis() {
        // Summary box
        $.ajax({
            url: BASE + 'training-curves', type: 'GET', dataType: 'json',
            data: { taskName: currentTask, nodeName: currentNode },
            success: function (data) {
                if (!data.success) return;
                var eo = data.energyOptimization;
                if (eo && (eo.initialEnergy != null || eo.finalEnergy != null)) {
                    $('#summaryBox').show();
                    $('#sumInitEnergy').text(eo.initialEnergy != null ? eo.initialEnergy.toFixed(2) : '--');
                    $('#sumFinalEnergy').text(eo.finalEnergy   != null ? eo.finalEnergy.toFixed(2)   : '--');
                    // cap display at 30%
                    var imp = eo.improvementPercent;
                    if (imp != null) {
                        var impNum = parseFloat(imp);
                        var impStr = impNum > 30
                            ? impNum.toFixed(2) + '% (≈30% max expected)'
                            : impNum.toFixed(2) + '%';
                        $('#sumImprove').text(impStr);
                    } else {
                        $('#sumImprove').text('--');
                    }
                    $('#sumConverged').text(data.converged ? 'Yes' : 'No');
                    $('#sumIters').text(data.totalIterations || '--');
                }
            }
        });
        // Energy charts
        $.ajax({
            url: BASE + 'parameter-analysis', type: 'GET', dataType: 'json',
            data: { taskName: currentTask, lambda: currentLambda },
            success: function (data) {
                if (!data.success) return;
                renderEnergyChart(data.charts.energyComparison);
                renderEpochEnergyChart(data.charts.trainingCurves);
            }
        });
    }

    // ── energy before vs after bar chart ─────────────────────────────────────
    function renderEnergyChart(ec) {
        // Compute 30% reference line value from first "before" entry
        var markLines = [];
        if (ec.before && ec.before.length > 0 && ec.before[0] != null) {
            var refVal = parseFloat(ec.before[0]) * 0.70; // 30% reduction target
            markLines = [{
                name: '−30%', yAxis: refVal,
                lineStyle: { color: '#F56C6C', type: 'dashed', width: 1 },
                label: { formatter: '−30%', position: 'end', fontSize: 10, color: '#F56C6C' }
            }];
        }
        chartEnergy.setOption({
            graphic: [],   // remove placeholder
            tooltip: { trigger: 'axis' },
            legend: { bottom: 4, itemHeight: 10, textStyle: { fontSize: 10 }, data: ['Before', 'After'] },
            grid: { left: '6%', right: '4%', top: 20, bottom: 46, containLabel: true },
            xAxis: { type: 'category', data: ec.categories, name: 'λ', nameTextStyle: { fontSize: 10 }, axisLabel: { fontSize: 10 } },
            yAxis: { type: 'value', name: 'Energy (J)', nameLocation: 'middle', nameRotate: 90, nameGap: 46, nameTextStyle: { fontSize: 10 }, scale: true },
            series: [
                { name: 'Before', type: 'bar', data: ec.before, itemStyle: { color: '#E6A23C' },
                  markLine: { silent: true, symbol: 'none', data: markLines } },
                { name: 'After',  type: 'bar', data: ec.after,  itemStyle: { color: '#23C6C8' } }
            ]
        });
    }

    // ── energy per epoch line chart ───────────────────────────────────────────
    function renderEpochEnergyChart(curves) {
        var series = [];
        var colors = ['#F56C6C', '#E6A23C', '#67C23A'];
        $.each(curves, function (i, c) {
            var pts = [];
            for (var j = 0; j < c.epochs.length; j++) pts.push([c.epochs[j], c.energy[j]]);
            series.push({
                name: c.name, type: 'line', smooth: true, symbol: 'none',
                lineStyle: { color: colors[i % colors.length], width: 1.5 },
                itemStyle: { color: colors[i % colors.length] },
                data: pts
            });
        });
        chartEpochEnergy.setOption({
            graphic: [],   // remove placeholder
            tooltip: { trigger: 'axis' },
            legend: { bottom: 4, itemHeight: 10, textStyle: { fontSize: 10 } },
            dataZoom: [{ type: 'inside' }],
            grid: { left: '6%', right: '4%', top: 20, bottom: 46, containLabel: true },
            xAxis: { type: 'value', name: 'Epoch', nameLocation: 'middle', nameGap: 20, nameTextStyle: { fontSize: 10 } },
            yAxis: { type: 'value', name: 'Energy (Ws)', nameLocation: 'middle', nameRotate: 90, nameGap: 46, nameTextStyle: { fontSize: 10 }, scale: true },
            series: series
        });
    }

    function resetEnergyCharts() {
        chartEnergy.setOption({
            graphic: PLACEHOLDER_GRAPHIC,
            xAxis: { data: [] },
            series: [{ name: 'Before', data: [] }, { name: 'After', data: [] }]
        }, { replaceMerge: ['series'] });

        // clear() removes all series reliably, then restore base config
        chartEpochEnergy.clear();
        chartEpochEnergy.setOption({
            graphic: PLACEHOLDER_GRAPHIC,
            tooltip: { trigger: 'axis' },
            legend: { bottom: 4, itemHeight: 10, textStyle: { fontSize: 10 } },
            dataZoom: [{ type: 'inside' }],
            grid: { left: '6%', right: '4%', top: 20, bottom: 46, containLabel: true },
            xAxis: { type: 'value', name: 'Epoch', nameLocation: 'middle', nameGap: 20, nameTextStyle: { fontSize: 10 } },
            yAxis: { type: 'value', name: 'Energy (Ws)', nameLocation: 'middle', nameRotate: 90, nameGap: 46, nameTextStyle: { fontSize: 10 }, scale: true },
            series: []
        });
    }

    // ── helpers ──────────────────────────────────────────────────────────────
    function showStatus(type, msg) {
        var cls = { info: 'alert-info', success: 'alert-success', warning: 'alert-warning', error: 'alert-danger' };
        $('#statusBox').attr('class', 'alert ' + (cls[type] || 'alert-info')).text(msg).show();
    }

    function resetButtons() {
        $('#btnStart').show();
        $('#btnStop').hide();
        $('#btnDebug').show();
        $('#trainingBadge').hide();
    }

    function fetchDebugAndShow() {
        $.ajax({
            url: BASE + 'train-debug', type: 'GET', dataType: 'json',
            data: { taskName: currentTask, nodeName: currentNode, lambda: currentLambda },
            success: function (d) {
                if (!d.success) { $('#debugBox').text('[Server Error] ' + (d.error || 'unknown')).show(); return; }
                var msg = 'outputDir: ' + (d.outputDir || '?') + '\n';
                msg += 'stdout.log: ' + d.stdoutExists + ' | log.txt: ' + d.logExists + '\n';
                if (d.stdout) msg += '\n--- stdout.log ---\n' + d.stdout;
                if (d.log)    msg += '\n--- log.txt ---\n' + d.log;
                $('#debugBox').text(msg).show();
            }
        });
    }

    // ── debug button ─────────────────────────────────────────────────────────
    function showDebugInfo() {
        if (!currentTask) { alert('No active training task.'); return; }
        fetchDebugAndShow();
    }

    // ── JAR upload / modal flow ───────────────────────────────────────────────
    var jarPollTimer  = null;
    var currentJobKey = null;
    var pendingTaskName = null;   // task name entered by user in modal

    function openHadoopModal() {
        var fileInput = document.getElementById('jarFileInput');
        if (!fileInput.files || fileInput.files.length === 0) {
            alert('Please select a JAR file first.');
            return;
        }
        var fileName = fileInput.files[0].name;
        // Step 1
        $('#modalJarName').text('File: ' + fileName);
        $('#modalStep1').show();
        $('#modalStep2').hide();
        // Pre-fill command in step 2
        $('#modalCmdInput').val('hadoop jar ' + fileName + ' nnbench');
        $('#modalTaskName').val('nnbench');
        // Show modal
        $('#hadoopModal').css('display', 'flex');
    }

    function closeHadoopModal() {
        $('#hadoopModal').hide();
        $('#modalStep1').show();
        $('#modalStep2').hide();
    }

    function startJarPolling(jobKey) {
        currentJobKey = jobKey;
        // Show only "Waiting..." during the whole process
        $('#jarStatusBox').css({ background: '#fffbe6', border: '1px solid #ffe58f', color: '#8a6900' }).show();
        $('#jarStatusText').html('<i class="fa fa-spinner fa-spin"></i> Waiting...');

        jarPollTimer = setInterval(function () {
            $.ajax({
                url: BASE + 'submit-status', type: 'GET', dataType: 'json',
                data: { jobKey: jobKey },
                success: function (data) {
                    if (!data.success) return;
                    if (data.done) {
                        clearInterval(jarPollTimer);
                        jarPollTimer = null;
                        // Hide waiting status
                        $('#jarStatusBox').hide();
                        // Add user-specified task name to dropdown
                        var taskName = pendingTaskName || 'nnbench';
                        var taskVal  = taskName.toLowerCase();
                        if ($('#taskSelect option[value="' + taskVal + '"]').length === 0) {
                            $('#taskSelect').append(
                                '<option value="' + taskVal + '">' + taskName + ' (Custom)</option>'
                            );
                        }
                        // Show cluster notification bar
                        $('#clusterNotice').show();
                        // Re-enable submit button
                        $('#btnUploadJar').prop('disabled', false)
                            .html('<i class="fa fa-cloud-upload"></i> Submit');
                    }
                },
                error: function () { /* silent — keep polling */ }
            });
        }, 2000);
    }

    function doUploadJar() {
        var fileInput = document.getElementById('jarFileInput');
        var formData  = new FormData();
        formData.append('jarFile', fileInput.files[0]);

        $('#btnUploadJar').prop('disabled', true)
            .html('<i class="fa fa-spinner fa-spin"></i> Submitting...');

        $.ajax({
            url: BASE + 'submit-jar', type: 'POST',
            data: formData, processData: false, contentType: false, dataType: 'json',
            success: function (data) {
                if (!data.success) {
                    $('#jarStatusBox').css({ background: '#fde', border: '1px solid #f5c6cb', color: '#721c24' }).show();
                    $('#jarStatusText').html('<i class="fa fa-times-circle"></i> Upload failed: ' + data.error);
                    $('#btnUploadJar').prop('disabled', false)
                        .html('<i class="fa fa-cloud-upload"></i> Submit');
                    return;
                }
                startJarPolling(data.jobKey);
            },
            error: function () {
                $('#jarStatusBox').css({ background: '#fde', border: '1px solid #f5c6cb', color: '#721c24' }).show();
                $('#jarStatusText').html('<i class="fa fa-times-circle"></i> Network error — could not reach server.');
                $('#btnUploadJar').prop('disabled', false)
                    .html('<i class="fa fa-cloud-upload"></i> Submit');
            }
        });
    }

    // ── boot ─────────────────────────────────────────────────────────────────
    $(function () {
        initCharts();

        $('#btnStart').on('click', startTraining);
        $('#btnStop').on('click', stopTraining);
        $('#btnDebug').on('click', showDebugInfo);

        $('#btnChooseFile').on('click', function () {
            document.getElementById('jarFileInput').click();
        });
        $('#jarFileInput').on('change', function () {
            var name = this.files && this.files.length > 0 ? this.files[0].name : 'No file selected';
            $('#jarFileName').text(name).css('color', this.files && this.files.length > 0 ? '#333' : '#888');
        });
        $('#btnUploadJar').on('click', openHadoopModal);

        // Modal buttons
        $('#modalCancelBtn').on('click', closeHadoopModal);
        $('#modalYesBtn').on('click', function () {
            $('#modalStep1').hide();
            $('#modalStep2').show();
            $('#modalTaskName').focus();
        });
        $('#modalBackBtn').on('click', function () {
            $('#modalStep2').hide();
            $('#modalStep1').show();
        });
        $('#modalSubmitBtn').on('click', function () {
            var taskName = $.trim($('#modalTaskName').val());
            if (!taskName) { $('#modalTaskName').focus(); return; }
            pendingTaskName = taskName;
            closeHadoopModal();
            doUploadJar();
        });
        // Close modal on backdrop click
        $('#hadoopModal').on('click', function (e) {
            if (e.target === this) closeHadoopModal();
        });
        // Close cluster notice
        $('#clusterNoticeClose').on('click', function () {
            $('#clusterNotice').hide();
        });

        $('#taskSelect').on('change', function () {
            var task = $(this).val();
            if (!task) return;
            lossData = [];
            chartLoss.setOption({ series: [{ data: [] }] });
            resetEnergyCharts();
            $('#summaryBox').hide();
            $('#statusBox').hide();
        });

        // Any parameter change invalidates previous results
        $('#lambdaSelect, #nodeSelect').on('change', function () {
            resetEnergyCharts();
            $('#summaryBox').hide();
        });
        $('#epsilonInput, #thetaInput, #lambdaSelect').on('input', function () {
            resetEnergyCharts();
            $('#summaryBox').hide();
        });
    });

})();
