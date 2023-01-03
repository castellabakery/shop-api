package kr.co.medicals.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    public final static int SUCCESS = 1000;
    private final static String SUCCESS_MESSAGE = "성공";

    public final static int FAIL = -1000;

    private final T data;
    private final int code;
    private final String message;


    protected ApiResponse(T data, int code, String message) {
        this.data = data;
        this.code = code;
        this.message = message;
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse(null, SUCCESS, SUCCESS_MESSAGE);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse(null, SUCCESS, message);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse(data, SUCCESS, SUCCESS_MESSAGE);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse(null, FAIL, message);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse(null, code, message);
    }

    public static <T> ApiResponse<T> fail(int code, String message, T data) {
        return new ApiResponse(data, code, message);
    }

}
