package reqhandler;

import constant.Operation;
import dao.LockerDao;
import dao.LogDao;
import model.Locker;
import model.OperationLog;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pojo.OperationLogViewData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static constant.Constant.DATE_PATTERN;
import static constant.Status.SUCCESS;
import static constant.Status.UNKNOWN_ERROR;

@Service("LogRequestHandler")
public class LogRequestHandler extends RequestHandler {

    private static Logger logger = Logger.getLogger(LogRequestHandler.class);
    @Autowired
    private LogDao logDao;
    @Autowired
    private LockerDao lockerDao;

    public void get() {
        String startTime = this.urlParam.get("startTime");
        String endTime = this.urlParam.get("endTime");
        String serial = this.urlParam.get("serial");

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        Calendar calendar = Calendar.getInstance();

        /*将日期字符串格式化为标准字符串 MYSQL不能识别2019-8-2 00:00:00这样的日期字符串*/
        try {
            startTime = sdf.format(sdf.parse(startTime));
        } catch (Exception e) {
            //如果开始时间为空或无法解析的字符串，则设置为最近7天
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, -7);
            startTime = sdf.format(calendar.getTime());
            logger.debug("startTime is not set or invalid, now set default to : " + startTime);
        }

        try {
            endTime = sdf.format(sdf.parse(endTime));
        } catch (Exception e) {
            //如果结束时间为空，则设置为当前时间
            logger.debug("endTime is not set or invalid, now set default to " + endTime);
            endTime = sdf.format(new Date());
        }

        int page = 0;
        int pageSize = 1000;
        /*获取分页信息,如果不包含分页信息，则设置为0页，每页1000条*/
        if (urlParam.containsKey("page")) {
            try {
                page = Integer.parseInt(urlParam.get("page"));
            } catch (Exception e) {
                logger.error("Invalid page param , page: " + page);
            }
        }
        if (urlParam.containsKey("pageSize")) {
            try {
                pageSize = Integer.parseInt(urlParam.get("pageSize"));
            } catch (Exception e) {
                logger.error("Invalid pagesize param, pageSize: " + pageSize);
            }
        }

        /*请求单个设备的日志*/
        if (serial != null && !serial.equals("")) {
            List<OperationLogViewData> logs = logDao.getLogsBySerial(
                    this.phoneNum, serial, startTime, endTime, page, pageSize);
            this.responseData.put("logs", logs);
            if (logs != null) {
                this.responseData.put("status", SUCCESS);
                this.responseData.put("message", "Get log successfully");
            } else {
                this.responseData.put("status", UNKNOWN_ERROR);
                this.responseData.put("message", "unknown error");
            }
        }
        /*获取登录账户的所有日志*/
        else {
            List<OperationLogViewData> logs = logDao.getLogs(
                    this.phoneNum, startTime, endTime, page, pageSize);
            this.responseData.put("logs", logs);
            if (logs != null) {
                this.responseData.put("status", SUCCESS);
                this.responseData.put("message", "Get all logs successfully");
            } else {
                this.responseData.put("status", UNKNOWN_ERROR);
                this.responseData.put("message", "unknown error");
            }
        }
    }

    public void add() {
        this.responseData.put("message", "unknown error");
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
                lockerDao.updateLastOpenTime(locker);
            }

            /*提交到数据库*/
            if (logDao.addOperationLog(operationLog) == 1) {
                this.responseData.put("status", SUCCESS);
                this.responseData.put("message", "Add operation log success");
            }
        }
    }

    public void setLogDao(LogDao logDao) {
        this.logDao = logDao;
    }

    public void setLockerDao(LockerDao lockerDao) {
        this.lockerDao = lockerDao;
    }
}
