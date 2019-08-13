package reqhandler;

import constant.Operation;
import dao.OperationLogDao;
import dao.UserDao;
import model.OperationLog;
import model.User;
import net.sf.json.JSONObject;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import util.HttpsUtil;
import util.RedisUtil;

import java.util.HashMap;

import static constant.Status.*;


@Service("UserRequestHandler")
public class UserRequestHandler extends RequestHandler {

    private static Logger logger = Logger.getLogger(UserRequestHandler.class);

    /*发送验证用的 key*/
    @Value("${core.api.verification_code_key}")
    private String verificationCodeApiKey;

    /*验证码超时时间*/
    @Value("${core.api.verification_code_timeout}")
    private int verificationCodeTimeout;

    @Autowired
    private UserDao userDao;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private OperationLogDao operationLogDao;

    public void add() {
        String tel = this.postData.has("phoneNum") ? this.postData.getString("phoneNum").trim() : "";
        String passwd = this.postData.has("password") ? this.postData.getString("password").trim() : "";
        String email = this.postData.has("email") ? this.postData.getString("email").trim() : "";
        String userName = this.postData.has("userName") ? this.postData.getString("userName").trim() : "";
        /*验证码*/
        String code = this.postData.has("code") ? this.postData.getString("code").trim() : "";
        if (tel.equals("")) {
            this.responseData.put("status", INVALID_PHONE_NUMBER);
            this.responseData.put("message", "Phone number can not be empty");
            return;
        }

        User user = new User();
        user.setPhoneNum(tel);
        user.setPassword(passwd);
        user.setEmail(email);
        user.setUserName(userName);

        /*判断用户是否已经存在*/
        if (userDao.existUser(tel)) {
            this.responseData.put("status", USER_ALREADY_EXISTS);
            this.responseData.put("message", "User already exists, please login directly");
            return;

        }
        Jedis redis = redisUtil.getJedis();
        if (code.equals(redis.get(tel + "code"))) {
            int status = userDao.addUser(user);
            if (status == 1) {
                this.responseData.put("status", SUCCESS);
                this.responseData.put("message", "Add user successfully");
            } else {
                this.responseData.put("status", UNKNOWN_ERROR);
                this.responseData.put("message", "Failed to add user");
            }
        } else {
            this.responseData.put("status", INCORRECT_VERIFICATION_CODE);
            this.responseData.put("message", "Verification Code is wrong");
        }
        redisUtil.returnResource(redis);
    }

    /*申请验证码*/
    public void getCode() {
        String tel = this.postData.has("phoneNum") ? this.postData.getString("phoneNum").trim() : null;
        String code = RandomStringUtils.random(6, "1234567890");

        HashMap<String, String> headers = new HashMap<String, String>();
        String contentType = "application/x-www-form-urlencoded;charset=utf-8";
        headers.put("Accept", "application/json;charset=utf-8");
        headers.put("Content-Type", contentType);

        String text = "【刘炎01】您的验证码是" + code + "。如非本人操作，请忽略本短信 ";
        String params = "apikey=" + verificationCodeApiKey + "&text=" + text + "&mobile=" + tel;

        if (tel != null) {
            /*记录操作日志*/
            OperationLog log = new OperationLog();
            log.setPhoneNum(tel);
            log.setOperation(Operation.Get_Verification_Code);
            log.setSerial("");
            log.setDescription("IP: " + this.request.getRemoteHost());
            operationLogDao.addOperationLog(log);
            String url = "https://sms.yunpian.com/v2/sms/single_send.json";
            JSONObject res = HttpsUtil.post(url, params, headers, contentType);
            if (res.getInt("status") != -1) {
                JSONObject response = res.getJSONObject("response");
                /*如果发送成功，则记录该验证码*/
                if (response.getInt("code") == 0) {
                    Jedis redis = redisUtil.getJedis();
                    redis.setex(tel + "code", verificationCodeTimeout, code.trim());
                    redisUtil.returnResource(redis);
                    this.responseData.put("status", SUCCESS);
                } else {
                    this.responseData.put("status", SEND_MESSAGE_FAILED);
                }
                this.responseData.put("message", response.getString("msg"));
                this.responseData.put("detail", response.containsKey("detail") ? response.getString("detail") : "");
            } else {
                this.responseData.put("status", SHORT_MESSAGE_GATEWAY_UNREACHABLE);
                this.responseData.put("error", "Internal Error");
                logger.error("Can not reache short message gateway");
            }
        } else {
            this.responseData.put("status", INVALID_PHONE_NUMBER);
            this.responseData.put("error", "Phone number can not be empty");
        }
    }

