package dao;

import model.Locker;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class LockerDao {
    private static Logger logger = Logger.getLogger(LockerDao.class);
    private final String GET_ALL_LOCKER_LIST = "select * from t_locker where phoneNum = ?";
    private final String GET_LOCKER_LIST_BY_SERIAL = "select * from t_locker where phoneNum = ? and serial = ?";
    private final String COUNT_LOCKER_BY_SERIAL = "count(*) from t_locker where serial=?";
    private final String ADD_LOCKER = "insert into t_locker(serial,phoneNum,description," +
            "createTime,lastOpenTime,hwType) values(?,?,?,?,?,?)";
    private final String DEL_LOCKER = "delete from t_locker where phoneNum=? and serial=?";
    private final String DEL_AUTHORIZATION = "delete from t_authorization where " +
            "fromAccount = ? and serial = ?";
    private final String MODIFY_LOCKER_DESCRIPTION = "update t_locker set description=? where " +
            "phoneNum=? and serial=?";
    private final String UPDATE_LOCKER_LAST_OPEN_TIME = "update t_locker set lastOpenTime = ? , toggleTimes = " +
            " toggleTimes + 1 where phoneNum=? and serial=?";
    private final String DELETE_LOG_BY_SERIAL = "delete from t_log where serial = ?";
    private final static String TRANSFER_LOCKER = "update t_locker set phoneNum = ? where phoneNum = ? and serial = ?";
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /* 如果serial 为null或者""，获取该用户下的所有locker，否则获取指定的locker
     * @param phoneNum
     * @param serial 可以为null
     * @return java.util.List<model.Locker>
     */
    public List<Locker> getLockerListByPhoneNum(String phoneNum, String serial) {
        final List<Locker> lockerList = new ArrayList<Locker>();
        String sql;
        String[] params;
        if (serial == null || serial.equals("")) {
            sql = GET_ALL_LOCKER_LIST;
            params = new String[]{phoneNum};
        } else {
            sql = GET_LOCKER_LIST_BY_SERIAL;
            params = new String[]{phoneNum, serial};
        }
        try {
            jdbcTemplate.query(sql, params, new RowCallbackHandler() {
                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    Locker locker = new Locker();
                    locker.setSerial(rs.getString("serial"));
                    locker.setPhoneNum(phoneNum);
                    locker.setDescription(rs.getString("description"));
                    locker.setCreateTime(rs.getString("createTime"));
                    locker.setLastOpenTime(rs.getString("lastOpenTime"));
                    locker.setHwType(rs.getString("hwType"));
                    locker.setToggleTimes(rs.getInt("toggleTimes"));
                    lockerList.add(locker);
                }
            });
        } catch (Exception e) {
            logger.error("Failed to get all locker list for " + phoneNum + " from Database ,SQL query error");
            return null;
        }

        return lockerList;
    }

    public boolean existSerial(String serial) {
        int count = 0;
        try {
            count = jdbcTemplate.queryForObject(COUNT_LOCKER_BY_SERIAL, new Object[]{serial}, Integer.class);
        } catch (Exception e) {
            logger.error("Failed to query locker from database, SQL error");
        }
        return count > 0;
    }

    /*新增*/
    public int addLocker(Locker locker) {
        int status = 1;
        try {
            jdbcTemplate.update(ADD_LOCKER, new String[]{
                    locker.getSerial(),
                    locker.getPhoneNum(),
                    locker.getDescription(),
                    locker.getCreateTime(),
                    locker.getLastOpenTime(),
                    locker.getHwType()
            });
        } catch (Exception e) {
            logger.error("Failed to add new locker, SQL error");
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            status = -1;
        }
        return status;
    }

    /*修改固件名称*/
    public int updateLockerDescription(Locker locker) {
        int status = -1;
        try {
            status = jdbcTemplate.update(MODIFY_LOCKER_DESCRIPTION, new Object[]{
                    locker.getDescription(),
                    locker.getPhoneNum(),
                    locker.getSerial()
            });
        } catch (Exception e) {
            logger.error("Failed to modify locker description, serial: " + locker.getSerial() +
                    " phoneNum: " + locker.getPhoneNum());
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return status;
    }

    /*修改最后一次登录时间
     * @param locker 设置serial,phoneNum,lastOpenTime即可
     * @return int 更新成功返回 1，否则返回-1
     */
    public int updateLastLoginTime(Locker locker) {
        int status = -1;
        try {
            status = jdbcTemplate.update(UPDATE_LOCKER_LAST_OPEN_TIME, new Object[]{
                    locker.getLastOpenTime(),
                    locker.getPhoneNum(),
                    locker.getSerial()
            });
        } catch (Exception e) {
            logger.error("Failed to update locker last open time, serial: " + locker.getSerial() +
                    " ,phoneNum: " + locker.getPhoneNum());
            // TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return status;
    }

    /*删除*/
    public int delLocker(String phoneNum, String serial) {
        int status = -1;
        if (phoneNum != null && serial != null) {
            try {
                status = jdbcTemplate.update(DEL_LOCKER, new String[]{phoneNum, serial});
                /*删除关联授权*/
                status = status * jdbcTemplate.update(DEL_AUTHORIZATION, new Object[]{phoneNum, serial});
                /*删除关联日志*/
                status = status * jdbcTemplate.update(DELETE_LOG_BY_SERIAL, new String[]{serial});
            } catch (Exception e) {
                logger.error("Delete locker failed from database, phoneNum: " + phoneNum + " ,serial: " + serial);
                //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }
        }
        return status;
    }

    /*转移*/
    public int transferLocker(String fromAccount, String serial, String toAccount) {
        int status = -1;
        try {
            status = jdbcTemplate.update(TRANSFER_LOCKER, new String[]{toAccount, fromAccount, serial});
        } catch (Exception e) {
            logger.error("Transfer locker from " + fromAccount + " to " + toAccount + " failed, SQL error");
        }
        return status;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
