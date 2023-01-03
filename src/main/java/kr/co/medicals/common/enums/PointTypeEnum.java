package kr.co.medicals.common.enums;

import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import lombok.Getter;

@Getter
public enum PointTypeEnum {

    FLAT_RATE("L", "정액포인트"),
    FIXED_RATE("I", "정률포인트"),
    NONE("N", "적립안함");


    private String code;
    private String description;

    PointTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public static boolean isExist(String code) {
        for (PointTypeEnum pointType : PointTypeEnum.values()) {
            if (pointType.code.equals(code)) {
                return true;
            }
        }
        throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "point type");
    }

}
