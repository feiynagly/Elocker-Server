package dao;

import model.Authorization;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import pojo.AuthorizationViewData;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class AuthorizationDao {
    private final String GET_AUTHORIZATION_LIST = "select * from t_authorization where fromAccount = ? or toAccount = ?";
    private final String ADD_AUTHORIZATION = "insert into t_authorization(serial,fromAccount,toAccount," +
            "startTime,endTime,description,weekday,dailyStartTime,dailyEndTime) value(?,?,?,?,?,?,?,?,?)";
    private final String DEL_AUTHORIZATION = "delete from t_authorization where id=?";
    private final String GET_AUTHORIZATION_ALL = "select a.id,a.serial,b.description as lockerDescription,a.fromAccount,a.toAccount,a.startTime," +
            "a.endTime,a.description ,a.weekday,a.dailyStartTime,a.dailyEndTime from t_authorization a,t_locker b " +
            " where a.serial = b.serial and (fromAccount = ? or toAccount = ?)";
    private final String GET_AUTHORIZATION_BY_TOACCOUNT = "select a.id,a.serial,b.description as lockerDescription,a.fromAccount,a.toAccount,a.startTime," +
            "a.endTime,a.description ,a.weekday,a.dailyStartTime,a.dailyEndTime from t_authorization a,t_locker b " +
            " where a.serial = b.serial and fromAccount = ? and toAccount = ?";
    private final String GET_AUTHORIZATION_BY_TOACCOUNT_AND_SERIAL = "select a.id,a.serial,b.description as lockerDescription,a.fromAccount,a.toAccount,a.startTime," +
            "a.endTime,a.description ,a.weekday,a.dailyStartTime,a.dailyEndTime from t_authorization a,t_locker b " +
            " where a.serial = ? and b.serial = ? and fromAccount = ? and toAccount = ?";
    private final String GET_AUTHORIZATION_BY_SERIAL = "select a.id,a.serial,b.description as lockerDescription,a.fromAccount,a.toAccount,a.startTime," +
            "a.endTime,a.description ,a.weekday,a.dailyStartTime,a.dailyEndTime from t_authorization a,t_locker b " +
            " where a.serial = ? and b.serial = ? and fromAccount = ?";
    private final String UPDATE_AUTHORIZATION = "update t_authorization set startTime=? ," +
            "endTime=?,description=? where id=?";
    private final String EXIST_AUTHORIZATION = "select count(*) from t_authorization where " +
            "serial = ? and fromAccount = ? and toAccount = ?";

    @Autowired
    private JdbcTemplate jdbcTemplate;
    private Logger logger = Logger.getLogger(AuthorizationDao.class);

    public List<Authorization> getAuthorization(String fromAccount, String toAccount, String serial) {
        String sql = null;
        Object[] params = null;
        if (fromAccount != null && toAccount != null && serial != null) {
            sql = GET_AUTHORIZATION_BY_TOACCOUNT_AND_SERIAL;
            params = new Object[]{serial, serial, fromAccount, toAccount};
        } else if (fromAccount != null && toAccount != null) {
            sql = GET_AUTHORIZATION_BY_TOACCOUNT;
            params = new Object[]{fromAccount, toAccount};
        } else if (fromAccount != null && serial != null) {
            /*获取当前所有授权*/
            sql = GET_AUTHORIZATION_BY_SERIAL;
            params = new Object[]{serial, serial, fromAccount};
        } else if (fromAccount != null) {
            /*获取当前所有授权*/
            sql = GET_AUTHORIZATION_ALL;
            params = new Object[]{fromAccount, fromAccount};
        }

        if (sql != null && params != null) {
            List<Authorization> authorizationList = new ArrayList<Authorization>();
            try {
                jdbcTemplate.query(sql, params, new RowCallbackHandler() {
                    @Override
                    public void processRow(ResultSet rs) throws SQLException {
                        AuthorizationViewData authorization = new AuthorizationViewData();
                        authorization.setId(rs.getLong("id"));
                        authorization.setSerial(rs.getString("serial"));
                        authorization.setLockerDescription(rs.getString("lockerDescription"));
                        authorization.setFromAccount(rs.getString("fromAccount"));
                        authorization.setToAccount(rs.getString("toAccount"));
                        authorization.setStartTime(rs.getString("startTime"));
                        authorization.setEndTime(rs.getString("endTime"));
                        authorization.setDescription(rs.getString("description"));
                        authorization.setWeekday(rs.getString("weekday"));
                        authorization.setDailyStartTime(rs.getString("dailyStartTime"));
                        authorization.setDailyEndTime(rs.getString("dailyEndTime"));
                        authorizationList.add(authorization);
                    }
                });
            } catch (Exception e) {
                logger.error("Failed to get authorization from database, SQL query error");
                return null;
            }
            return authorizationList;
        } else {
            return null;
        }
    }

    public int addAuthorization(Authorization authorization) {
        int status = -1;
        try {
            status = jdbcTemplate.update(ADD_AUTHORIZATION, new Object[]{
                    authorization.getSerial(),
                    authorization.getFromAccount(),
                    authorization.getToAccount(),
                    authorization.getStartTime(),
                    authorization.getEndTime(),
                    authorization.getDescription(),
                    authorization.getWeekday(),
                    authorization.getDailyEndTime(),
                    authorization.getDailyEndTime()
            });
        } catch (Exception e) {
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Failed to add authorization item to database, SQL error");
        }
        return status;
    }

    public int updateAuthorization(Authorization authorization) {
        int status = -1;
        try {
            status = jdbcTemplate.update(UPDATE_AUTHORIZATION, new Object[]{
                    authorization.getStartTime(),
                    authorization.getEndTime(),
                    authorization.getDescription(),
                    authorization.getId()
            });
        } catch (Exception e) {
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Failed to update authorization,SQL query error,authorization: " + authorization.toString());
        }
        return status;
    }

    public int delAuthorizationById(Long id) {
        int status = -1;
        try {
            status = jdbcTemplate.update(DEL_AUTHORIZATION, new Object[]{id});
        } catch (Exception e) {
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Failed to delete authorization ,SQL error, id: " + id);
        }
        return status;
    }

    public boolean existAuthorization(String serial, String fromAccount, String toAccount) {
        int count = 0;
        try {
            count = jdbcTemplate.queryForObject(EXIST_AUTHORIZATION, new Object[]{
                    serial, fromAccount, toAccount}, Integer.class);
        } catch (Exception e) {
            logger.error("Failed to count authorization ,SQL error, phoneNum: " + fromAccount);
        }
        return count > 0;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
