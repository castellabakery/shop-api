package kr.co.medicals.common.util;

import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import java.util.Objects;

@Slf4j
public class ObjectCheck {

    private ObjectCheck() {
    }

    public static boolean isBlank(String value) {

        if (org.apache.commons.lang3.StringUtils.isEmpty(value)) { // null, blank
            return true;
        }

        if (org.apache.commons.lang3.StringUtils.isBlank(value)) { // white space
            return true;
        }

        if ("null".equals(value)) {
            return true;
        }

        return false;
    }

    public static void stringLengthCheck(String value, int min, int max, String name) {
        if (!(value.length() >= min && value.length() <= max)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "NOT MATCH LENGTH : " + name);
        }
    }

    public static void stringBlankLengthCheck(String value, int min, int max, String name) {
        if (isBlank(value)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", name);
        }
        if (!(value.length() >= min && value.length() <= max)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "NOT MATCH LENGTH : " + name);
        }
    }

    public static void isBlankStringException(String value, String name) {
        if (isBlank(value)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", name);
        }
    }


    public static Long isBlankLongException(Object obj, String name) {
        if (ObjectUtils.isEmpty(obj)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", name);
        }
        Long value;
        try {
            value = Long.valueOf(obj.toString());
        } catch (Exception e) {
            log.error("convert fail obj {}, {}: ", obj, e);
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "NOT_MATCH_TYPE", name);
        }
        return value;
    }

    public static void checkYnException(String value, String name) {
        if (isBlank(value)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", name);
        }
        if (!(Objects.equals("Y", value) || Objects.equals("N", value))) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "NOT_MATCH_VALUE", name);
        }
    }

    // 숫자 있으면 오류
    public static void notNumeric(String value) {
        if (ObjectUtils.isEmpty(value)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", value);
        }
        String number = value.replaceAll("[^0-9]", "");
        if (!ObjectUtils.isEmpty(number)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.IS_NUMERIC, "메인 아이디에는 숫자가 포함 될 수 없습니다.", value);
        }
    }
}
