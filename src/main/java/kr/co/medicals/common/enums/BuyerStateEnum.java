package kr.co.medicals.common.enums;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Getter
@Slf4j
public enum BuyerStateEnum {

    READY("R", "READY", "가입 승인 대기"),
    WAIT("W", "WAIT", "수정 승인 대기"),
    DONE("D", "DONE", "관리자 확인 완료"),
    REJECTED("J", "REJECTED", "관리자 반려"),
    STOP("S", "STOP", "정지"),
    EXPIRED("X", "EXPIRED", "삭제");

    private String code;
    private String enCode;
    private String krCode;

    BuyerStateEnum(String code, String enCode, String krCode) {
        this.code = code;
        this.enCode = enCode;
        this.krCode = krCode;
    }

    public static String getKrCodeByCode(String krCode) {
        for (BuyerStateEnum stateEnum : BuyerStateEnum.values()) {
            if (Objects.equals(stateEnum.getKrCode(), krCode)) {
                return stateEnum.getCode();
            }
        }
        return "";
    }

}
