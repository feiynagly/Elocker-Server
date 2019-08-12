package constant;

public class Status {

    public static final short SUCCESS = 200;
    public static final short UNAUTHORISED_REQUEST = 401;
    public static final short INVALID_API_KEY = 402;
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
    //用户名为空或者非法
    public static final short INVALID_PHONE_NUMBER = 606;
    public static final short DUPLICATE_AUTHORIZATION = 607;
    //数字签名为空或不存在
    public static final short INVALID_SIGN = 608;
    public static final short INCORRECT_VERIFICATION_CODE = 609;
    //发送短信失败
    public static final short SEND_MESSAGE_FAILED = 610;
    public static final short SHORT_MESSAGE_GATEWAY_UNREACHABLE = 611;
    public static final short USER_ALREADY_EXISTS = 612;
    public static final short USER_NOT_EXISTS = 613;
}
