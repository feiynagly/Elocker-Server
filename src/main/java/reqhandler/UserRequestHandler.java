package reqhandler;

import constant.Operation;
import dao.OperationLogDao;
import dao.UserDao;
import model.OperationLog;
import model.User;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import util.RedisUtil;

import static constant.Status.*;


@Service("UserRequestHandler")
public class UserRequestHandler extends RequestHandler {

    private static Logger logger = Logger.getLogger(UserRequestHandler.class);
    @Autowired
    private UserDao userDao;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OperationLogDao operationLogDao;

    public void changePassword() {
        Jedis jedis = redisUtil.getJedis();
        if (this.phoneNum == null) {
            this.responseData.put("error", "Internal Error");
            this.responseData.put("status", UNKNOWN_ERROR);
            return;
        }

        String oldpassword = this.postData.getString("oldpass");
        String newpassword = this.postData.getString("newpass");
        if (userDao.getPassword(this.phoneNum).equals(oldpassword)) {
            int status = userDao.updatePasword(this.phoneNum, newpassword);
            if (status == 1) {
                /*密码修改成功后强制重新登录*/
                jedis.del(this.token);
                redisUtil.returnResource(jedis);
                OperationLog log = new OperationLog();
                log.setDescription(this.phoneNum + " Change Password");
                log.setPhoneNum(this.phoneNum);
                log.setOperation(Operation.Change_Password);
                log.setSerial(this.phoneNum);
                operationLogDao.addOperationLog(log);
                this.responseData.put("success", "update password successfully");
                this.responseData.put("status", SUCCESS);
            } else {
                this.responseData.put("error", "Internal error");
                this.responseData.put("status", UNKNOWN_ERROR);
            }
        } else {
            this.responseData.put("error", "old password is wrong");
            this.responseData.put("status", INCORRECT_USERNAME_OR_PASSWORD);
            logger.info("Failed to update password for " + this.phoneNum + " ,old password is wrong");
        }
    }

    public void get() {
        if (this.phoneNum != null) {
            User user = userDao.getUserByPhoneNum(this.phoneNum);
            if (user != null) {
                this.responseData.put("user", user);
                this.responseData.put("status", SUCCESS);
            } else {
                this.responseData.put("error", "get user info error");
                this.responseData.put("status", UNKNOWN_ERROR);
            }
        }
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

    public void setOperationLogDao(OperationLogDao operationLogDao) {
        this.operationLogDao = operationLogDao;
    }
}
