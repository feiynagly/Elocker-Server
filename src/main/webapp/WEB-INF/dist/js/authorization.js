//var startTime = moment().format("YYYY-MM-DD HH:mm:ss");
//var endTime = moment().format("YYYY-MM-DD HH:mm:ss");
var authorizationtable

$(document).ready(function () {
    $("input[name='checkList']").iCheck({
        checkboxClass: 'icheckbox_flat-blue',
        radioClass: 'iradio_flat-blue',
        increaseArea: '20%'
    });

    /*初始化表格*/
  authorizationtable = $('#authorizationlist').DataTable({
        dom: 'rt<".table-foot-margin"><".col-md-2" l><".col-md-4" i><".col-md-6" p>',
        "aoColumnDefs": [{
            "bSortable": false,
            "aTargets": [0, 1, 2]
        }, {
            "visible": false,
            "aTargets": [0]
        }],
        "bStateSave": true,
        "scrollY": true,
        "paging": true,
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

    //初始化数据
  updateAuthorizationList()

    /*初始化日期选择器*/
    var objs = [$("#add_startTime"), $("#add_endTime"), $("#edit_startTime"), $("#edit_endTime")];
    for (var i = 0; i < objs.length; i++)
        objs[i].daterangepicker({
            "startDate": moment(), //设置开始日期
            "showDropdowns": true, //年月份下拉框
            "singleDatePicker": true, //单日历
            "minDate": moment().startOf('day'),
            "showDropdowns": true,     //显示下拉框选择年份
            "timePicker": true,        //显示时分秒
            "timePickerIncrement": 1,  //时间增量
            "timePicker24Hour": true, //24小时时间制
            "autoUpdateInput": true,  //自动更新输入框
            "showWeekNumbers": true,
            "opens": "center",
            "locale": {
                "format": 'YYYY-MM-DD HH:mm:ss',
                "applyLabel": '确认',
                "cancelLabel": '取消',
                "fromLabel": '起始时间',
                "firstDay": 1
            }
        });

    // 初始化iCheck
    $("input[name='checkAll']").iCheck({
        checkboxClass: 'icheckbox_flat-blue',
        radioClass: 'iradio_flat-blue',
        increaseArea: '20%'
    });
    $("input[name='columnItem']").iCheck({
        checkboxClass: 'icheckbox_flat-blue',
        radioClass: 'iradio_flat-blue',
        increaseArea: '20%'
    });

    // checkbox全选、反选
    $("#checkAll").on('ifChecked', function (event) {
        $("input[name='checkList']").iCheck('check');
    });

    $("#checkAll").on('ifUnchecked', function (event) {
        $("input[name='checkList']").iCheck('uncheck');
    });

    //当表格分页改变时，为新增的条目初始化iCheck
  authorizationtable.on('draw', function () {
        $("input[name='checkList']").iCheck({
            checkboxClass: 'icheckbox_flat-blue',
            radioClass: 'iradio_flat-blue',
            increaseArea: '20%'
        });
    })

    // 隐藏或显示表格指定列
    $("input[name='columnItem']").on('ifChanged', function () {
      var column = authorizationtable.column($(this).attr('data-column'))
        if ($(this).is(":checked"))
            column.visible(true);
        else
            column.visible(false);
    })

})

// 默认表格所有列都显示
$("input[name='columnItem']").iCheck('check');

//设置每页显示条目数
function pageLenChange() {
    var pageLength = Number($("#pageLen").val());
  authorizationtable.page.len(pageLength).draw()
}

//打开新增加锁页面
function openAddAuthorizationPage () {
    /!*设置固件选择列表初始值*!/
    $.ajax({
        type: "GET",
      url: 'locker/get',
        contentType: "application/json",
        success: function (data) {
            if (data.hasOwnProperty("error")) {
                alert(data.error);
                return;
            }
            var lockers = data.lockerList;
            if (lockers == "" || lockers == null)
                return;
            for (var i = 0; i < lockers.length; i++) {
                var descriptionHtml = "<option value=" + lockers[i].serial + ">" + lockers[i].description + "</option>";
                $("#add_lockerdecription").append(descriptionHtml)
            }
        },
        error: function (data) {
            alert("无法获取固件列表,请检查本地网络连接");
        }
    });
  $('#addauthorizationmodal').modal()
}

function addAuthorization (event) {
    event.preventDefault();
  var postData = new Object()

  postData.serial = $('#add_lockerdecription').val()
  postData.toAccount = $('#add_toAccount').val()
  postData.startTime = $('#add_startTime').val()
  postData.endTime = $('#add_endTime').val()
  postData.description = $('#add_description').val()

  if (postData.toAccount == '') {
        alert("Account to authorise to can not be empty");
        return;
    }

    $.ajax({
        type: "POST",
      url: 'authorization/add',
        dataType: "json",
        contentType: "application/json",
      data: JSON.stringify(postData),
        success: function (data) {
            if (data.hasOwnProperty("error"))
                alert(data.error);
            else {
              $('#addauthorizationmodal').modal('hide')
              updateAuthorizationList()
            }
        },
        error: function (data) {
            alert("添加新硬件失败，请检查本地网络连接");
        }
    });
}

//更新AuthorizationList
function updateAuthorizationList () {
    $.ajax({
        type: "GET",
      url: 'authorization/get',
        contentType: "application/json",
        success: function (data) {
            if (data.hasOwnProperty("error")) {
                alert(data.error);
                return;
            }
          if (data.authorizationlist == '' || data.authorizationlist == null)
                return;
            // 清空表格数据，重新加载
          $('#authorizationlist').dataTable().fnClearTable()
          $('#authorizationlist').dataTable().fnAddData(packagingDatatableData(data.authorizationlist), true)

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
        tr.push(data[i].id);
        tr.push(checkBoxHtml);
        tr.push(i + 1);
        tr.push(data[i].lockerDescription);
        tr.push(data[i].fromAccount);
        tr.push(data[i].toAccount);
        tr.push(data[i].startTime);
        tr.push(data[i].endTime);
        tr.push(data[i].description);
        trs.push(tr);
    }
    return trs;
}

var id;

function openEditAuthorizationPage () {
    /*获取选中的条目*/
    var rows = new Array()
    $("input[name='checkList']").each(function () {
        if ($(this).parent().hasClass("checked")) {
            var row_obj = $(this).parents("tr");
            rows.push(row_obj);
        }
    });
    if (rows.length != 1) {
        alert("请仅选择一个条目进行修改");
        return;
    }

    /*设置初始值*/
  id = authorizationtable.row(rows[0]).data()[0]
  $('#edit_lockerdescription').val(authorizationtable.row(rows[0]).data()[3])
  $('#edit_toAccount').val(authorizationtable.row(rows[0]).data()[5])
  $('#editauthorizationmodal').modal()

}

function editAuthorization () {
  var postData = new Object()
  postData.description = $('#edit_description').val()
  postData.startTime = $('#edit_startTime').val()
  postData.endTime = $('#edit_endTime').val()
  postData.id = id
    $.ajax({
        type: "POST",
      url: 'authorization/update',
        contentType: "application/json",
        dataType: "json",
      data: JSON.stringify(postData),
        success: function (data) {
          $('#editauthorizationmodal').modal('hide')
          updateAuthorizationList()
        },
        error: function (data) {
            alert("网络连接失败，请检查本地网络连接")
        }
    })
}

//删除硬件
function delAuthorization () {
    var ids = new Array()
    $("input[name='checkList']").each(function () {
        if ($(this).parent().hasClass("checked")) {
            var row_obj = $(this).parents("tr");
          ids.push(authorizationtable.row(row_obj).data()[0])
        }
    });
    if (ids.length == 0) {
        alert("请至少选择一个条目");
        return;
    }
    $.ajax({
        type: "POST",
      url: 'authorization/delete',
        contentType: "application/json",
        dataType: "json",
        data: JSON.stringify({"ids": ids}),
        success: function (data) {
            //移除页面中删除成功的授权
            for (var i = 0; i < data.success.length; i++) {
                var id = data.success[i];
                $("input[name='checkList']").each(function () {
                    var row_obj = $(this).parents("tr");
                  if (id == authorizationtable.row(row_obj).data()[0])
                        row_obj.remove();
                });
            }
            //输出报错信息
            if (data.error.length > 0) {
                var log = "";
                for (var i = 0; i < data.error.length; i++)
                    log = log + "删除授权 " + data.error[i] + "失败" + "\n";
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
  authorizationtable.search(val, false, false).draw()
}



