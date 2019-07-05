<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>

<!DOCTYPE html>
<html>
<head>

    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="login">
    <meta name="author" content="login">

    <title>登录</title>

    <!--设置默认图标-->
    <link rel="shortcut icon" href="/dist/css/favicon.ico"/>
    <link rel="bookmark" href="/dist/css/favicon.ico"/>

    <!-- Bootstrap Core CSS -->
    <link href="vendor/bootstrap/css/bootstrap.min.css" rel="stylesheet"/>

    <!-- Custom CSS -->
    <link href="dist/css/admin-2.css" rel="stylesheet"/>

</head>

<body>
<div class="container">
    <div class="row">
        <div class="col-md-4 col-md-offset-4">
            <div class="login-panel panel panel-default">
                <div class="panel-heading">
                    <h2 class="panel-title" style="text-align:center">智能锁管理系统</h2>
                </div>
                <div class="panel-body">
                    <form id="loginform" name="loginform" class="form-horizontal">
                        <div class="form-group" style="margin-left: 2%;margin-top: 6%;margin-right: 2%;">
                            <input class="form-control" placeholder="手机号" id="phoneNum" name="phoneNum" autofocus
                                   required/>
                        </div>
                        <div class="form-group" style="margin-left: 2%;margin-bottom: 8%;margin-right: 2%;">
                            <input class="form-control" placeholder="密码" id="password" name="password" type="password"
                                   required/>
                        </div>
                        <button class="btn btn-md btn-primary" style="margin-left: 2%;width:96%" onclick="login(event)">
                            登录
                        </button>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- jQuery -->
<script src="vendor/jquery/jquery-3.1.0.min.js"></script>
<script src="vendor/jquery/jquery.validate.min.js"></script>
<script src="vendor/jquery/localization/messages_zh.js"></script>

<!-- Bootstrap Core JavaScript -->
<script src="vendor/bootstrap/js/bootstrap.min.js"></script>

<!-- Login controller Javascript -->
<script src="dist/js/login.js"></script>
<script src="vendor/md5/md5.min.js"></script>


</body>
</html>
