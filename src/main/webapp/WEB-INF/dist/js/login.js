$(document).ready(function () {
    $('#loginform').validate({
        errorPlacement: function (error, element) {
            $(element).parent().append(error)
        },
        errorElement: 'span',
        onkeyup: true,
        messages: {
            phoneNum: {required: '请输入用户名'},
            password: {required: '请输入密码'}
        }
    })

})

function login(event) {
    event.preventDefault()

    if ($('#loginform').valid()) {
        var phoneNum = $('#phoneNum').val()
        var password = $('#password').val()
        var enc_passwd = md5(phoneNum + md5(password))

        $.ajax({
            type: 'POST',
            async: true,
            cache: false,
            url: 'login/login',
            data: JSON.stringify({phoneNum: phoneNum, password: enc_passwd}),
            contentType: 'application/json',
            dataType: 'json',
            success: function (data) {
                if (data.hasOwnProperty('redirectUrl')) {
                    window.location.href = data.redirectUrl
                }
            },
            error: function (data) {
                if (data.hasOwnProperty('responseJSON')) {
                    alert(data.responseJSON.error)
                } else {
                    alert('Server is not reachable,Please check the local network')
                }
            }
        })
    }
}