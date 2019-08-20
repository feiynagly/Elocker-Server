package reqhandler;

import constant.Operation;
import dao.AuthorizationDao;
import dao.LogDao;
import dao.ManuInfoDao;
import dao.UserDao;
import model.Authorization;
import model.OperationLog;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static constant.Status.*;


@Service("AuthorizationRequestHandler")
public class AuthorizationRequestHandler extends RequestHandler {

    private static Logger logger = Logger.getLogger(AuthorizationRequestHandler.class);
    @Autowired
    private AuthorizationDao authorizationDao;

    @Autowired
    private ManuInfoDao manuInfoDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private LogDao logDao;

    public void get() {
        this.responseData.put("error", "Unknown error");
        this.responseData.put("status", UNKNOWN_ERROR);

        String toAccount = this.urlParam.get("toAccount");
        String serial = this.urlParam.get("serial");

        List<Authorization> authorizationList = authorizationDao.getAuthorization(phoneNum, toAccount, serial);
        this.responseData.put("authorizationList", authorizationList);
        if (authorizationList != null) {
            this.responseData.put("status", SUCCESS);
            this.responseData.put("message", "get authorization list successfully");
            this.responseData.remove("error");
        }
    }

    public void add() {

        String toAccount = this.postData.has("toAccount") ? this.postData.getString("toAccount") : null;
        String serial = this.postData.has("serial") ? this.postData.getString("serial") : null;
        /*授权账户必须存在*/
        if (toAccount == null || toAccount.equals("") || !userDao.existUser(toAccount)) {
            this.responseData.put("error", "The account to authorise to is valid");
            this.responseData.put("status", INVALID_TO_ACCOUNT_VALUE);
            return;
        }

        /*授权的序列号必须存在*/
        if (serial == null || !manuInfoDao.existSerial(serial)) {
            this.responseData.put("error", "invalid serial number");
            this.responseData.put("status", INVALID_SERIAL_NUMBER);
            return;
        }

        /*禁止重复授权*/
        if (authorizationDao.existAuthorization(serial, this.phoneNum, toAccount)) {
            this.responseData.put("error", "Duplicate authorization");
            this.responseData.put("status", DUPLICATE_AUTHORIZATION);
            return;
        }

        Authorization authorization = new Authorization();
        authorization.setSerial(this.postData.getString("serial"));
        authorization.setFromAccount(this.phoneNum);
        authorization.setToAccount(toAccount);
        authorization.setStartTime(this.postData.getString("startTime"));
        authorization.setEndTime(this.postData.getString("endTime"));
        authorization.setDescription(this.postData.getString("description"));
        if (this.postData.containsKey("weekday"))
            authorization.setWeekday(this.postData.getString("weekday"));
        if (this.postData.containsKey("dailyStartTime"))
            authorization.setDailyStartTime(this.postData.getString("dailyStartTime"));
        if (this.postData.containsKey("dailyEndTime"))
            authorization.setDailyEndTime(this.postData.getString("dailyEndTime"));

        if (authorizationDao.addAuthorization(authorization) == 1) {
            this.responseData.put("message", "Add new authorization successfully");
            this.responseData.put("status", SUCCESS);
            OperationLog operationLog = new OperationLog();
            operationLog.setSerial(serial);
            operationLog.setPhoneNum(this.phoneNum);
            operationLog.setOperation(Operation.Add_Authorization);
            operationLog.setDescription("Add a new authorization");
            logDao.addOperationLog(operationLog);
        } else {
            this.responseData.put("error", "Add authorization failed");
            this.responseData.put("status", UNKNOWN_ERROR);
        }
    }

    public void update() {
        Authorization authorization = new Authorization();
        authorization.setId(postData.getLong("id"));
        authorization.setStartTime(postData.getString("startTime"));
        authorization.setEndTime(postData.getString("endTime"));
        authorization.setDescription(postData.getString("description"));
        if (authorizationDao.updateAuthorization(authorization) == 1) {
            this.responseData.put("message", "update authorization successfully");
            this.responseData.put("status", SUCCESS);
        } else {
            this.responseData.put("message", "update authorization failed");
            this.responseData.put("status", UNKNOWN_ERROR);
        }
    }

    public void delete() {
        JSONArray ids = this.postData.getJSONArray("ids");
        JSONArray success = new JSONArray();
        JSONArray error = new JSONArray();
        for (int i = 0; i < ids.size(); i++) {
            JSONObject item = ids.getJSONObject(i);
            Long id = item.getLong("id");
            String serial = item.getString("serial");
            int status = authorizationDao.delAuthorizationById(id);
            if (status == 1) {
                success.add(id);
                OperationLog operationLog = new OperationLog();
                operationLog.setDescription("Delete Authorization " + id);
                operationLog.setPhoneNum(this.phoneNum);
                operationLog.setOperation(Operation.Delete_Authorization);
                operationLog.setSerial(serial);
                logDao.addOperationLog(operationLog);
            }
            else
                error.add(id);
        }
        this.responseData.put("success", success);
        this.responseData.put("error", error);
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setAuthorizationDao(AuthorizationDao authorizationDao) {
        this.authorizationDao = authorizationDao;
    }

    public void setManuInfoDao(ManuInfoDao manuInfoDao) {
        this.manuInfoDao = manuInfoDao;
    }

    public void setLogDao(LogDao logDao) {
        this.logDao = logDao;
    }
}
