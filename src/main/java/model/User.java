package model;

import constant.Constant;

import java.text.SimpleDateFormat;
import java.util.Date;

public class User {

    private String userName;
    private String apiKey;
    private String password;
    private String phoneNum;
    private String createTime;
    private short privilegeLevel;
    private String email;
    private String lastLoginTime;
    private String lastLoginIp;
    private String appVersion;
    private String userAgent;

    public User() {
        this.createTime = new SimpleDateFormat(Constant.DATE_PATTERN).format(new Date());
        this.privilegeLevel = 0;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        if (userName != null && !userName.equals("")) {
            this.userName = userName;
        }
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
        if (this.userName == null)
            this.userName = phoneNum;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public short getPrivilegeLevel() {
        return privilegeLevel;
    }

    public void setPrivilegeLevel(short privilegeLevel) {
        this.privilegeLevel = privilegeLevel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCreateTime() {
        return createTime;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        if (appVersion != null)
            this.appVersion = appVersion;
        else
            this.appVersion = "Unknow";
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        if (userAgent != null)
            this.userAgent = userAgent;
        else
            this.userAgent = "Unknow";
    }

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
    }
}
