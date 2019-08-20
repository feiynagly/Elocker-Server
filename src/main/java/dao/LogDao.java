package dao;

import constant.Operation;
import model.OperationLog;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import pojo.OperationLogViewData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@Repository
public class LogDao {
    private final static String ADD_OPERATION_LOG = "insert into t_log(serial,phoneNum,operation,sTime,description) " +
            " values (?,?,?,?,?)";

    private final static String DEL_LOG_BY_SERIAL = "delete from t_log where serial = ?";
    private static Logger logger = Logger.getLogger(LogDao.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /*
     * @param phoneNum  手机号
     * @param serial 序列号
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param page 页面
     * @param pageSize 每页数量
     * @return java.util.List<pojo.OperationLogViewData>
     */
    public List<OperationLogViewData> getLogsBySerial(String phoneNum, String serial, String startTime,
                                                      String endTime, int page, int pageSize) {
        List<OperationLogViewData> operationLogs = new ArrayList<OperationLogViewData>();
        String sql = "select a.id,a.serial,b.description as lockerDescription, " +
                "a.phoneNum,a.operation,a.sTime,a.description from t_log as a inner join t_locker as b " +
                "on a.serial=? and b.serial=? where a.phoneNum=? and (a.sTime between ? and ?) " +
                " limit ?,?";
        try {
            jdbcTemplate.query(sql, new Object[]{serial, serial, phoneNum,
                            startTime, endTime, page * pageSize, pageSize},
                    new RowCallbackHandler() {
                        @Override
                        public void processRow(ResultSet rs) throws SQLException {
                            OperationLogViewData operationLog = new OperationLogViewData();
                            operationLog.setId(rs.getLong("id"));
                            operationLog.setSerial(rs.getString("serial"));
                            operationLog.setLockerDescription(rs.getString("lockerDescription"));
                            operationLog.setPhoneNum(phoneNum);
                            operationLog.setsTime(rs.getString("sTime"));
                            operationLog.setDescription(rs.getString("description"));
                            operationLog.setOperation(Operation.from(rs.getString("operation")));
                            operationLogs.add(operationLog);
                        }
                    });
        } catch (Exception e) {
            logger.error("Failed to get operation logs, SQL error, phoneNum: " + phoneNum);
            return null;
        }
        return operationLogs;
    }

    /*
     * @param phoneNum
     * @param startTime
     * @param endTime
     * @param page   页数（第一页为page = 0)
     * @param pageSize
     * @return java.util.List<pojo.OperationLogViewData>
     */
    public List<OperationLogViewData> getLogs(String phoneNum, String startTime,
                                              String endTime, int page, int pageSize) {
        String sql = "select a.id,a.serial,b.description as lockerDescription, " +
                "a.phoneNum,a.operation,a.sTime,a.description from t_log as a left join t_locker as b " +
                " on a.serial=b.serial where a.phoneNum=? and (sTime between ? and ?) " +
                " limit ?,?";
        List<OperationLogViewData> operationLogs = new ArrayList<>();
        try {
            jdbcTemplate.query(sql, new Object[]{phoneNum, startTime, endTime
                    , page * pageSize, pageSize}, new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    OperationLogViewData operationLog = new OperationLogViewData();
                    operationLog.setId(rs.getLong("id"));
                    operationLog.setSerial(rs.getString("serial"));
                    operationLog.setLockerDescription(rs.getString("lockerDescription"));
                    operationLog.setPhoneNum(phoneNum);
                    operationLog.setsTime(rs.getString("sTime"));
                    operationLog.setDescription(rs.getString("description"));
                    operationLog.setOperation(Operation.from(rs.getString("operation")));
                    operationLogs.add(operationLog);
                }
            });
        } catch (Exception e) {
            logger.error("Failed to get operation logs, SQL error, phoneNum: " + phoneNum);
            return null;
        }
        return operationLogs;
    }

    public int addOperationLog(OperationLog operationLog) {
        int status = -1;
        try {
            status = jdbcTemplate.update(ADD_OPERATION_LOG, new Object[]{
                    operationLog.getSerial(),
                    operationLog.getPhoneNum(),
                    operationLog.getOperation().toString(),
                    operationLog.getsTime(),
                    operationLog.getDescription()
            });
        } catch (Exception e) {
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("failed to add a new operation log into database for "
                    + operationLog.getPhoneNum() + " ,SQL error");
        }
        return status;
    }

    public int delOperationLogBySerial(String serial) {
        int status = -1;
        if (serial != null) {
            try {
                status = jdbcTemplate.update(DEL_LOG_BY_SERIAL, new Object[]{serial});
            } catch (Exception e) {
                logger.error("failed to delete logs , serial: " + serial + " SQL error");
            }
        }
        return status;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
