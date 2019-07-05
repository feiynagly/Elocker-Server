package model;

/*记录授权信息*/
public class Authorization {
    private Long id;
    private String serial;
    /*授权者手机号*/
    private String fromAccount;
    /*被授权者手机号*/
    private String toAccount;
    private String startTime;
    private String endTime;
    private String description;
    /*星期几，周期性时间，数组字符串类型,例如String weekday="[1,3,5]"表示每个周一，周三，周五*/
    private String weekday;
    /*每天的开始时间，周期性时间，格式为hh:mm:ss*/
    private String dailyStartTime;
    /*每天的开始时间，周期性时间，格式为hh:mm:ss*/
    private String dailyEndTime;

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
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
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
        this.weekday = weekday;
    }

    public String getDailyStartTime() {
        return dailyStartTime;
    }

    public void setDailyStartTime(String dailyStartTime) {
        this.dailyStartTime = dailyStartTime;
    }

    public String getDailyEndTime() {
        return dailyEndTime;
    }

    public void setDailyEndTime(String dailyEndTime) {
        this.dailyEndTime = dailyEndTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
