$(document).ready(function () {
    //初始化jquery validate
    $('#passwd').validate({
        errorPlacement: function (error, element) {
            $(element).parent().append(error)
        },
        errorElement: 'span',
        //取消每输入一个字符就开始校验，输入完成后输入框失去焦点后再校验
        onkeyup: true,
        rules: {
            oldpassword: {required: true},
            password1: {required: true},
            password2: {required: true, equalTo: '#password1'}
        },
        messages: {
            oldpassword: {required: '请输入原密码'},
            password1: {required: '请输入新密码'},
            password2: {required: '请再次确认密码', equalTo: '两次输入的密码不一致'}
        }
    })

})

//修改密码
function changePassword(event) {
    event.preventDefault()
  var oldpasswd = $('#oldpassword').val()
    var passwd1 = $('#password1').val()
    var passwd2 = $('#password2').val()
    if ($('#passwd').valid()) {
        $.ajax({
            type: 'POST',
            url: 'user/changePassword',
            dataType: 'json',
            contentType: 'application/json',
            dataType: 'json',
            data: JSON.stringify({'oldpass': md5(oldpasswd), 'newpass': md5(passwd1)}),
            success: function (data) {
                if (data.hasOwnProperty('error')) {
                    alert(data.error)
                } else {
                    alert('密码修改成功，请重新登录')
                }
            },
            error: function () {
                alert('网络错误,密码修改失败')
            }
        })
    }
}




