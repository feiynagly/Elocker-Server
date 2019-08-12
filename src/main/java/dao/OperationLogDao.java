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
public class OperationLogDao {

    private final static String GET_LOGS_BY_SERIAL = "select a.id,a.serial,b.description as lockerDescription, " +
            "a.phoneNum,a.operation,a.sTime,a.description from t_log as a inner join t_locker as b " +
            "on a.serial=b.serial=? where a.serial = ? and a.phoneNum=? and (a.sTime between ? and ?)";
    private final static String GET_LOGS_ALL = "select a.id,a.serial,b.description as lockerDescription, " +
            "a.phoneNum,a.operation,a.sTime,a.description from t_log as a inner join t_locker as b " +
            " on a.serial=b.serial where a.phoneNum=? and (sTime between ? and ?)";
    private final static String ADD_OPERATION_LOG = "insert into t_log(serial,phoneNum,operation,sTime,description) " +
            " values (?,?,?,?,?)";

    private final static String DEL_LOG_BY_SERIAL = "delete from t_log where serial = ?";
    private static Logger logger = Logger.getLogger(OperationLogDao.class);
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /*
     * @param phoneNum
     * @param serial
     * */
    public List<OperationLogViewData> getLogsBySerial(String phoneNum, String serial, String startTime, String endTime) {
        List<OperationLogViewData> operationLogs = new ArrayList<OperationLogViewData>();
        try {
            jdbcTemplate.query(GET_LOGS_BY_SERIAL, new Object[]{serial, phoneNum, startTime, endTime},
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

    public List<OperationLogViewData> getLogs(String phoneNum, String startTime, String endTime) {
        List<OperationLogViewData> operationLogs = new ArrayList<OperationLogViewData>();
        try {

            jdbcTemplate.query(GET_LOGS_ALL, new Object[]{phoneNum, startTime, endTime}, new RowCallbackHandler() {
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
