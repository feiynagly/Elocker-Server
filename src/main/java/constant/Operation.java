package constant;

public enum Operation {
    Open("Open"),
    Lock("Lock"),
    Modify_Locker("Modify_Locker"),
    Delete_Locker("Delete_Locker"),
    Add_Locker("Add_Locker"),
    Transfer_Locker("Transfer_Locker"),
    Modify_Authorization("Modify_Authorization"),
    Delete_Authorization("Delete_Authorization"),
    Add_Authorization("Add_Authorization"),
    Login("Login"),
    Login_Out("Login_Out"),
    Change_Password("Change_Password"),
    Unknown("Unknow");

    private String description;

    Operation(String description) {
        this.description = description;
    }

    public static Operation from(String value) {
        Operation operation;
        try {
            operation = Operation.valueOf(value);
        } catch (Exception e) {
            operation = Operation.Unknown;
        }
        return operation;
    }

    @Override
    public String toString() {
        return this.description;
    }
}