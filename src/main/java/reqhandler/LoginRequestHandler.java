package reqhandler;

import dao.UserDao;
import model.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import util.RedisUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static constant.Constant.DATE_PATTERN;
import static constant.Status.*;

@Service("LoginRequestHandler")
public class LoginRequestHandler extends RequestHandler {

    private static Logger logger = Logger.getLogger(LoginRequestHandler.class);

    @Value("${core.api.token_timeout}")
    private int tokenTimeout;

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
        String password = this.postData.has("password") ? this.postData.getString("password") : null;

        if (phoneNum == null || password == null) {
            this.responseData.put("message", "Username or password can not be empty");
            this.responseData.put("status", INCORRECT_USERNAME_OR_PASSWORD);
            return;
        }
        Jedis jedis = redisUtil.getJedis();
        /*记录重新登录次数*/
        jedis.set(phoneNum, String.valueOf(maxRetryTime), "NX", "EX", waitTime);

        /*多次输入密码失败时直接返回*/
        if (Integer.parseInt(jedis.get(phoneNum)) <= 0) {
            logger.info(phoneNum + " try login in for " + maxRetryTime + " times but failed");
            this.responseData.put("message", "Login too frequently ,please login in after "
                    + jedis.ttl(phoneNum) + " seconds");
            this.responseData.put("status", AUTHENTICATION_ERROR_COUNTER_EXCEED);
            jedis.close();
            return;
        }

        String enc_password = userDao.getPassword(phoneNum);
        if (password.equals(enc_password)) {
            /*将计算token的除URL字段以外的相关值缓存在redis数据库，用于后续请求*/
            String apiKey = UUID.randomUUID().toString().replaceAll("-", "");
            String partToken = DigestUtils.md5Hex(enc_password + apiKey);
            jedis.setex(phoneNum + "token", tokenTimeout, partToken);

            if (userDao.setApiKey(phoneNum, apiKey) != 1) {
                this.responseData.put("status", UNKNOWN_ERROR);
                this.responseData.put("message", "Failed to insert apiKey into database");
                redisUtil.returnResource(jedis);
                return;
            }

            /*更新登录信息*/
            String lastLoginTime = new SimpleDateFormat(DATE_PATTERN).format(new Date());
            User user = new User();
            user.setPhoneNum(phoneNum);
            user.setLastLoginTime(lastLoginTime);
            user.setLastLoginIp(this.request.getRemoteAddr());
            user.setUserAgent(this.request.getHeader("User-Agent"));
            user.setAppVersion(this.request.getHeader("App-Version"));
            userDao.updateLoginInfo(user);

            this.responseData.put("redirectUrl", "mainview");
            this.responseData.put("message", "Login in success");
            this.responseData.put("status", SUCCESS);
            this.responseData.put("apiKey", apiKey);
            logger.info("User  " + phoneNum + "  login in , IP : "
                    + this.request.getRemoteAddr());

            /*数据库读取失败*/
        } else if (enc_password == null) {
            this.responseData.put("message", "Internal Error");
            this.responseData.put("status", UNKNOWN_ERROR);
        } else {
            jedis.decr(phoneNum);
            this.responseData.put("message", "Username or password is wrong");
            this.responseData.put("status", INCORRECT_USERNAME_OR_PASSWORD);
        }
        redisUtil.returnResource(jedis);
    }

    public void logout() {
        Jedis jedis = redisUtil.getJedis();
        if (jedis.exists(this.token)) {
            logger.info("User: " + jedis.get(this.token) + " logout");
            jedis.del(this.token);
        }
        jedis.close();
        this.responseData.put("redirectUrl", "loginview");
        this.responseData.put("message", this.phoneNum + " log out");
        this.responseData.put("status", SUCCESS);
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setRedisUtil(RedisUtil redisUtil) {
        this.redisUtil = redisUtil;
    }

}
