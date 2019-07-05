package reqhandler;

import constant.Operation;
import dao.LockerDao;
import dao.OperationLogDao;
import model.Locker;
import model.OperationLog;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pojo.OperationLogViewData;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static constant.Constant.DATE_FORMAT;
import static constant.Status.SUCCESS;
import static constant.Status.UNKNOWN_ERROR;

@Service("OperationLogRequestHandler")
public class OperationLogRequestHandler extends RequestHandler {

    private static Logger logger = Logger.getLogger(OperationLogRequestHandler.class);
    @Autowired
    private OperationLogDao operationLogDao;
    @Autowired
    private LockerDao lockerDao;

    public void get() {
        String startTime = this.urlParam.get("startTime");
        String endTime = this.urlParam.get("endTime");
        String serial = this.urlParam.get("serial");

        Timestamp sTimestamp = null, eTimestamp = null;
        //如果开始时间为空，则设置为最近三个月
        if (startTime == null || startTime.equals("")) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.MONTH, -1);
            startTime = new SimpleDateFormat(DATE_FORMAT).format(calendar.getTime());
            logger.info("startTime is not set , now set default to " + startTime);
        }

        //如果结束时间为空，则设置为当前时间
        if (endTime == null || endTime.equals("")) {
            endTime = new SimpleDateFormat(DATE_FORMAT).format(new Date());
            logger.info("endTime is not set , now set default to " + endTime);
        }

        /*请求单个设备的日志*/
        if (serial != null && !serial.equals("")) {
            List<OperationLogViewData> logs = operationLogDao.getLogsBySerial(this.phoneNum, serial, startTime, endTime);
            this.responseData.put("logs", logs);
            if (logs != null) {
                this.responseData.put("status", SUCCESS);
            } else {
                this.responseData.put("status", UNKNOWN_ERROR);
                this.responseData.put("error", "unknown error");
            }
        }
        /*获取登录账户的所有日志*/
        else {
            List<OperationLogViewData> logs = operationLogDao.getLogs(this.phoneNum, startTime, endTime);
            this.responseData.put("logs", logs);
            if (logs != null) {
                this.responseData.put("status", SUCCESS);
                this.responseData.put("success", "Get all logs successfully");
            } else {
                this.responseData.put("status", UNKNOWN_ERROR);
                this.responseData.put("error", "unknown error");
            }
        }
    }

    public void add() {
        this.responseData.put("error", "unknown error");
        this.responseData.put("status", UNKNOWN_ERROR);
        if (this.phoneNum != null && this.postData.containsKey("serial")) {
            OperationLog operationLog = new OperationLog();
            operationLog.setPhoneNum(this.phoneNum);
            //operationLog.setOperation(Operation.valueOf(this.postData.getString("operation")));
            operationLog.setSerial(this.postData.getString("serial"));
            Operation operation = Operation.from(this.postData.getString("operation"));
            operationLog.setOperation(operation);
            operationLog.setsTime(this.postData.getString("sTime"));
            operationLog.setDescription(this.postData.getString("description"));
            /*如果是开锁日志，更新t_locker表中最后开锁时间*/
            if (operation == Operation.Open) {
                Locker locker = new Locker();
                locker.setPhoneNum(operationLog.getPhoneNum());
                locker.setSerial(operationLog.getSerial());
                locker.setLastOpenTime(operationLog.getsTime());
                lockerDao.updateLastLoginTime(locker);
            }

            /*提交到数据库*/
            if (operationLogDao.addOperationLog(operationLog) == 1) {
                this.responseData.put("status", SUCCESS);
                this.responseData.put("success", "Add operation log success");
                this.responseData.remove("error");
            }
        }
    }

    public void setOperationLogDao(OperationLogDao operationLogDao) {
        this.operationLogDao = operationLogDao;
    }

    public void setLockerDao(LockerDao lockerDao) {
        this.lockerDao = lockerDao;
    }
}
