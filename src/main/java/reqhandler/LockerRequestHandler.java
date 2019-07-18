package reqhandler;

import constant.Operation;
import dao.*;
import model.Locker;
import model.OperationLog;
import net.sf.json.JSONArray;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static constant.Constant.DATE_PATTERN;
import static constant.Constant.TYPE_BLE_LOCKER;
import static constant.Status.*;

@Service("LockerRequestHandler")
public class LockerRequestHandler extends RequestHandler {

    private static Logger logger = Logger.getLogger(LockerRequestHandler.class);

    @Autowired
    private ManuInfoDao manuInfoDao;

    @Autowired
    private LockerDao lockerDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private AuthorizationDao authorizationDao;

    @Autowired
    private OperationLogDao operationLogDao;

    /*如果serial 为null或者""，获取该用户下的所有locker(以及授权给用户的锁)
     *，否则获取指定的locker*/
    public void get() {
        String serial = this.urlParam.get("serial");
        List<Locker> lockerList = lockerDao.getLockerListByPhoneNum(this.phoneNum, serial);
        this.responseData.put("lockerList", lockerList);
        if (lockerList != null) {
            this.responseData.put("success", "get locker successfully");
            this.responseData.put("status", SUCCESS);
        } else {
            this.responseData.put("error", "Internal error");
            this.responseData.put("status", UNKNOWN_ERROR);
        }

    }

    public void add() {
        String serial = this.postData.getString("serial");
        String description = this.postData.getString("description");
        String createTime = new SimpleDateFormat(DATE_PATTERN).format(new Date());

        Locker locker = new Locker();
        locker.setSerial(serial);
        locker.setPhoneNum(this.phoneNum);
        locker.setDescription(description);
        locker.setCreateTime(createTime);
        locker.setLastOpenTime(createTime);
        locker.setHwType(TYPE_BLE_LOCKER);

        if (isLockerValid(locker)) {
            int status = lockerDao.addLocker(locker);

            /*消息处理*/
            if (status == 1) {
                this.responseData.put("success", "Add new locker success");
                this.responseData.put("status", SUCCESS);
                OperationLog operationLog = new OperationLog();
                operationLog.setPhoneNum(this.phoneNum);
                operationLog.setOperation(Operation.Add_Locker);
                operationLog.setSerial(serial);
                operationLog.setDescription("Add Locker " + serial);
                operationLogDao.addOperationLog(operationLog);
            } else if (status == -1) {
                this.responseData.put("error", "Internal error");
                this.responseData.put("status", UNKNOWN_ERROR);
            }
        }
    }

    private boolean isLockerValid(Locker locker) {
        if (locker.getPhoneNum() == null || locker.getPhoneNum().equals("")) {
            this.responseData.put("error", "Phone number can not be empty");
            this.responseData.put("status", INVALID_PHONE_NUMBER);
            return false;
        }

        if (locker.getSerial() == null || locker.getSerial().equals("")) {
            this.responseData.put("error", "Serial number can not be empty");
            this.responseData.put("status", INVALID_SERIAL_NUMBER);
            return false;
        }

        /*如果产品库中没有该产品*/
        if (!manuInfoDao.existSerial(locker.getSerial())) {
            this.responseData.put("error", "Serial number is invalid");
            this.responseData.put("status", INVALID_SERIAL_NUMBER);
            return false;
        }

        /*如果已经绑定到其它账户*/
        if (lockerDao.existSerial(locker.getSerial())) {
            this.responseData.put("error", "The locker is bind to other account already");
            this.responseData.put("status", DUPLICATE_SERIAL_NUMBER);
            return false;
        }
        return true;

    }

    /*对Locker进行重命名*/
    public void update() {
        String serial = this.postData.getString("serial");
        String description = this.postData.getString("description");

        Locker locker = new Locker();
        locker.setSerial(serial);
        locker.setDescription(description);
        locker.setPhoneNum(this.phoneNum);
        if (serial != null && this.phoneNum != null) {
            int status = lockerDao.updateLockerDescription(locker);
            if (status == 1) {
                this.responseData.put("success", "update locker success");
                this.responseData.put("status", SUCCESS);
                OperationLog operationLog = new OperationLog();
                operationLog.setPhoneNum(this.phoneNum);
                operationLog.setSerial(serial);
                operationLog.setOperation(Operation.Modify_Locker);
                operationLog.setDescription("Modify Locker Name");
                operationLogDao.addOperationLog(operationLog);
            } else {
                this.responseData.put("error", "update locker failed");
                this.responseData.put("status", UNKNOWN_ERROR);
            }
        }
    }

    public void delete() {
        JSONArray lockerSerials = this.postData.getJSONArray("lockerSerials");
        /*记录删除成功和删除失败的条目*/
        JSONArray success = new JSONArray();
        JSONArray error = new JSONArray();
        for (int i = 0; i < lockerSerials.size(); i++) {
            String serial = lockerSerials.getString(i);
            if (serial != null && this.phoneNum != null) {
                int status = lockerDao.delLocker(this.phoneNum, serial);
                if (status == 1) {
                    success.add(serial);
                    /*删除相关授权*/
                    authorizationDao.delAllAuthorizationByOwner(serial, this.phoneNum);
                    /*删除相关日志，并添加一条删除记录*/
                    operationLogDao.delOperationLogBySerial(serial);
                    OperationLog operationLog = new OperationLog();
                    operationLog.setSerial(serial);
                    operationLog.setOperation(Operation.Delete_Locker);
                    operationLog.setPhoneNum(this.phoneNum);
                    operationLog.setDescription("Delete Locker " + serial);
                    operationLogDao.addOperationLog(operationLog);
                } else
                    error.add(serial);
            }
        }
        this.responseData.put("success", success);
        this.responseData.put("error", error);
    }

    public void transfer() {
        String toAccount = this.postData.getString("toAccount");
        String serial = this.postData.getString("serial");

        if (userDao.existUser(toAccount)) {
            int status = lockerDao.transferLocker(this.phoneNum, serial, toAccount);
            if (status > 0) {
                this.responseData.put("success", "transfer locker success");
                this.responseData.put("status", SUCCESS);
                /*删除相关授权*/
                authorizationDao.delAllAuthorizationByOwner(serial, this.phoneNum);
                /*记录日志*/
                OperationLog operationLog = new OperationLog();
                operationLog.setPhoneNum(toAccount);
                operationLog.setSerial(serial);
                operationLog.setOperation(Operation.Transfer_Locker);
                operationLog.setDescription("Transfer Locker from " + this.phoneNum);
                operationLogDao.addOperationLog(operationLog);

            } else {
                this.responseData.put("error", "Internal error");
                this.responseData.put("status", UNKNOWN_ERROR);
            }
        } else {
            this.responseData.put("error", "the account to authorize to does not exist");
            this.responseData.put("status", INVALID_TO_ACCOUNT_VALUE);
        }

    }

    public void setLockerDao(LockerDao lockerDao) {
        this.lockerDao = lockerDao;
    }

    public void setManuInfoDao(ManuInfoDao manuInfoDao) {
        this.manuInfoDao = manuInfoDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setOperationLogDao(OperationLogDao operationLogDao) {
        this.operationLogDao = operationLogDao;
    }

    public void setAuthorizationDao(AuthorizationDao authorizationDao) {
        this.authorizationDao = authorizationDao;
    }
}
