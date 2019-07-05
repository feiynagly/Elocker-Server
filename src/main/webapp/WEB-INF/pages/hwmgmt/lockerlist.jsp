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
            <h3 class="page-header">固件列表</h3>
        </div>
        <!-- /.col-lg-12 -->
    </div>
    <!-- /.row -->
    <div class="row">
        <div class="panel panel-default">
            <div class="panel-body">
                <!--     <div class="panel-heading" style="text-align:right">

                     </div>-->
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
                        <button id="addlocker" class="btn btn-primary btn-md" onclick="openAddLockerPage()"><i
                                class="fa fa-plus"></i> 新增
                        </button>
                        <button id="editlocker" class="btn btn-primary btn-md" onclick="openEditLockerPage()"><i
                                class="fa fa-edit"></i> 编辑
                        </button>
                        <button id="delLocker" class="btn btn-primary btn-md" onclick="delLocker()"><i
                                class="fa fa-trash-o"></i> 删除
                        </button>
                        <button id="refresh" class="btn btn-primary btn-md" onclick="updateLockerList()"><i
                                class="fa fa-refresh"></i> 刷新
                        </button>
                    </div>
                    <table id="lockerList" style="width:100%"
                           class="table table-striped table-bordered table-hover cell-border">
                        <thead>
                        <tr>
                            <th style="text-align:center" width="6%">
                                <input id="checkAll" name="checkAll" type="checkbox">
                            </th>
                            <th style="text-align:center" width="7%"><strong>序号</strong></th>
                            <th style="text-align:center" width="16%"><strong>序列号</strong></th>
                            <th style="text-align:center" width="20%"><strong>固件名称</strong></th>
                            <th style="text-align:center" width="14%"><strong>类型</strong></th>
                            <th style="text-align: center" width="18%"><strong>所属用户</strong></th>
                            <th style="text-align:center" width="25%"><strong>添加时间</strong></th>
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
        <div id="addlockermodal" class="modal fade page-wrapper"
             style="padding-right: 9%;padding-top: 2%;padding-left: 3%">
            <div class="panel panel-default">
                <form id="addlockerform" class="form-horizontal form-label-left panel-body">
                    <div class="form-group">
                        <label class="control-label col-md-4 col-sm-3 col-xs-3" for="add_lockerDescription">锁名称
                            *</label>
                        <div class="col-md-4 col-sm-4 col-xs-4">
                            <input type="text" id="add_lockerDescription" class="form-control input-sm">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-4 col-sm-3 col-xs-3" for="add_lockerSerial">序列号 *</label>
                        <div class="col-md-4 col-sm-4 col-xs-4">
                            <input type="text" id="add_lockerSerial" class="form-control input-sm">
                        </div>
                    </div>

                    <div class="form-group">
                        <div class="col-md-6 col-sm-6 col-xs-6" style="text-align:right">
                            <button data-dismiss="modal" class="btn btn-sm btn-primary">取消</button>
                        </div>
                        <div class="col-md-3 col-sm-3 col-xs-3s">
                            <button onclick="addLocker(event)" class="btn btn-sm btn-primary">确定</button>
                        </div>
                    </div>
                </form>
            </div>
            <!-- ./panel -->
        </div>
        <!-- ./addclokermodal -->
        <div id="editlockermodal" class="modal fade page-wrapper"
             style="padding-right: 9%;padding-top: 5%;padding-left: 3%;">
            <div class="panel panel-default">
                <form id="editlockerform" class="form-horizontal form-label-left panel-body">
                    <div class="form-group">
                        <label class="control-label col-md-4 col-sm-3 col-xs-3" for="edit_description">固件名称 *</label>
                        <div class="col-md-4 col-sm-4 col-xs-4">
                            <input type="text" id="edit_description" class="form-control input-sm">
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="control-label col-md-4 col-sm-3 col-xs-3" for="edit_serial">序列号 *</label>
                        <div class="col-md-4 col-sm-4 col-xs-4">
                            <input type="text" id="edit_serial" class="form-control input-sm" readonly>
                        </div>
                    </div>
                    <div class="form-group">
                        <div class="col-md-6 col-sm-6 col-xs-6" style="text-align:right">
                            <button data-dismiss="modal" class="btn btn-sm btn-primary">取消</button>
                        </div>
                        <div class="col-md-3 col-sm-3 col-xs-3s">
                            <button onclick="updateLocker(event)" class="btn btn-sm btn-primary">确定</button>
                        </div>
                    </div>
                </form>
            </div>
            <!-- ./panel -->
        </div>
        <!--./editlockermodal-->
    </div>
</div>


<!-- Login controller Javascript -->
<script src="dist/js/locker.js"></script>
</body>
</html>
