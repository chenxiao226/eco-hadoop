(function () {
    var monitorWebBaseURL = "/monitor/memory/";

    $(function () {
        $.ajax({
            type: "POST",
            url: monitorWebBaseURL + "basic",
            data: {},
            dataType: "json",
            success: function (data) {
                if (data.success && data.dataObject) {
                    try {
                        // 假设 sum 单位为 MB，转换为 GB
                        var totalMemMB = parseFloat(data.dataObject.totalMem.sum) || 0;
                        var totalSwapMB = parseFloat(data.dataObject.totalSwap.sum) || 0;

                        var totalMemGB = (totalMemMB / 1024).toFixed(1); // MB → GB
                        var totalSwapGB = (totalSwapMB / 1024).toFixed(1);

                        $("#totalMem")
                            .html(totalMemGB + " <small>GB</small>")
                            .data("totalMemValue", parseFloat(totalMemGB));

                        $("#totalSwap")
                            .html(totalSwapGB + " <small>GB</small>");

                    } catch (e) {
                        console.error("数据处理失败:", e);
                        $("#totalMem, #totalSwap").html("N/A");
                    }
                } else {
                    $("#totalMem, #totalSwap").html("N/A");
                }
            },
            error: function () {
                $("#totalMem, #totalSwap").html("N/A");
            }
        });
    });
})();