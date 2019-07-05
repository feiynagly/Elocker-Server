package pojo;

import model.OperationLog;

public class OperationLogViewData extends OperationLog {
    private String lockerDescription;

    public String getLockerDescription() {
        return lockerDescription;
    }

    public void setLockerDescription(String lockerDescription) {
        this.lockerDescription = lockerDescription;
    }
}
