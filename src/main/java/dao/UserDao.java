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
    private final String GET_USER_BY_PHONE_SQL = "select * from t_user where phoneNum=?";
    private final String GET_PASSWORD_SQL = "select password from t_user where phoneNum=?";
    private final String DEL_USER_BY_PHONE_SQL = "delete from t_user where phoneNum=?";
    private final String ADD_USER_SQL = "insert into t_user (phoneNum,userName,password,creatTime," +
            "lastLoginTime,lastLoginIp,privilegeLevel,email) values (?,?,?,?,?,?,?,?)";
    private final String UPDATE_PASSWORD_SQL = "update t_user set password = ? where phoneNum = ?";
    private final String UPDATE_LOGININFO_SQL = "update t_user set lastLoginIp = ? , lastLoginTime = ? "
            + "where phoneNum = ?";
    private final String UPDATE_USERINFO_SQL = "update t_user set userName = ? , email = ? "
            + "where phoneNum = ?";
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * @param phoneNum
     * @return User 返回User对象(不包含密码)，如果SQL查询失败，则返回Null
     */
    public User getUserByPhoneNum(String phoneNum) {
        final User user = new User();
        try {
            jdbcTemplate.query(GET_USER_BY_PHONE_SQL, new Object[]{phoneNum}, new RowCallbackHandler() {

                @Override
                public void processRow(ResultSet rs) throws SQLException {
                    user.setUserName(rs.getString("userName"));
                    user.setCreatTime(rs.getString("createTime"));
                    user.setLastLoginTime(rs.getString("lastLoginTime"));
                    user.setLastLoginIp(rs.getString("lastLoginIp"));
                    user.setLastLoginTime(rs.getString("lastLoginTime"));
                    user.setPrivilegeLevel(rs.getShort("privilegeLevel"));
                    user.setEmail(rs.getString("email"));
                }
            });
        } catch (Exception e) {
            logger.error("Failed get username " + phoneNum + " from database,SQL query error");
            return null;
        }
        return user;
    }

    /**
     * @param phoneNum
     * @return 返回的user对象仅仅包含password字段，如果SQL查询失败，则返回Null
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
                    user.getCreatTime(),
                    user.getLastLoginTime(),
                    user.getLastLoginIp(),
                    user.getPrivilegeLevel(),
                    user.getEmail()
            });
        } catch (Exception e) {
            //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            logger.error("failed insert user into database, SQL update error");
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
     * @param user
     * @return 操作成功返回1，否则返回-1
     */
    public int updateLoginInfo(User user) {
        int status = 0;
        try {
            status = jdbcTemplate.update(UPDATE_LOGININFO_SQL, new Object[]{
                    user.getLastLoginIp(),
                    user.getLastLoginTime(),
                    user.getPhoneNum()
            });
        } catch (Exception e) {
            if (user != null)
                //TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                logger.error("failed update loginInfo for " + user.getPhoneNum() + " into database");
            status = -1;
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

    private static final String EXIST_USER = "select count(*) from t_user where phoneNum = ? ";

    public boolean existUser(String phoneNum) {
        int i = 0;
        if (phoneNum != null) {
            try {
                i = jdbcTemplate.queryForObject(EXIST_USER, new String[]{phoneNum}, Integer.class);
            } catch (Exception e) {
                logger.error("Failed to query user, SQL error");
            }
        }
        return i > 0;
    }

    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

}
