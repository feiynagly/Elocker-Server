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
            <h3 class="page-header">授权列表</h3>
        </div>
        <!-- /.col-lg-12 -->
    </div>
    <!-- /.row -->
    <div class="row">
        <div class="panel panel-default">
            <div class="panel-body">
                <div class="form-horizontal clearfix">
                    <div class="col-lg-3 col-md-3 col-sm-3">
                        <div class="form-group input-group">
                            <input type="text" id="searchVal" onchange="search()" placeholder="请输入关键字"
                                   class="input-md form-control">
                            <span class="input-group-btn">
                                <button type="button" onclick="search()" class="btn btn-md btn-primary">搜索</button>
                            </span>
                        </div>
                    </div>
                    <div class="col-lg-9 col-md-9 col-sm-9" style="text-align:right">
                        <div class="btn-group">
                            <button id="selectColumn" class="btn btn-primary btn-md dropdown-toggle"
                                    data-toggle="dropdown">
                                <i class="fa fa-reorder"></i> 设置
                            </button>
                            <ul class="dropdown-menu" role="menu">
                                <li><input type="checkbox" name="columnItem" data-column="2"><span
                                        style="margin-left: 3%">序号</span></li>
                                <li><input type="checkbox" name="columnItem" data-column="3"><span
                                        style="margin-left: 3%">序列号</span></li>
                                <li><input type="checkbox" name="columnItem" data-column="4"><span
                                        style="margin-left: 3%">授权者</span></li>
                                <li><input type="checkbox" name="columnItem" data-column="5"><span
                                        style="margin-left: 3%">授权给</span></li>
                                <li><input type="checkbox" name="columnItem" data-column="6"><span
                                        style="margin-left: 3%">开始时间</span></li>
                                <li><input type="checkbox" name="columnItem" data-column="7"><span
                                        style="margin-left: 3%">截止时间</span></li>
                                <li><input type="checkbox" name="columnItem" data-column="8"><span
                                        style="margin-left: 3%">备注</span></li>
                            </ul>
                        </div>
                        <button id="addauthorization" class="btn btn-primary btn-md"
                                onclick="openAddAuthorizationPage()"><i
                                class="fa fa-plus"></i> 新增
                        </button>
                        <button id="editauthorization" class="btn btn-primary btn-md"
                                onclick="openEditAuthorizationPage()"><i
                                class="fa fa-edit"></i> 编辑
                        </button>
                        <button id="delauthorization" class="btn btn-primary btn-md" onclick="delAuthorization()"><i
                                class="fa fa-trash-o"></i> 删除
                        </button>
                        <button id="refresh" class="btn btn-primary btn-md" onclick="updateAuthorizationList()"><i
                                class="fa fa-refresh"></i> 刷新
                        </button>
                    </div>
                    <table id="authorizationlist" style="width:100%"
                           class="table table-striped table-bordered table-hover cell-border">
                        <thead>
                        <tr>
                            <th style="text-align:center" width="7%">ID</th>
                            <th style="text-align:center" width="5%">
                                <input id="checkAll" name="checkAll" type="checkbox">
                            </th>
                            <th style="text-align:center" width="7%"><strong>序号</strong></th>
                            <th style="text-align:center" width="16%"><strong>固件名称</strong></th>
                            <th style="text-align:center" width="14%"><strong>授权者</strong></th>
                            <th style="text-align:center" width="14%"><strong>授权给</strong></th>
                            <th style="text-align: center" width="15%"><strong>开始时间</strong></th>
                            <th style="text-align:center" width="15%"><strong>结束时间</strong></th>
                            <th style="text-align:center" width="14%"><strong>备注</strong></th>
                        </tr>
                        </thead>
                        </tbody>
                    </table>
                    <!-- ./table -->
                </div>
                <!-- ./panel-body -->
            </div>
            <!-- ./panel -->
        </div>
        <!-- ./row -->
        <div id="addauthorizationmodal" class="modal fade page-wrapper"
             style="padding-right: 9%;padding-top: 2%;padding-left: 3%">
            <div class="panel panel-default">
                <form id="addauthorizationform" class="form-horizontal form-label-left panel-body">
                    <div class="form-group">
                        <label class="control-label col-md-4 col-sm-3 col-xs-3" for="add_lockerdecription"> 锁名称
                            *</label>
                        <div class="col-md-4 col-sm-4 col-xs-4">
                            <select id="add_lockerdecription" class="form-control input-sm">
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-4 col-sm-3 col-xs-3" for="add_toAccount">授权给 *</label>
                        <div class="col-md-4 col-sm-4 col-xs-4">
                            <input type="text" id="add_toAccount" class="form-control input-sm" placeholder="请输入手机号"
                                   required>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-4 col-sm-3 col-xs-3" for="add_startTime">开始时间 *</label>
                        <div class="col-md-4 col-sm-4 col-xs-4">
                            <input type="text" id="add_startTime" class="form-control input-sm">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-4 col-sm-3 col-xs-3" for="add_endTime">截止时间 *</label>
                        <div class="col-md-4 col-sm-4 col-xs-4">
                            <input type="text" id="add_endTime" class="form-control input-sm">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-4 col-sm-3 col-xs-3" for="add_endTime">备注 *</label>
                        <div class="col-md-4 col-sm-4 col-xs-4">
                            <input type="text" id="add_description" class="form-control input-sm">
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-md-6 col-sm-6 col-xs-6" style="text-align:right">
                            <button data-dismiss="modal" class="btn btn-sm btn-primary">取消</button>
                        </div>
                        <div class="col-md-3 col-sm-3 col-xs-3s">
                            <button onclick="addAuthorization(event)" class="btn btn-sm btn-primary">确定</button>
                        </div>
                    </div>
                </form>
            </div>
            <!-- ./panel -->
        </div>
        <!-- ./addclokermodal -->
        <div id="editauthorizationmodal" class="modal fade page-wrapper"
             style="padding-right: 9%;padding-top: 5%;padding-left: 3%;">
            <div class="panel panel-default">
                <form id="editauthorizationform" class="form-horizontal form-label-left panel-body">
                    <div class="form-group">
                        <label class="control-label col-md-4 col-sm-3 col-xs-3" for="edit_lockerdescription">固件名称
                            *</label>
                        <div class="col-md-4 col-sm-4 col-xs-4">
                            <input type="text" id="edit_lockerdescription" class="form-control input-sm" readonly>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-4 col-sm-3 col-xs-3" for="edit_toAccount">授权给 *</label>
                        <div class="col-md-4 col-sm-4 col-xs-4">
                            <input type="text" id="edit_toAccount" class="form-control input-sm" readonly>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-4 col-sm-3 col-xs-3" for="edit_startTime">开始时间 *</label>
                        <div class="col-md-4 col-sm-4 col-xs-4">
                            <input type="text" id="edit_startTime" class="form-control input-sm">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-4 col-sm-3 col-xs-3" for="edit_endTime">结束时间 *</label>
                        <div class="col-md-4 col-sm-4 col-xs-4">
                            <input type="text" id="edit_endTime" class="form-control input-sm">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-4 col-sm-3 col-xs-3" for="edit_description">备注 *</label>
                        <div class="col-md-4 col-sm-4 col-xs-4">
                            <input type="text" id="edit_description" class="form-control input-sm">
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-md-6 col-sm-6 col-xs-6" style="text-align:right">
                            <button data-dismiss="modal" class="btn btn-sm btn-primary">取消</button>
                        </div>
                        <div class="col-md-3 col-sm-3 col-xs-3s">
                            <button onclick="editAuthorization(event)" class="btn btn-sm btn-primary">确定</button>
                        </div>
                    </div>
                </form>
            </div>
            <!-- ./panel -->
        </div>
        <!--./editauthorizationmodal-->
    </div>
</div>


<!-- Login controller Javascript -->
<script src="dist/js/authorization.js"></script>
</body>
</html>
