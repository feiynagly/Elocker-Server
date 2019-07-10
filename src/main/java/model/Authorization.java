package model;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static constant.Constant.DATE_PATTERN;

/*记录授权信息*/
public class Authorization {
    private Long id;
    private String serial;
    /*对应授权的锁名称*/
    private String lockerDescription;
    /*授权者手机号*/
    private String fromAccount;
    /*被授权者手机号*/
    private String toAccount;
    private String startTime;
    private String endTime;
    private String description;
    /*星期几，周期性时间，字符串类型,例如String weekday="1,3,5"表示每个周一，周三，周五*/
    private String weekday;
    /*每天的开始时间，周期性时间，格式为hh:mm:ss*/
    private String dailyStartTime;
    /*每天的开始时间，周期性时间，格式为hh:mm:ss*/
    private String dailyEndTime;

    public Authorization() {
        setDefault();
    }

    private void setDefault() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

        this.startTime = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        this.endTime = sdf.format(calendar.getTime());

        this.description = "Authorization";
        this.weekday = "1,2,3,4,5,6,7";
        this.dailyStartTime = "00:00:00";
        this.dailyEndTime = "23:59:00";
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(String fromAccount) {
        this.fromAccount = fromAccount;
    }

    public String getToAccount() {
        return toAccount;
    }

    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        if (startTime == null) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

            this.startTime = sdf.format(calendar.getTime());
        } else {
            this.startTime = startTime;
        }
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        if (endTime == null) {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);
            calendar.add(Calendar.DAY_OF_MONTH, 7);
            this.endTime = sdf.format(calendar.getTime());
        } else {
            this.endTime = endTime;
        }
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWeekday() {
        return weekday;
    }

    public void setWeekday(String weekday) {
        if (weekday == null) {
            this.weekday = "1,2,3,4,5,6,7";
        } else {
            this.weekday = weekday;
        }
    }

    public String getDailyStartTime() {
        return dailyStartTime;
    }

    public void setDailyStartTime(String dailyStartTime) {
        if (dailyStartTime == null) {
            this.dailyStartTime = "00:00:00";
        } else {
            this.dailyStartTime = dailyStartTime;
        }
    }

    public String getDailyEndTime() {
        return dailyEndTime;
    }

    public void setDailyEndTime(String dailyEndTime) {
        if (dailyEndTime == null) {
            this.dailyEndTime = "23:59:00";
        } else {
            this.dailyEndTime = dailyEndTime;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLockerDescription() {
        return lockerDescription;
    }

    public void setLockerDescription(String lockerDescription) {
        this.lockerDescription = lockerDescription;
    }
}
