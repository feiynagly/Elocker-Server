<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    String path = request.getContextPath();
    String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
%>

<!DOCTYPE html>
<html>
<body>
<div>
    <div class="row">
        <div class="col-lg-12 col-md-12 col-sm-12">
            <h3 class="page-header">日志管理</h3>
        </div>
        <!-- /.col-lg-12 -->
    </div>
    <!-- /.row -->
    <div class="row">
        <div class="col-lg-12 col-md-12 col-sm-12">
            <div class="panel panel-default">
                <div class="panel-heading" style="text-align:right">
                    <button class="btn btn-primary btn-md" onclick="updateLogTable()"><i class="fa fa-refresh"></i> 刷新
                    </button>
                </div>
                <!-- ./panel-heading -->
                <div class="panel-body">
                    <div class="form-horizontal">
                        <div id="paging" class="col-lg-2 col-md-2 col-sm-2">
                            <div>
                                <select id="lockerSelect" onchange="updateLogTable()" class="form-control input-md">
                                    <option value="">--全部--</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-lg-1 col-md-1 col-sm-1">
                        </div>
                        <!-- ./col-lg-3 -->
                        <div class="col-lg-6 col-md-6 col-sm-6">
                            <div class="form-inline">
                                <label for="timeRange">时间 :</label>
                                <div class="input-group">
                                    <input id="timeRange" size="44" type="text" class="input-md form-control">
                                    <span class="input-group-addon">
										    <i class="fa fa-calendar"></i>
										</span>
                                </div>
                            </div>
                        </div>
                        <!-- ./col-lg-6 -->
                        <div class="col-lg-3 col-md-3 col-sm-3">
                            <div class="form-group input-group">
                                <input type="text" id="searchVal" onchange="search()" placeholder="请输入关键字"
                                       class="input-md form-control">
                                <span class="input-group-btn">
                                    <button type="button" onclick="search()" class="btn btn-md btn-primary">搜索</button>
                                </span>
                            </div>
                        </div>
                        <!--./col-lg-3  -->
                    </div>
                    <table id="log" style="width:100%" class="table table-striped table-bordered table-hover">
                        <thead style="text-align:center ;white-space:nowrap">
                        <th style="text-align: center" width="2%"><strong>序号</strong></th>
                        <th style="text-align: center" width="6%"><strong>固件名称</strong></th>
                        <th style="text-align: center" width="6%"><strong>操作账户</strong></th>
                        <th style="text-align: center" width="8%"><strong>操作</strong></th>
                        <th style="text-align: center" width="10%"><strong>时间</strong></th>
                        <th style="text-align: center" width="20%"><strong>备注</strong></th>
                        </thead>
                    </table>
                    <!-- /.table-responsive -->
                </div>
                <!-- /.panel-body -->
            </div>
            <!-- /.panel -->
        </div>
        <!-- /.col-lg-12 -->
    </div>
    <!-- /.row -->
</div>
<script src="dist/js/logmgmt.js"></script>
</body>
</html>
