function logout() {
    $.ajax({
        type: "GET",
        contentType: "application/json",
        url: "login/logout",
        success: function (data) {
            if (data.hasOwnProperty("redirectUrl"))
                window.location.href = data.redirectUrl;

        },
        error: function (data) {
            alert("网络连接失败，请检查本地网络连接");
        }
    });
}

//页面刷新
function loadPage(url) {
    $("#content").load(url);
}

//固件列表
$("#lockerview").click(function () {
    $("#content").load("lockerview");
})

//授权列表
$('#authorizationview').click(function () {
  $('#content').load('authorizationview')
})

//日志管理
$("#logview").click(function () {
    $("#content").load("logview");
})

//日志管理
$("#userview").click(function () {
    $("#content").load("userview");
})
