package reqhandler;

import dao.UserDao;
import model.User;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import util.RedisUtil;

import java.text.SimpleDateFormat;
import java.util.Date;

import static constant.Constant.DATE_PATTERN;
import static constant.Status.*;

/*
 * Redis中数据类型{UUID:phonenum}
 * */
@Service("LoginRequestHandler")
public class LoginRequestHandler extends RequestHandler {

    private static Logger logger = Logger.getLogger(LoginRequestHandler.class);

    @Value("${core.http.max_idle_time}")
    private int maxIdleTime;

    @Value("${core.http.max_retry_time}")
    private int maxRetryTime;

    @Value("${core.http.wait_time}")
    private int waitTime;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RedisUtil redisUtil;

    public void login() {
        String phoneNum = this.postData.has("phoneNum") ? this.postData.getString("phoneNum") : null;
        String post_passwd = this.postData.has("password") ? this.postData.getString("password") : null;
        String token = this.cookieData.get("token");

        if (phoneNum == null || post_passwd == null) {
            this.responseData.put("error", "Username or password can not be empty");
            this.responseData.put("status", INCORRECT_USERNAME_OR_PASSWORD);
            return;
        }
        Jedis jedis = redisUtil.getJedis();

        /*记录重新登录次数*/
        jedis.set(phoneNum, String.valueOf(maxRetryTime), "NX", "EX", waitTime);

        /*多次输入密码失败时直接返回*/
        if (Integer.parseInt(jedis.get(phoneNum)) <= 0) {
            logger.info(phoneNum + " try login in for " + maxRetryTime + " times but failed");
            this.responseData.put("error", "Login too frequently ,please login in after " + jedis.ttl(phoneNum) + " seconds");
            this.responseData.put("status", AUTHENTICATION_ERROR_COUNTER_EXCEED);
            jedis.close();
            return;
        }

        String password = userDao.getPassword(phoneNum);
        if (password != null && password.equals(post_passwd)) {
            jedis.set(token, phoneNum, "NX", "EX", maxIdleTime);

            /*更新登录信息*/
            String lastLoginIp = this.request.getRemoteAddr();
            String lastLoginTime = new SimpleDateFormat(DATE_PATTERN).format(new Date());
            User user = new User();
            user.setPhoneNum(phoneNum);
            user.setLastLoginIp(lastLoginIp);
            user.setLastLoginTime(lastLoginTime);
            userDao.updateLoginInfo(user);

            this.responseData.put("redirectUrl", "mainview");
            this.responseData.put("success", "login in success");
            this.responseData.put("status", SUCCESS);
            logger.info("User  " + phoneNum + "  login in successfully");

            /*数据库读取失败*/
        } else if (password == null) {
            this.responseData.put("error", "Internal Error");
            this.responseData.put("status", UNKNOWN_ERROR);
        } else {
            jedis.decr(phoneNum);
            this.responseData.put("error", "Username or password is wrong");
            this.responseData.put("status", INCORRECT_USERNAME_OR_PASSWORD);
        }
        jedis.close();

    }

    public void logout() {
        String token = this.cookieData.get("token");
        Jedis jedis = redisUtil.getJedis();
        if (jedis.exists(token)) {
            logger.info("User: " + jedis.get(token) + " logout");
            jedis.del(token);
        }
        jedis.close();
        this.responseData.put("redirectUrl", "loginview");
        this.responseData.put("success", "success log out");
        this.responseData.put("status", SUCCESS);
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }
}
