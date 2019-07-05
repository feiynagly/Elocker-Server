package reqhandler;

import dao.AuthorizationDao;
import dao.ManuInfoDao;
import model.Authorization;
import net.sf.json.JSONArray;
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

    public void get() {
        this.responseData.put("error", "unknow error");
        this.responseData.put("status", UNKNOWN_ERROR);

        String toAccout = this.urlParam.get("toAccount");
        String serial = this.urlParam.get("serial");

        List<Authorization> authorizationList = authorizationDao.getAuthorization(phoneNum, toAccout, serial);
        this.responseData.put("authorizationlist", authorizationList);
        if (authorizationList != null) {
            this.responseData.put("status", SUCCESS);
            this.responseData.remove("error");
        }
    }

    public void add() {

        String toAccount = this.postData.has("toAccount") ? this.postData.getString("toAccount") : null;
        String serial = this.postData.has("serial") ? this.postData.getString("serial") : null;
        /*禁止给空用户授权*/
        if (toAccount == null || toAccount.equals("")) {
            this.responseData.put("error", "The account to authorise to can not be null");
            this.responseData.put("status", AUTHORIZATION_TO_NULL_ACCOUNT);
            return;
        }
        /*授权的序列号必须存在*/
        if (serial != null && !manuInfoDao.existSerial(serial)) {
            this.responseData.put("error", "invalid serial number");
            this.responseData.put("status", INVALID_SERIAL_NUMBER);
            return;
        }

        /*禁止重复授权*/
        if (serial != null && toAccount != null) {
            if (authorizationDao.existAuthorization(serial, this.phoneNum, toAccount)) {
                this.responseData.put("error", "Duplicate authorization");
                this.responseData.put("status", DUPLICATE_AUTHORIZATION);
                return;
            }
        }

        Authorization authorization = new Authorization();
        authorization.setSerial(this.postData.getString("serial"));
        authorization.setFromAccount(this.phoneNum);
        authorization.setToAccount(toAccount);
        authorization.setStartTime(this.postData.getString("startTime"));
        authorization.setEndTime(this.postData.getString("endTime"));
        authorization.setDescription(this.postData.getString("description"));


        if (authorizationDao.addAuthorization(authorization) == 1) {
            this.responseData.put("success", "Add new authorization successfully");
            this.responseData.put("status", SUCCESS);
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
            this.responseData.put("success", "update authorization successfully");
            this.responseData.put("status", SUCCESS);
        } else {
            this.responseData.put("error", "update authorization failed");
            this.responseData.put("status", UNKNOWN_ERROR);
        }
    }

    public void delete() {
        JSONArray ids = this.postData.getJSONArray("ids");
        JSONArray success = new JSONArray();
        JSONArray error = new JSONArray();
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.getLong(i);
            int status = authorizationDao.delAuthorizationById(id);
            if (status == 1)
                success.add(id);
            else
                error.add(id);
        }
        this.responseData.put("success", success);
        this.responseData.put("error", error);
    }

    public void setAuthorizationDao(AuthorizationDao authorizationDao) {
        this.authorizationDao = authorizationDao;
    }

    public void setManuInfoDao(ManuInfoDao manuInfoDao) {
        this.manuInfoDao = manuInfoDao;
    }
}
