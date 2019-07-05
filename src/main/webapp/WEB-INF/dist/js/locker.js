$(document).ready(function () {
    $("input[name='checkList']").iCheck({
        checkboxClass: 'icheckbox_flat-blue',
        radioClass: 'iradio_flat-blue',
        increaseArea: '20%'
    });

    /*初始化表格*/
    table = $('#lockerList').DataTable({
        dom: 'rt<".table-foot-margin"><".col-md-2" l><".col-md-4" i><".col-md-6" p>',
        "aoColumnDefs": [{
            "bSortable": false,
            "aTargets": [0]
        }],
        "aaSorting": [[1, "asc"]],
        "bStateSave": true,
        "scrollY": true,
        "paging": true,
        "destroy": true,
        "language": {
            "search": "搜索",
            "processing": "正在加载……",
            "lengthMenu": "每页显示_MENU_ 项",
            "zeroRecords": "没有匹配结果",
            "info": "第 _PAGE_ 页 ( 共 _PAGES_ 页 , _TOTAL_ 项  )",
            "infoEmpty": "显示第 0 至 0 项结果，共 0 项",
            "infoFiltered": "(由 _MAX_ 项结果过滤)",
            "paginate": {
                "first": "首页",
                "previous": "上一页",
                "next": "下一页",
                "last": "末页"
            }
        }
    });

    updateLockerList();

    // 初始化iCheck
    $("input[name='checkAll']").iCheck({
        checkboxClass: 'icheckbox_flat-blue',
        radioClass: 'iradio_flat-blue',
        increaseArea: '20%'
    });
    $("input[name='checkList']").iCheck({
        checkboxClass: 'icheckbox_flat-blue',
        radioClass: 'iradio_flat-blue',
        increaseArea: '20%'
    });

    // checkbox全选、反选
    $("#checkAll").on('ifChecked', function (event) {
        $("input[name='checkList']").iCheck('check');
        // $('#flowTable tbody tr').addClass('odd');
    });

    $("#checkAll").on('ifUnchecked', function (event) {
        $("input[name='checkList']").iCheck('uncheck');
        // $('#flowTable tbody tr').removeClass('selected');
    });

    //当表格分页改变时，为新增的条目初始化iCheck
    table.on("draw", function () {
        $("input[name='checkList']").iCheck({
            checkboxClass: 'icheckbox_flat-blue',
            radioClass: 'iradio_flat-blue',
            increaseArea: '20%'
        });
    })

})

//打开新增加锁页面
function openAddLockerPage() {
    $("#addlockermodal").modal();
}


function addLocker(event) {
    event.preventDefault();
  var postData = new Object()

  postData.serial = $('#add_lockerSerial').val()
  postData.description = $('#add_lockerDescription').val()

    $.ajax({
        type: "POST",
      url: 'locker/add',
        dataType: "json",
        contentType: "application/json",
      data: JSON.stringify(postData),
        success: function (data) {
            if (data.hasOwnProperty("error"))
                alert(data.error);
            else {
                $("#addlockermodal").modal("hide");
                updateLockerList();
            }
        },
        error: function (data) {
            alert("添加新硬件失败，请检查本地网络连接");
        }
    });
}

//更新lockerList
function updateLockerList() {
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
            // 清空表格数据，重新加载
            $('#lockerList').dataTable().fnClearTable();
            $('#lockerList').dataTable().fnAddData(packagingDatatableData(data.lockerList), true);

            // 给新增的选框加上iCheck CSS
            $("input[name='checkList']").iCheck({
                checkboxClass: 'icheckbox_flat-blue',
                radioClass: 'iradio_flat-blue',
                increaseArea: '20%'
            });
        },
        error: function (data) {
            alert("请检查本地网络连接");
        }
    });
}

//将Serverlet返回值转换为DataTable可以识别的数据类型
function packagingDatatableData(data) {
    var checkBoxHtml = '<input name="checkList" type="checkbox">';
    var trs = [];
    for (var i = 0; i < data.length; i++) {
        var tr = [];
        tr.push(checkBoxHtml);
        tr.push(i + 1);
        tr.push(data[i].serial);
        tr.push(data[i].description);
        tr.push(data[i].hwType);
        tr.push(data[i].phoneNum);
        tr.push(data[i].createTime);
        trs.push(tr);
    }
    return trs;
}

function openEditLockerPage() {
    /*获取选中的条目*/
    var tds = new Array();
    $("input[name='checkList']").each(function () {
        if ($(this).parent().hasClass("checked"))
            tds.push($(this).parent().parent());
    });
    if (tds.length != 1) {
        alert("请仅选择一个条目进行修改");
        return;
    }

    //获取选中行的除第一个元素外的其它元素
    var tr = $(tds[0]).nextAll();
    var serial = $(tr[1]).text();

    /*设置初始值*/
    $("#edit_serial").val(serial);
    $("#editlockermodal").modal();

}

function updateLocker () {
    var description = $("#edit_description").val();
    var serial = $("#edit_serial").val();
    $.ajax({
        type: "POST",
      url: 'locker/update',
        contentType: "application/json",
        dataType: "json",
        data: JSON.stringify({"description": description, "serial": serial}),
        success: function (data) {
            $("#editlockermodal").modal("hide");
            updateLockerList();
        },
        error: function (data) {
            alert("网络连接失败，请检查本地网络连接")
        }
    })
}

//删除硬件
function delLocker() {
    var lockerSerials = new Array()
    $("input[name='checkList']").each(function () {
        if ($(this).parent().hasClass("checked")) {
            var obj = $(this).parent().parent().next().next();
            lockerSerials.push(obj.text());
        }
    })
    if (lockerSerials.length == 0) {
        alert("请至少选择一个条目");
        return;
    }
    $.ajax({
        type: "POST",
      url: 'locker/delete',
        contentType: "application/json",
        dataType: "json",
        data: JSON.stringify({"lockerSerials": lockerSerials}),
        success: function (data) {
            //移除页面中删除成功的流表
            for (var i = 0; i < data.success.length; i++) {
                var serial = data.success[i];
                $("input[name='checkList']").each(function () {
                    //表格中serial元素对应的对象
                    var obj = $(this).parent().parent().next().next();
                    if (obj.text() == serial) {
                        var tr = $(this).parent().parent().parent();
                        tr.remove();
                    }
                })
            }
            //输出报错信息
            if (data.error.length > 0) {
                var log = "";
                for (var i = 0; i < data.error.length; i++)
                    log = log + "删除固件 " + data.error[i] + "失败" + "\n";
                alert(log);
            }
        },
        error: function () {
            alert("网络连接失败，请检查本地网络连接");
        }
    });
}

// 自定义搜索
function search() {
    var val = $('#searchVal').val();
    table.search(val, false, false).draw();
}



