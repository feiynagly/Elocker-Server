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
            <h3 class="page-header">密码修改</h3>
        </div>
        <!-- /.col-lg-12 -->
    </div>
    <!-- /.row -->
    <div class="row">
        <div class="panel panel-default">
            <div class="panel-body">
                <form id="passwd" name="passwd" class="form-horizontal form-label-left">
                    <div class="form-group">
                        <label class="control-label col-md-4 col-sm-4 col-xs-4" for="oldpassword">原密码 *</label>
                        <div class="col-md-3 col-sm-3 col-xs-3">
                            <input type="password" id="oldpassword" name="oldpassword" class="form-control input-md">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-4 col-sm-4 col-xs-4" for="password1">新密码 *</label>
                        <div class="col-md-3 col-sm-3 col-xs-3">
                            <input type="password" id="password1" name="password1" class="form-control input-md">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-4 col-sm-4 col-xs-4" for="password2">再次输入新密码 *</label>
                        <div class="col-md-3 col-sm-3 col-xs-3">
                            <input type="password" id="password2" name="password2" class="form-control input-md">
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-md-7 col-sm-7 col-xs-7" style="text-align:right">
                            <button data-dismiss="modal" class="btn btn-md btn-primary">取消</button>
                            <button onclick="changePassword(event)" class="btn btn-md btn-primary"
                                    style="margin-left:5%;margin-right:8%">确定
                            </button>
                        </div>
                    </div>
                </form>
            </div>
            <!--./panel-body-->
        </div>
        <!-- ./panel -->
    </div>
    <!-- ./row -->
    <script src="dist/js/usermgmt.js"></script>
</div>
</body>
</html>
