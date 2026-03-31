$(document).ready(function() {
    var monitorWebBaseURL = "/tasksubmit/tasksubmit/";
    
    // 初始化开关状态
    let energyPredictionEnabled = false;
    let parameterRecommendationEnabled = false;
    
    // 模拟数据 - 扩展所有字段的默认值
    const mockData = {
        energyPrediction: {
            defaultConfig: 0.0071,
            recommendedConfig: 0.0056,
            parameters: {
                "mapreduce.output.fileoutputformat.compress": "true"
            }
        },
        parameterRecommendations: {
            "mapreduce.map.memory.mb": "2048",
            "mapreduce.reduce.memory.mb": "4096",
//            "mapreduce.map.java.opts": "-Xmx1638m",
//            "mapreduce.reduce.java.opts": "-Xmx3276m",
            "mapreduce.task.io.sort.mb": "256",
            "mapreduce.map.sort.spill.percent": "0.8",
            "mapreduce.task.timeout": "600000",
            "mapreduce.reduce.shuffle.parallelcopies": "30",
            "mapreduce.job.queuename": "default"
        },
        // 新增：所有表单字段的默认值
        formDefaults: {
            taskName: "hadoop-mapreduce-examples-3.3.6.jar",
            host: "cluster-node-01",
            taskDescription: "MapReduce word count job processing log files",
            triggerName: "cpu_usage",
            triggerExpression: "gt",
            triggerSeverity: "4",
            triggerStatus: "1",
            executionFrequency: "daily",
            startTime: "2025-10-31T1+19:00",
            mapreduceMapMemory: "2048",
            mapreduceReduceMemory: "4096"
        },
        // 下拉选项数据
        selectOptions: {
            triggerName: [
                "Please Select",
                "cpu_usage",
                "memory_usage",
                "disk_usage",
                "network_traffic",
                "system_load"
            ],
            triggerExpression: [
                "Please Select",
                "gt",
                "lt",
                "eq",
                "ge",
                "le"
            ],
            triggerSeverity: [
                "Please Select",
                "1",
                "2",
                "3",
                "4",
                "5"
            ],
            triggerStatus: [
                "Please Select",
                "1",
                "0"
            ]
        }
    };

    // 初始化页面时填充下拉选项
    function initializeSelectOptions() {
        // 填充Trigger Name选项
        const triggerNameSelect = $('.form-item:has(label:contains("Trigger Name")) select');
        triggerNameSelect.empty();
        mockData.selectOptions.triggerName.forEach(option => {
            triggerNameSelect.append($('<option>').text(option).val(option));
        });

        // 填充Trigger Expression选项
        const triggerExpressionSelect = $('.form-item:has(label:contains("Trigger Expression")) select');
        triggerExpressionSelect.empty();
        mockData.selectOptions.triggerExpression.forEach(option => {
            triggerExpressionSelect.append($('<option>').text(option).val(option));
        });

        // 填充Trigger Severity选项
        const triggerSeveritySelect = $('.form-item:has(label:contains("Trigger Severity")) select');
        triggerSeveritySelect.empty();
        mockData.selectOptions.triggerSeverity.forEach(option => {
            triggerSeveritySelect.append($('<option>').text(option).val(option));
        });

        // 填充Trigger Status选项
        const triggerStatusSelect = $('.form-item:has(label:contains("Trigger Status")) select');
        triggerStatusSelect.empty();
        mockData.selectOptions.triggerStatus.forEach(option => {
            triggerStatusSelect.append($('<option>').text(option).val(option));
        });
    }

    // 页面加载时初始化
    initializeSelectOptions();

    // 1. 文件上传功能
    $('#uploadJarBtn').click(function() {
        $('#jarFileInput').click();
    });

    $('#jarFileInput').change(function(e) {
        const file = e.target.files[0];
        if (file) {
            simulateFileUpload(file)
                .then(result => {
                    const taskNameInput = $('.task-name-container input[type="text"]');
                    taskNameInput.val(result.taskName);
                    alert('File uploaded successfully: ' + file.name);
                    console.log('JAR包上传成功，Task Name设置为:', result.taskName);
                })
                .catch(error => {
                    alert('File upload failed: ' + error);
                });
        }
    });

    function simulateFileUpload(file) {
        return new Promise((resolve) => {
            setTimeout(() => {
                let taskName = '';

                if (file.name.includes('hadoop-mapreduce-examples')) {
                    const versionMatch = file.name.match(/hadoop-mapreduce-examples-(\d+\.\d+\.\d+)\.jar/);
                    if (versionMatch) {
                        taskName = `hadoop-mapreduce-examples-${versionMatch[1]}.jar`;
                    } else {
                        taskName = 'hadoop-mapreduce-examples-3.3.6.jar';
                    }
                } else {
                    taskName = file.name;
                }

                resolve({
                    success: true,
                    fileName: file.name,
                    taskName: taskName,
                    fileSize: file.size,
                    uploadTime: new Date().toISOString()
                });
            }, 500);
        });
    }

    // 2. Task Energy Efficiency Prediction 功能
    $('#energyPredictionToggle').change(function() {
        energyPredictionEnabled = $(this).is(':checked');
        if (energyPredictionEnabled) {
            showEnergyPredictionResults();
        } else {
            $('#energyPredictionResults').remove();
        }
    });

    // 3. Parameter Recommendation 功能
    $('#parameterRecommendationToggle').change(function() {
        parameterRecommendationEnabled = $(this).is(':checked');
        if (parameterRecommendationEnabled) {
            showParameterRecommendations();
        } else {
            $('#parameterRecommendationResults').remove();
        }
    });

    // 显示能源预测结果
    function showEnergyPredictionResults() {
        $('#energyPredictionResults').remove();

        const resultsHTML = `
            <div id="energyPredictionResults" style="margin: 20px 0; background: #f8f9fa; padding: 15px; border-radius: 5px;">
                <h4>Energy Efficiency Prediction Results</h4>
                <table class="prediction-table" style="width: 100%; border-collapse: collapse; margin-top: 10px;">
                    <thead>
                        <tr style="background: #e9ecef;">
                            <th style="padding: 10px; border: 1px solid #dee2e6; text-align: left;"></th>
                            <th style="padding: 10px; border: 1px solid #dee2e6; text-align: left;">Default Configuration</th>
                            <th style="padding: 10px; border: 1px solid #dee2e6; text-align: left;">Recommended Configuration</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td style="padding: 10px; border: 1px solid #dee2e6;">Predicted Energy Consumption (KWh)</td>
                            <td style="padding: 10px; border: 1px solid #dee2e6;">${mockData.energyPrediction.defaultConfig}</td>
                            <td style="padding: 10px; border: 1px solid #dee2e6;">${mockData.energyPrediction.recommendedConfig}</td>
                        </tr>
                        <tr>
                            <td style="padding: 10px; border: 1px solid #dee2e6;">mapreduce.output.fileoutputformat.compress</td>
                            <td style="padding: 10px; border: 1px solid #dee2e6;">False</td>
                            <td style="padding: 10px; border: 1px solid #dee2e6;">True</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        `;

        $('.toggle-container').after(resultsHTML);
    }

    // 显示参数推荐
    function showParameterRecommendations() {
        $('#parameterRecommendationResults').remove();

        const basicParams = {
            "mapreduce.map.memory.mb": mockData.parameterRecommendations["mapreduce.map.memory.mb"],
            "mapreduce.reduce.memory.mb": mockData.parameterRecommendations["mapreduce.reduce.memory.mb"]
        };

        const allParams = mockData.parameterRecommendations;

        let basicParamsHTML = '';
        Object.keys(basicParams).forEach(key => {
            basicParamsHTML += `
                <div class="form-item">
                    <label>${key}</label>
                    <input type="text" value="${basicParams[key]}" placeholder="Recommended value" readonly style="background: #f8f9fa;">
                </div>
            `;
        });

        let allParamsHTML = '';
        Object.keys(allParams).forEach(key => {
            allParamsHTML += `
                <div class="form-item">
                    <label>${key}</label>
                    <input type="text" value="${allParams[key]}" placeholder="Recommended value" readonly style="background: #f8f9fa;">
                </div>
            `;
        });

        const resultsHTML = `
            <div id="parameterRecommendationResults" style="margin: 20px 0;">
                <h4>Parameter Recommendations</h4>
                <div class="form-grid" id="basicParameters">
                    ${basicParamsHTML}
                </div>
                <div id="moreParameters" style="display: none;">
                    <div class="form-grid">
                        ${allParamsHTML}
                    </div>
                </div>
                <button type="button" id="showMoreBtn" class="upload-btn" style="width: auto; margin-top: 10px;">
                    SHOW MORE
                </button>
            </div>
        `;

        if ($('#energyPredictionResults').length) {
            $('#energyPredictionResults').after(resultsHTML);
        } else {
            $('.toggle-container').after(resultsHTML);
        }

        $('#showMoreBtn').click(function() {
            const moreSection = $('#moreParameters');
            const btn = $(this);

            if (moreSection.is(':visible')) {
                moreSection.hide();
                btn.text('SHOW MORE');
                $('html, body').animate({
                    scrollTop: $('#basicParameters').offset().top - 20
                }, 300);
            } else {
                moreSection.show();
                btn.text('SHOW LESS');
                $('html, body').animate({
                    scrollTop: moreSection.offset().top - 20
                }, 300);
            }
        });
    }

    // 4. Submit 功能 - 自动填充所有字段
    $('#submitBtn').click(function(e) {
        e.preventDefault();

        // 自动填充所有表单字段
        autoFillFormFields();

        // 验证是否已上传JAR包
        const taskName = $('.task-name-container input[type="text"]').val();
        if (!taskName || !taskName.includes('.jar')) {
            alert('Please upload a JAR file first!');
            return;
        }

        // 验证是否启用了功能
        if (!energyPredictionEnabled && !parameterRecommendationEnabled) {
            alert('Please enable at least one feature (Energy Prediction or Parameter Recommendation)!');
            return;
        }

        // 收集表单数据
        const formData = collectFormData();

        console.log('提交的表单数据:', formData);

        // 模拟提交到服务器
        simulateSubmit(formData)
            .then(result => {
                alert('Submit Success!');
                console.log('提交成功:', result);
                showSubmissionSummary(formData, result);
            })
            .catch(error => {
                alert('Submit Failed: ' + error);
                console.error('提交失败:', error);
            });
    });

    // 自动填充所有表单字段的函数
    function autoFillFormFields() {
        console.log('开始自动填充表单字段...');

        // 1. 填充Task Name（如果为空）
        const taskNameInput = $('.task-name-container input[type="text"]');
        if (!taskNameInput.val()) {
            taskNameInput.val(mockData.formDefaults.taskName);
        }

        // 2. 填充所有输入框
        $('.form-item input[type="text"]').each(function() {
            const label = $(this).closest('.form-item').find('label').text().trim();
            if (!$(this).val()) {
                switch(true) {
                    case label.includes('Host'):
                        $(this).val(mockData.formDefaults.host);
                        break;
                    case label.includes('Task Description'):
                        $(this).val(mockData.formDefaults.taskDescription);
                        break;
                    case label.includes('Execution Frequency'):
                        $(this).val(mockData.formDefaults.executionFrequency);
                        break;
                    case label.includes('mapreduce.map.memory.mb'):
                        $(this).val(mockData.formDefaults.mapreduceMapMemory);
                        break;
                    case label.includes('mapreduce.reduce.memory.mb'):
                        $(this).val(mockData.formDefaults.mapreduceReduceMemory);
                        break;
                }
            }
        });

        // 3. 填充所有下拉框
        $('.form-item select').each(function() {
            const label = $(this).closest('.form-item').find('label').text().trim();
            if ($(this).val() === 'Please Select' || !$(this).val()) {
                switch(true) {
                    case label.includes('Trigger Name'):
                        $(this).val(mockData.formDefaults.triggerName);
                        break;
                    case label.includes('Trigger Expression'):
                        $(this).val(mockData.formDefaults.triggerExpression);
                        break;
                    case label.includes('Trigger Severity'):
                        $(this).val(mockData.formDefaults.triggerSeverity);
                        break;
                    case label.includes('Trigger Status'):
                        $(this).val(mockData.formDefaults.triggerStatus);
                        break;
                }
            }
        });

        // 4. 填充日期时间选择器
        const startTimeInput = $('.form-item:has(label:contains("StartTime")) input[type="datetime-local"]');
        if (!startTimeInput.val()) {
            startTimeInput.val(mockData.formDefaults.startTime);
        }

        console.log('表单字段自动填充完成');
    }

    // 收集表单数据的函数
    function collectFormData() {
        return {
            taskName: $('.task-name-container input[type="text"]').val(),
            jarFileName: $('.task-name-container input[type="text"]').val(),
            energyPrediction: energyPredictionEnabled,
            parameterRecommendation: parameterRecommendationEnabled,
            host: $('.form-item:has(label:contains("Host")) input').val(),
            taskDescription: $('.form-item:has(label:contains("Task Description")) input').val(),
            triggerName: $('.form-item:has(label:contains("Trigger Name")) select').val(),
            triggerExpression: $('.form-item:has(label:contains("Trigger Expression")) select').val(),
            triggerSeverity: $('.form-item:has(label:contains("Trigger Severity")) select').val(),
            triggerStatus: $('.form-item:has(label:contains("Trigger Status")) select').val(),
            executionFrequency: $('.form-item:has(label:contains("Execution Frequency")) input').val(),
            startTime: $('.form-item:has(label:contains("StartTime")) input').val(),
            mapreduceMapMemory: $('.form-item:has(label:contains("mapreduce.map.memory.mb")) input').val(),
            mapreduceReduceMemory: $('.form-item:has(label:contains("mapreduce.reduce.memory.mb")) input').val(),
            submissionTime: new Date().toISOString()
        };
    }

    // 模拟提交函数
    function simulateSubmit(formData) {
        return new Promise((resolve, reject) => {
            setTimeout(() => {
                if (!formData.taskName) {
                    reject('Task Name is required');
                    return;
                }

                if (!formData.host) {
                    reject('Host is required');
                    return;
                }

                resolve({
                    success: true,
                    message: 'Task submitted successfully',
                    taskId: 'TASK_' + Date.now(),
                    estimatedCompletion: new Date(Date.now() + 30 * 60 * 1000).toISOString(),
                    featuresEnabled: {
                        energyPrediction: formData.energyPrediction,
                        parameterRecommendation: formData.parameterRecommendation
                    }
                });
            }, 1000);
        });
    }

    // 显示提交摘要
    function showSubmissionSummary(formData, result) {
        const summaryHTML = `
            <div id="submissionSummary" style="margin: 20px 0; background: #d4edda; padding: 15px; border-radius: 5px; border: 1px solid #c3e6cb;">
                <h4 style="color: #155724; margin-top: 0;">Submission Summary</h4>
                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
                    <div><strong>Task ID:</strong> ${result.taskId}</div>
                    <div><strong>JAR File:</strong> ${formData.jarFileName}</div>
                    <div><strong>Host:</strong> ${formData.host}</div>
                    <div><strong>Trigger Name:</strong> ${formData.triggerName}</div>
                    <div><strong>Energy Prediction:</strong> ${formData.energyPrediction ? 'Enabled' : 'Disabled'}</div>
                    <div><strong>Parameter Recommendation:</strong> ${formData.parameterRecommendation ? 'Enabled' : 'Disabled'}</div>
                    <div><strong>Start Time:</strong> ${formData.startTime}</div>
                    <div><strong>Estimated Completion:</strong> ${new Date(result.estimatedCompletion).toLocaleString()}</div>
                </div>
            </div>
        `;

        $('.button-group').before(summaryHTML);

        setTimeout(() => {
            $('#submissionSummary').fadeOut(500, function() {
                $(this).remove();
            });
        }, 5000);
    }

    // Reset 功能
    $('#resetBtn').click(function() {
        // 重置表单
        $('input[type="text"]').val('');
        $('select').val('Please Select');
        $('input[type="datetime-local"]').val('2025-10-31T19:00');

        // 重置文件上传
        $('#jarFileInput').val('');

        // 重置开关
        $('#energyPredictionToggle, #parameterRecommendationToggle').prop('checked', false);
        energyPredictionEnabled = false;
        parameterRecommendationEnabled = false;

        // 移除结果显示
        $('#energyPredictionResults, #parameterRecommendationResults, #submissionSummary').remove();
    });

    // Trigger Name 变化处理
    $('.form-item:has(label:contains("Trigger Name")) select').change(function() {
        const selectedTrigger = $(this).val();
        if (selectedTrigger && selectedTrigger !== 'Please Select') {
            const defaultValues = {
                'cpu_usage': { expression: 'gt', severity: '4', status: '1' },
                'memory_usage': { expression: 'gt', severity: '4', status: '1' },
                'disk_usage': { expression: 'gt', severity: '3', status: '1' }
            };

            if (defaultValues[selectedTrigger]) {
                $('.form-item:has(label:contains("Trigger Expression")) select').val(defaultValues[selectedTrigger].expression);
                $('.form-item:has(label:contains("Trigger Severity")) select').val(defaultValues[selectedTrigger].severity);
                $('.form-item:has(label:contains("Trigger Status")) select').val(defaultValues[selectedTrigger].status);
            }
        }
    });
});