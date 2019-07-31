package model;

import constant.Constant;

import java.text.SimpleDateFormat;
import java.util.Date;

public class User {

    private String userName;
    private String password;
    private String phoneNum;
    private String createTime;
    /*YYYY-MM-DD hh:mm:ss*/
    private String lastLoginTime;
    private String lastLoginIp;
    private short privilegeLevel;
    private String email;

    public User() {
        this.createTime = new SimpleDateFormat(Constant.DATE_PATTERN).format(new Date());
        this.lastLoginTime = createTime;
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

    public String getLastLoginIp() {
        return lastLoginIp;
    }

    public void setLastLoginIp(String lastLoginIp) {
        this.lastLoginIp = lastLoginIp;
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

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(String lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }
}
