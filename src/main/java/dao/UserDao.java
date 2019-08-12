package dao;

import model.User;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;

@Repository
public class UserDao {

    private static Logger logger = Logger.getLogger(UserDao.class);
    private final String GET_PASSWORD_SQL = "select password from t_user where phoneNum=?";
    private final String DEL_USER_BY_PHONE_SQL = "delete from t_user where phoneNum=?";
    private final String ADD_USER_SQL = "insert into t_user (phoneNum,userName,password,createTime," +
            "privilegeLevel,email) values (?,?,?,?,?,?)";
    private final String UPDATE_PASSWORD_SQL = "update t_user set password = ? where phoneNum = ?";
    private final String UPDATE_USERINFO_SQL = "update t_user set userName = ? , email = ? "
            + "where phoneNum = ?";
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * @param phoneNum
     * @return User 返回User对象(不包含密码和apiKey)，如果SQL查询失败，则返回Null
     */
    public User getUser(String phoneNum) {
        String sql = "select * from t_user where phoneNum = ?";
        final User user = new User();
        try {
            jdbcTemplate.query(sql, new Object[]{phoneNum}, new RowCallbackHandler() {

                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    user.setPhoneNum(rs.getString("phoneNum"));
                    user.setUserName(rs.getString("userName"));
                    user.setCreateTime(rs.getString("createTime"));
                    user.setPrivilegeLevel(rs.getShort("privilegeLevel"));
                    user.setEmail(rs.getString("email"));
                    user.setLastLoginTime(rs.getString("lastLoginTime"));
                    user.setAppVersion(rs.getString("appVersion"));
                    user.setUserAgent(rs.getString("userAgent"));
                }
            });
        } catch (Exception e) {
            logger.error("Failed get username " + phoneNum + " from database,SQL query error");
            return null;
        }
        return user;
    }

    /*
     * @param appKey
     * @param phoneNum
     * @return int 插入成功返回1，插入失败返回-1
     */
    public int setAppKey(String appKey, String phoneNum) {
        int stauts = 1;
        String sql = "update t_user set appKey = ? where phoneNum = ?";
        try {
            stauts = jdbcTemplate.update(sql, new Object[]{appKey, phoneNum});
        } catch (Exception e) {
            logger.error("Set app key failed , SQL update error");
            stauts = -1;
        }
        return stauts;
    }

    /*  更新lastLoginTime,appVersion,userAgent
     * @param user 需包含phoneNum,lastLoginTime,appVersion,userAgent四个参数
     * @return int
     */
    public int updateLoginInfo(User user) {
        int status;
        String sql = "update t_user (lastLoginTime,lastLoginIp,appVersion,userAgent) values " +
                " (?,?.?,?) where phoneNum = ? ";
        try {
            status = jdbcTemplate.update(sql, new Object[]{
                    user.getLastLoginTime(),
                    user.getLastLoginIp(),
                    user.getAppVersion(),
                    user.getUserAgent(),
                    user.getPhoneNum()
            });
        } catch (Exception e) {
            status = -1;
            logger.error("Failed to update login info , SQL error");
        }
        return status;
    }

    /**
     * @param phoneNum
     * @return 返回用户密码
     */
    public String getPassword(String phoneNum) {
        String password = null;
        if (phoneNum != null) {
            try {
                password = jdbcTemplate.queryForObject(GET_PASSWORD_SQL, new Object[]{phoneNum}, String.class);
            } catch (Exception e) {
                logger.error("failed to get password of " + phoneNum + " from database,SQL query error");
            }
        }
        return password;
    }

    /**
     * @param phoneNum
     * @return 操作成功返回1，否则返回-1
     */
    public int delUserByPhoneNum(String phoneNum) {
        int status = 0;
        try {
            status = jdbcTemplate.update(DEL_USER_BY_PHONE_SQL, new Object[]{phoneNum});
        } catch (Exception e) {
            logger.error("failed delete " + phoneNum + " from database，SQL update error");
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            status = -1;
        }
        return status;
    }

    /**
     * @param user
     * @return 操作成功返回1，否则返回-1
     */
    public int addUser(User user) {
        int status = 0;
        try {
            status = jdbcTemplate.update(ADD_USER_SQL, new Object[]{
                    user.getPhoneNum(),
                    user.getUserName(),
                    user.getPassword(),
                    user.getCreateTime(),
                    user.getPrivilegeLevel(),
                    user.getEmail()
            });
        } catch (Exception e) {
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("Failed insert user into database, SQL update error");
            status = -1;
        }
        return status;
    }

    /**
     * @param phoneNum
     * @param password
     * @return 操作成功返回1，否则返回-1
     */
    public int updatePasword(String phoneNum, String password) {
        int status = -1;
        try {
            status = jdbcTemplate.update(UPDATE_PASSWORD_SQL, new Object[]{
                    password, phoneNum});
        } catch (Exception e) {
            logger.error("failed update password for " + phoneNum);
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return status;
    }

    /**
     * 更新 email userName
     *
     * @param user
     * @return 操作成功返回1，否则返回-1
     */
    public int updateUserInfo(User user) {
        int status = 0;
        try {
            status = jdbcTemplate.update(UPDATE_USERINFO_SQL, new Object[]{
                    user.getUserName(),
                    user.getEmail(),
                    user.getPhoneNum()
            });
        } catch (Exception e) {
            if (user != null)
                logger.error("failed update user info for " + user.getPhoneNum() + " into database");
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            status = -1;
        }
        return status;
    }

    public boolean existUser(String phoneNum) {
        int i = 0;
        String sql = "select count(*) from t_user where phoneNum = ? ";
        if (phoneNum != null) {
            try {
                i = jdbcTemplate.queryForObject(sql, new Object[]{phoneNum}, Integer.class);
            } catch (Exception e) {
                logger.error("Failed to query user, SQL error");
            }
        }
        return i > 0;
    }

    /*
     * @param oldPhoneNum 新手机号
     * @param newPhoneNum 老手机号
     * @return int  修改成功返回1，修改失败返回-1
     */
    public int updatePhoneNum(String oldPhoneNum, String newPhoneNum) {
        int status = 1;
        String[] sqls = {
                "update t_user set phoneNum = ? where phoneNum = ?",
                "update t_authorization set fromAccount = ? where fromAccount = ?",
                "update t_authorization set toAccount = ? where toAccount = ?",
                "update t_locker set phoneNum = ? where phoneNum = ?",
                "update t_log set phoneNum = ? where phoneNum = ?",
                "update t_client set phoneNum = ? where phoneNum = ?"
        };

        try {
            for (String sql : sqls) {
                jdbcTemplate.update(sql, new Object[]{newPhoneNum, oldPhoneNum});
            }
        } catch (Exception e) {
            status = -1;
            logger.error("Update phone number failed, SQL query error");
        }
        return status;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

}
