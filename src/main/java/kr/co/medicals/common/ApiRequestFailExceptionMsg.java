package kr.co.medicals.common;

import lombok.Getter;

@Getter
public class ApiRequestFailExceptionMsg extends RuntimeException {

    private int code;
    private String reason;
    private String data;

    public ApiRequestFailExceptionMsg(int code, String reason, Object data) {
        super(reason+"="+data);
        this.code = code;
        this.reason = reason;
        this.data = String.valueOf(data);
    }

    public ApiRequestFailExceptionMsg(int code, String reason) {
        super(reason);
        this.code = code;
        this.reason = reason;
    }

}
