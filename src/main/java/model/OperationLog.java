package model;

import constant.Operation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import static constant.Constant.DATE_PATTERN;

public class OperationLog {
    private Long id;
    private String serial;
    private String phoneNum;
    private Operation operation;
    /*发生时间，字符串格式 YY-MM-DD hh:mm:ss*/
    private String sTime;
    private String description;

    public OperationLog() {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
        sdf.setTimeZone(TimeZone.getDefault());
        this.sTime = sdf.format(new Date());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public void setsTime(String sTime) {
        this.sTime = sTime;
    }

    public String getsTime() {
        return sTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
