package constant;

public class Status {

    public static final short SUCCESS = 200;
    public static final short UNAUTHORISED_REQUEST = 401;
    public static final short NOT_ACCEPTABLE = 406;
    public static final short REQUEST_TOO_FREQUENTLY = 429;
    public static final short UNKNOWN_ERROR = 500;

    public static final short INCORRECT_USERNAME_OR_PASSWORD = 601;
    //连续多次认证错误
    public static final short AUTHENTICATION_ERROR_COUNTER_EXCEED = 602;
    public static final short INVALID_SERIAL_NUMBER = 603;
    //序列号已经绑定至其他用户
    public static final short DUPLICATE_SERIAL_NUMBER = 604;
    //授权给空账户
    public static final short INVALID_TO_ACCOUNT_VALUE = 605;
    public static final short INVALID_PHONE_NUMBER = 606;
    public static final short DUPLICATE_AUTHORIZATION = 607;


    public static final short AUTHORIZATION_TO_NULL_ACCOUNT = 610;

}
