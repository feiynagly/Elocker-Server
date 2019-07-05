<%@ page language="java" pageEncoding="utf-8" %>
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
    <meta name="description" content="">
    <meta name="author" content="">

    <title>智能锁后台管理系统</title>

    <!--设置默认图标-->
    <link rel="shortcut icon" href="dist/css/favicon.ico"/>
    <link rel="bookmark" href="dist/css/favicon.ico"/>

    <!-- Bootstrap Core CSS -->
    <link href="vendor/bootstrap/css/bootstrap.min.css" rel="stylesheet">

    <!-- DataTables Core CSS-->
    <link href="vendor/datatables-plugins/dataTables.bootstrap.css" rel="stylesheet">

    <!-- MetisMenu CSS -->
    <link href="vendor/metisMenu/metisMenu.min.css" rel="stylesheet">

    <!-- Custom Fonts -->
    <link href="vendor/font-awesome/css/font-awesome.min.css" rel="stylesheet" type="text/css">

    <!-- iCheck CSS  -->
    <link href="vendor/icheck/css/blue.css" rel="stylesheet" type="text/css">

    <!-- Datetime Picker CSS -->
    <link href="vendor/daterangepicker/css/daterangepicker.css" rel="stylesheet" type="text/css">

    <!-- Custom CSS -->
    <link href="dist/css/admin-2.css" rel="stylesheet">

</head>

<body>

<div id="wrapper">

    <!-- Navigation -->
    <nav class="navbar navbar-default navbar-static-top" role="navigation" style="margin-bottom: 0">
        <div class="navbar-header">
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a class="navbar-brand" href="main">智能锁管理系统</a>
        </div>
        <!-- /.navbar-header -->

        <ul class="nav navbar-top-links navbar-right">
            <!-- /.dropdown -->
            <li class="dropdown">
                <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                    <i class="fa fa-user fa-fw"></i> <i class="fa fa-caret-down"></i>
                </a>
                <ul class="dropdown-menu dropdown-user">
                    <li><a onclick="loadPage('/userview')"><i class="fa fa-user fa-fw"></i> 用户管理</a>
                    </li>
                    <li><a href="#"><i class="fa fa-gear fa-fw"></i> 系统设置</a>
                    </li>
                    <li class="divider"></li>
                    <li><a onclick="logout()"><i class="fa fa-sign-out fa-fw"></i> 退出系统</a>
                    </li>
                </ul>
                <!-- /.dropdown-user -->
            </li>
            <!-- /.dropdown -->
        </ul>
        <!-- /.navbar-top-links -->

        <div class="navbar-default sidebar" role="navigation">
            <div class="sidebar-nav navbar-collapse">
                <ul class="nav" id="side-menu">
                    <li class="sidebar-search">
                        <div class="input-group custom-search-form">
                            <input type="text" class="form-control" placeholder="Search...">
                            <span class="input-group-btn">
                                   <button class="btn btn-default" type="button">
                                      <i class="fa fa-search"></i>
                                   </button>
                                </span>
                        </div>
                        <!-- /input-group -->
                    </li>
                    <li>
                        <a href="#"><i class="fa fa-sitemap"></i>固件管理<span class="fa arrow"></span></a>
                        <ul class="nav nav-second-level">
                            <li>
                                <a href="#" id="lockerview">固件列表</a>
                            </li>
                            <li>
                                <a href="#" id="authorizationview">授权列表</a>
                            </li>
                        </ul>
                        <!--/.nav-second-level -->
                    </li>
                    <li>
                        <a href="#"><i class="fa fa-cogs"></i> 系统管理<span class="fa arrow"></span></a>
                        <ul class="nav nav-second-level">
                            <li>
                                <a href="#" id="logview">日志查询</a>
                            </li>
                            <li>
                                <a href="#" id="userview">密码修改</a>
                            </li>
                        </ul>
                        <!-- /.nav-second-level -->
                    </li>
                </ul>
            </div>
            <!-- /.sidebar-collapse -->
        </div>
        <!-- /.navbar-static-side -->
    </nav>

    <div id="page-wrapper">
        <div class="row">
            <div class="col-lg-12 col-md-12 col-sm-12" id="content">
                <!-- here display the content -->
            </div>
            <!-- /.col-lg-12 -->
        </div>
    </div>
    <!-- /#page-wrapper -->

</div>
<!-- /#wrapper -->

<!-- jQuery -->
<script src="vendor/jquery/jquery-3.1.0.min.js"></script>
<script src="vendor/jquery/jquery.validate.min.js"></script>
<script src="vendor/jquery/localization/messages_zh.js"></script>

<!-- dataTables JavaScript -->
<script src="vendor/datatables/js/jquery.dataTables.min.js"></script>
<script src="vendor/datatables-plugins/dataTables.bootstrap.min.js"></script>

<!-- Bootstrap Core JavaScript -->
<script src="vendor/bootstrap/js/bootstrap.min.js"></script>

<!-- Metis Menu Plugin JavaScript -->
<script src="vendor/metisMenu/metisMenu.min.js"></script>

<!-- iCheck JavaScript -->
<script src="vendor/icheck/js/icheck.min.js"></script>

<!-- Daterange Picker JavaScript -->
<script src="vendor/daterangepicker/js/moment.min.js"></script>
<script src="vendor/daterangepicker/js/daterangepicker.js"></script>
<script src="vendor/daterangepicker/js/zh-cn.js"></script>

<!-- Custom JavaScript -->
<script src="dist/js/admin-2.js"></script>
<script src="dist/js/main.js"></script>
<script src="vendor/md5/md5.min.js"></script>

</body>

</html>