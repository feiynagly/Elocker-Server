package reqhandler;

import dao.UserDao;
import org.apache.commons.codec.digest.DigestUtils;
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

    public void changePassword() {
        Jedis jedis = redisUtil.getJedis();
        if (this.phoneNum == null) {
            this.responseData.put("error", "Internal Error");
            return;
        }

        String oldpassword = DigestUtils.md5Hex(this.phoneNum + this.postData.getString("oldpass"));
        String newpassword = DigestUtils.md5Hex(this.phoneNum + this.postData.getString("newpass"));
        if (userDao.getPassword(this.phoneNum).equals(oldpassword)) {
            int status = userDao.updatePasword(this.phoneNum, newpassword);
            if (status == 1) {
                /*密码修改成功后强制重新登录*/
                jedis.del(this.token);
                redisUtil.returnResource(jedis);
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

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }
}
