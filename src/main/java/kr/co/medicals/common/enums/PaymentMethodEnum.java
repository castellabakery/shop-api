package kr.co.medicals.common.enums;

import lombok.Getter;

@Getter
public enum PaymentMethodEnum {

    CREDIT_CARD("CC", "CREDIT_CARD", "카드"),
    POINT("PT", "POINT", "포인트"),
    VIRTUAL_ACCOUNT("VA", "VIRTUAL_ACCOUNT", "");


    private String code;
    private String enCode;
    private String krCode;

    PaymentMethodEnum(String code, String enCode, String krCode) {
        this.code = code;
        this.enCode = enCode;
        this.krCode = krCode;
    }

    public static boolean isExist(String code) {
        for (PaymentMethodEnum paymentMethodEnum : PaymentMethodEnum.values()) {
            if (paymentMethodEnum.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

}
