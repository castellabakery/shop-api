package kr.co.medicals.common.constants;

import lombok.Getter;

@Getter
public class ApiResponseCodeConstants {

    public static final int SUCCESS = 1000;
    public static final int FAIL = 9999;

    public static final int NOT_EXISTS = 1001;
    public static final int IS_EXISTS = 1002;
    public static final int REQUIRED_EMPTY = 1003;
    public static final int IS_NUMERIC = 1004;
    public static final int CONVERT_FAIL = 1005;
    public static final int NOT_MATCH = 1006;
    public static final int REQUESTED_FAIL = 1007;
    public static final int LOGIN_FAILED = 1008;
    public static final int XX_FAILED = 1009;
    public static final int AUTHENTICATION_FAILED = 1010;

}