    public void resetPassword() {
        String tel = this.postData.has("phoneNum") ? this.postData.getString("phoneNum").trim() : null;
        String passwd = this.postData.has("password") ? this.postData.getString("password").trim() : null;
        if (tel == null || passwd == null) {
            this.responseData.put("status", INVALID_PHONE_NUMBER);
            this.responseData.put("message", "Phone number or password can not be empty");
            return;
        }

        /*判断用户是否已经存在*/
        if (!userDao.existUser(tel)) {
            this.responseData.put("status", USER_NOT_EXISTS);
            this.responseData.put("message", "User not exist");
            return;
        }

        /*验证码*/
        String code = this.postData.has("code") ? this.postData.getString("code").trim() : "";
        Jedis redis = redisUtil.getJedis();
        if (code.equals(redis.get(tel + "code"))) {
            if (userDao.updatePasword(tel, passwd) == 1) {
                this.responseData.put("status", SUCCESS);
                this.responseData.put("message", "Reset password successfully");
                /*记录操作日志*/
                OperationLog log = new OperationLog();
                log.setPhoneNum(tel);
                log.setSerial("");
                log.setDescription("IP: " + request.getRemoteHost());
                log.setOperation(Operation.Reset_Password);
                operationLogDao.addOperationLog(log);
                logger.info(tel + "reset password");
            } else {
                this.responseData.put("status", UNKNOWN_ERROR);
                this.responseData.put("message", "Internal Error");
            }
        } else {
            this.responseData.put("status", INCORRECT_VERIFICATION_CODE);
            this.responseData.put("message", "Incorrect verification code");
        }
        redisUtil.returnResource(redis);
    }

    public void changePassword() {
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
                Jedis jedis = redisUtil.getJedis();
                /*密码修改成功后强制重新登录*/
                jedis.del(this.token);
                redisUtil.returnResource(jedis);
                OperationLog log = new OperationLog();
                log.setDescription(this.phoneNum + " Change Password");
                log.setPhoneNum(this.phoneNum);
                log.setOperation(Operation.Change_Password);
                log.setSerial(this.phoneNum);
                operationLogDao.addOperationLog(log);
                this.responseData.put("message", "update password successfully");
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

    public void changePhoneNum() {
        String newPhoneNum = this.postData.has("newPhoneNum") ? this.postData.getString("phoneNum") : "";
        String code = this.postData.has("code") ? this.postData.getString("code") : "";

        if (newPhoneNum.equals("")) {
            this.responseData.put("status", INVALID_PHONE_NUMBER);
            this.responseData.put("message", "new phone number can not be empty");
            return;
        }
        Jedis redis = redisUtil.getJedis();
        if (code.equals(redis.get(newPhoneNum + "code"))) {
            int status = userDao.updatePhoneNum(this.phoneNum, newPhoneNum);
            if (status == 1) {
                this.responseData.put("status", SUCCESS);
                this.responseData.put("message", "Change phone number successfully");
            } else {
                this.responseData.put("status", UNKNOWN_ERROR);
                this.responseData.put("message", "unknown error");
            }
        } else {
            this.responseData.put("status", INCORRECT_VERIFICATION_CODE);
            this.responseData.put("message", "The verification code is wrong");
        }
        redisUtil.returnResource(redis);
    }

    public void get() {
        if (this.phoneNum != null) {
            User user = userDao.getUser(this.phoneNum);
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
