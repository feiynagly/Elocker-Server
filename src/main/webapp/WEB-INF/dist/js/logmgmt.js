var startTime = moment().subtract(3, "months").format("YYYY-MM-DD HH:mm:ss");
var endTime = moment().format("YYYY-MM-DD HH:mm:ss");
var serial;
var logtable;

$(document).ready(function () {
    logtable = $("#log").DataTable({
        "dom": 'rt<".table-foot-margin"><".col-md-2" l><".col-md-4" i><".col-md-6" p>',
        "aoColumnDefs": [{
            "bSortable": false,
            "aTargets": [0]
        }],
        "bStateSave": false,
        "scrollY": true,
        "scrollX": true,
        "bScrollCollapse": true,
        "language": {
            "search": "搜索    ",
            "processing": "正在加载……",
            "lengthMenu": "每页显示  _MENU_ 项",
            "zeroRecords": "没有匹配结果",
            "info": "第 _PAGE_ 页 ( 共 _PAGES_ 页  )",
            "infoEmpty": "显示第 0 至 0 项结果，共 0 项",
            "infoFiltered": "(由   _MAX_ 项结果过滤)",
            "paginate": {
                "first": "首页",
                "previous": "上一页",
                "next": "下一页",
                "last": "末页"
            }
        }
    });
    // End dataTables

    $("#timeRange").daterangepicker({
        'startDate': moment().subtract(30, 'days'),        //默认起始日期
        "endDate": moment(),            //默认结束日期
        "minDate": moment().subtract(1, 'year'),
        "maxDate": moment().endOf('day'),
        "showDropdowns": true,     //显示下拉框选择年份
        "timePicker": true,        //显示时分秒
        "timePickerIncrement": 1,  //时间增量
        "timePicker24Hour": true, //24小时时间制
        "autoApply": true,    //选取时间后自动确定
        "autoUpdateInput": true,  //自动更新输入框
        "locale": {
            "format": 'YYYY-MM-DD HH:mm:ss',
            "separator": " 至 ",
            "applyLabel": '确认',
            "cancelLabel": '取消',
            "fromLabel": '起始时间',
            "toLabel": '结束时间',
            "customRangeLabel": '自定义',
            "firstDay": 1
        },
        "ranges": {
            '最近3小时': [moment().subtract(3, 'hours'), moment()],
            '今日': [moment().startOf('day'), moment()],
            '昨日': [moment().subtract(1, 'days').startOf('day'), moment().startOf('day')],
            '最近七天': [moment().subtract(6, 'days'), moment()],
            '最近30天': [moment().subtract(29, 'days'), moment()]
        }
    }, function (start, end, label) {
        startTime = start.format("YYYY-MM-DD HH:mm:ss");
        endTime = end.format("YYYY-MM-DD HH:mm:ss");
    });

    //所选时间段改变后自动更新
    $("#timeRange").change(function () {
        updateLogTable();
    })

    //初始化iCheck
    $("input[name='columnItem']").iCheck({
        checkboxClass: 'icheckbox_flat-blue',
        radioClass: 'iradio_flat-blue',
        increaseArea: '20%'
    });

    updateLogTable();

    //获取锁列表
    $.ajax({
        type: "GET",
        url: 'locker/get',
        contentType: "application/json",
        success: function (data) {
            if (data.hasOwnProperty("error")) {
                alert(data.error);
                return;
            }
            if (data.lockerList == "" || data.lockerList == null)
                return;
            for (var i = 0; i < data.lockerList.length; i++) {
                var html = "<option value=" + data.lockerList[i].serial + ">" + data.lockerList[i].description + "</option>";
                $("#lockerSelect").append(html);
            }

        },
        error: function (data) {
            alert("请检查本地网络连接");
        }
    });


})

//设置每页显示条目数
function pageLenChange() {
    var pageLength = Number($("#pageLen").val());
    logtable.page.len(pageLength).draw();
}

//搜索
function search() {
    var regex = $("#searchVal").val();
    logtable.search(regex).draw();
}

//将返回值格式化为dataTable 能直接读取的二维数组
function packagingDatatableData(data) {
    var trs = [];
    for (var i = 0; i < data.length; i++) {
        var tr = new Array();
        tr.push(i + 1);
        tr.push(data[i].lockerDescription);
        tr.push(data[i].phoneNum);
        tr.push(data[i].operation);
        tr.push(data[i].sTime);
        tr.push(data[i].description);
        trs.push(tr);
    }
    return trs;
}

function updateLogTable() {
    var serial = $("#lockerSelect").val();
    var url = 'log/get?serial=' + serial + '&startTime=' + startTime + ' &endTime=' + endTime
    $.ajax({
        type: "GET",
        url: encodeURI(encodeURI(url)),
        contentType: "application/json",
        success: function (data) {
            $('#log').dataTable().fnClearTable(); // 清空表格
            //如果没有数据或者SQL获取日志失败，则直接返回
            if (data.logs == null || data.logs.length == 0)
                return;
            $('#log').dataTable().fnClearTable();
            $('#log').dataTable().fnAddData(packagingDatatableData(data.logs), true);
        }
    });
}


