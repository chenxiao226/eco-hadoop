(function(){

    var addToCompareDisabled = "";
    var currentCpApp = [];

    $(function() {
        initTable();
    });

    $("#queryBtn").click(function() {
        $('#historyAppTable').bootstrapTable('refresh'); // 刷新表格
    });

    function initTable() {
        var url = monitorWebBaseURL+"history/getHistoryListByPage";
        var table = $('#historyAppTable').bootstrapTable({
            method : 'POST',
            dataType : 'json',
            contentType : "application/x-www-form-urlencoded",
            cache : false,
            striped : true, // 是否显示行间隔色
            sidePagination : "server", // 分页方式：client客户端分页，server服务端分页（*）
            url : url,
            //height : $(window).height() - 260,
            //width : $(window).width(),
            showColumns : false,
            pagination : true,
            queryParams : queryParams,
            minimumCountColumns : 2,
            pageNumber : 1, // 初始化加载第一页，默认第一页
            pageSize : 5, // 每页的记录行数（*）
            pageList : [ 5, 10, 15, 20 ], // 可供选择的每页的行数（*）
            uniqueId : "id", // 每一行的唯一标识，一般为主键列
            showExport : true,
            exportDataType : 'all',
            responseHandler : responseHandler,
            columns : [
                {
                    field : '',
                    title : '序号',
                    formatter : function(value, row, index) {
                        return index + 1;
                    }
                },
                {
                    field : 'id',
                    title : '任务ID',
                    align : 'center',
                    valign : 'middle'
                },
                {
                    field : 'name',
                    title : '任务名称',
                    align : 'center',
                    valign : 'middle'
                },
                {
                    field : 'username',
                    title : '用户',
                    align : 'center',
                    valign : 'middle'
                },
                {
                    field : 'jobType',
                    title : '任务类型',
                    align : 'center',
                    valign : 'middle'
                },
                {
                    field : 'startTime',
                    title : '任务开始时间',
                    align : 'center',
                    valign : 'middle',
                    formatter : function(value, row, index) {
                        return formatDateTime(value);
                    }
                },
                {
                    field : 'finishTime',
                    title : '任务结束时间',
                    align : 'center',
                    valign : 'middle',
                    formatter : function(value, row, index) {
                        return formatDateTime(value);
                    }
                }
                ,
                {
                    field : 'severity',// 级别
                    title : 'severity',
                    align : 'center',
                    valign : 'middle'
                },
                {
                    field : 'score',// 得分
                    title : 'score',
                    align : 'center',
                    valign : 'middle'
                }

                ,
                {
                    field : 'kk',// 得分
                    title : '操作',
                    align : 'center',
                    valign : 'middle',
                    formatter : function(value, row, index) {
                        return "<a href='module/history/historyjobDetail.html?Id=" + row.id + "'>详情</a>";
                    }
                }, {
                    field : 'kh',// 得分
                    title : '操作',
                    align : 'center',
                    valign : 'middle',
                    formatter : function(value, row, index) {
                        return "<button type='button' class='btn btn-warning cpBtn' "+addToCompareDisabled+">加入对比</button>";
                    },
                    events: {
                        //点击事件
                        'click .btn': clickOnRow
                    }
                }
				/*
				 * , { field : 'trackingUrl', title : 'Address', align :
				 * 'center', valign : 'middle', vis }
				 */
            ]
        });
    }

    // 添加加入对比的点击事件
    function clickOnRow(e, value, row, index) {

        var cpItemCount = $(".cpItem").length;
        // 重复的不添加
        if(currentCpApp[""+row.id] != null){
            return;
        }
        if(cpItemCount >= 2){
            alert("一次只能进行两个任务的对比");
            return;
        }
        if(cpItemCount == 1){
            $(".cpBtn").attr("disabled",true);
            addToCompareDisabled = "disabled";
            $("#startCompareBtn").css("display","block");
        }
        var compareItemTemplate = $("#compareItemTemplate");
        compareItemTemplate.find(".cpAppId").html(row.id);
        compareItemTemplate.find(".cpAppName").html(row.name);
        compareItemTemplate.find(".cpAppType").html(row.jobType);
        compareItemTemplate.find(".cpAppUser").html(row.username);
        compareItemTemplate.find(".cpAppStartTime").html(row.startTime);

        $("#compareItemList").append('<div class="col-md-6 cpItem">'+compareItemTemplate.html()+'</div>');
        currentCpApp[""+row.id] = 1;

        $(".cpRemoveBtn").on("click",function(){
            var thisCpItem = $(this).closest(".cpItem");
            // 去掉去重记录
            currentCpApp[""+thisCpItem.find(".cpAppId").html()] = null;
            thisCpItem.remove();
            $(".cpBtn").attr("disabled",false);
            addToCompareDisabled = "";
            $("#startCompareBtn").css("display","none");
        });
    }

    function queryParams() {
        var param = {
            name : getName($("#Name").val()),
            startTime : getDateTime($("#startDate").val()),
            finishTime : getDateTime($("#endDate").val()),
            limit : this.limit, // 页面大小
            offset : this.offset, // 页码
            pageindex : this.pageNumber,
            pageSize : this.pageSize
        }
        return param;
    }

// 用于server 分页，表格数据量太大的话 不想一次查询所有数据，可以使用server分页查询，数据量小的话可以直接把sidePagination:
// "server" 改为 sidePagination: "client" ，同时去掉responseHandler:
// responseHandler就可以了，
    function responseHandler(res) {

        if (res.records!=0) {
            return {
                "rows" : res.result,
                "total" : res.records
            };
        } else {
            return {
                "rows" : [],
                "total" : 0
            };
        }
    }

// 时间戳转换成yyyy-mm-dd hh:mm:ss

    function formatDateTime(inputTime) {
        var date = new Date(inputTime);
        var y = date.getFullYear();
        var m = date.getMonth() + 1;
        m = m < 10 ? ('0' + m) : m;
        var d = date.getDate();
        d = d < 10 ? ('0' + d) : d;
        var h = date.getHours();
        h = h < 10 ? ('0' + h) : h;
        var minute = date.getMinutes();
        var second = date.getSeconds();
        minute = minute < 10 ? ('0' + minute) : minute;
        second = second < 10 ? ('0' + second) : second;
        return y + '-' + m + '-' + d + ' ' + h + ':' + minute + ':' + second;
    };
// 返回一个时间戳
    function getDateTime(dateTime) {
        if (dateTime != null && dateTime.length != 0) {
            var dd = new Date(dateTime).getTime();
            return dd;
        } else {
            return null;
        }
    };
// 返回一个正确的名称
    function getName(name) {
        if (name != null && name.length != 0) {
            return name;
        } else {
            return null;
        }
    }

// 一下是测试
    $('.form_datetime').datetimepicker({
        // language: 'fr',
        weekStart : 1,
        todayBtn : 1,
        autoclose : 1,
        todayHighlight : 1,
        startView : 2,
        forceParse : 0,
        showMeridian : 1,
        minuteStep : 3,
        language : 'zh-CN'
    });
    $('.form_date').datetimepicker({
        language : 'fr',
        weekStart : 1,
        todayBtn : 1,
        autoclose : 1,
        todayHighlight : 1,
        startView : 2,
        minView : 2,
        forceParse : 0
    });
    $('.form_time').datetimepicker({
        language : 'fr',
        weekStart : 1,
        todayBtn : 1,
        autoclose : 1,
        todayHighlight : 1,
        startView : 1,
        minView : 0,
        maxView : 1,
        forceParse : 0
    });

    $(".form_datetime").datetimepicker({
        format : "dd MM yyyy - hh:ii",
        autoclose : true,
        todayBtn : true,
        startDate : new Date(),
        minuteStep : 1
    });
})();
