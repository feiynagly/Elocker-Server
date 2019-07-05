package model;

public class Locker {
    private String serial;
    private String phoneNum;
    private String description;
    private String createTime;
    private String lastOpenTime;
    private String hwType;
    /*开锁次数*/
    private int toggleTimes;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLastOpenTime() {
        return lastOpenTime;
    }

    public void setLastOpenTime(String lastOpenTime) {
        this.lastOpenTime = lastOpenTime;
    }

    public String getHwType() {
        return hwType;
    }

    public void setHwType(String hwType) {
        this.hwType = hwType;
    }

    public int getToggleTimes() {
        return toggleTimes;
    }

    public void setToggleTimes(int toggleTimes) {
        this.toggleTimes = toggleTimes;
    }
}
