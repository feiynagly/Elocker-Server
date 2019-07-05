package pojo;

import model.Authorization;

/*扩展Authorization类，增加了锁名称字段，用于前端显示*/
public class AuthorizationViewData extends Authorization {
    private String lockerDescription;

    public String getLockerDescription() {
        return lockerDescription;
    }

    public void setLockerDescription(String lockerDescription) {
        this.lockerDescription = lockerDescription;
    }
}
