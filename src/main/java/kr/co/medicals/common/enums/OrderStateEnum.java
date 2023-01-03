package kr.co.medicals.common.enums;

import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Getter
public enum OrderStateEnum {

    ORDER_STANDBY(10, "ORDER_STANDBY", "주문대기"),
    PAY_STANDBY(11, "PAY_STANDBY", "결제대기"),
    PAY_DONE(12, "PAY_DONE", "결제완료"),

    SHIPPING(13, "SHIPPING", "배송중"),
    DELIVERY_COMPLETED(14, "DELIVERY_COMPLETED", "배송완료"),

    CANCEL_REQUEST(15, "CANCEL_REQUEST", "취소신청"),
    CANCEL_DONE(16, "CANCEL_DONE", "취소완료"),

    REFUND_REQUEST(17, "REFUND_REQUEST", "환불신청"),
    REFUND_DONE(18, "REFUND_DONE", "환불완료"),

    ORDER_CONFIRM(20, "ORDER_CONFIRM", "구매확정"),

    PAYMENT_ERROR(98, "PAYMENT_ERROR", "결제실패"), // -- 성공 후 실패. 자동 취소마저 실패함.
    ORDER_ERROR(99, "ORDER_ERROR", "주문오류"); // -- 호출 못함(+통신 실패), 호출은 했으나 응답이 오류.


    private int code;
    private String enCode;
    private String krCode;

    OrderStateEnum(int code, String enCode, String krCode) {
        this.code = code;
        this.enCode = enCode;
        this.krCode = krCode;
    }

    public static int getCodeByEnCode(String enCode) {
        for (OrderStateEnum stateEnum : OrderStateEnum.values()) {
            if (Objects.equals(stateEnum.getEnCode(), enCode)) {
                return stateEnum.getCode();
            }
        }
        throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "요청한 주문 상태 일치하는 영문 코드가 없음.", enCode);
    }

    public static int getCodeByKrCode(String krCode) {
        for (OrderStateEnum stateEnum : OrderStateEnum.values()) {
            if (Objects.equals(stateEnum.getKrCode(), krCode)) {
                return stateEnum.getCode();
            }
        }
        return 0;
    }

    public static String getEnCodeByCode(int code) {
        for (OrderStateEnum stateEnum : OrderStateEnum.values()) {
            if (Objects.equals(stateEnum.getCode(), code)) {
                return stateEnum.getEnCode();
            }
        }
        throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "요청한 주문 상태 일치하는 코드가 없음.", code);
    }

    public static List<Integer> searchOrderInProgress() {
        return Arrays.asList( // 결제완료, 배송중, 배송완료, 취소신청, 환불신청, 결제오류
                OrderStateEnum.PAY_DONE.getCode()
                , OrderStateEnum.SHIPPING.getCode()
                , OrderStateEnum.DELIVERY_COMPLETED.getCode()
                , OrderStateEnum.CANCEL_REQUEST.getCode()
                , OrderStateEnum.REFUND_REQUEST.getCode()
                , OrderStateEnum.PAYMENT_ERROR.getCode()
        );
    }

    public static List<Integer> searchOrderDoneProgress() {
        return Arrays.asList( // 취소완료, 환불완료, 구매확정, 주문오류
                OrderStateEnum.CANCEL_DONE.getCode()
                , OrderStateEnum.REFUND_DONE.getCode()
                , OrderStateEnum.ORDER_CONFIRM.getCode()
                , OrderStateEnum.ORDER_ERROR.getCode()
        );
    }


    // buyer 의 경우 변경 가능 여부
    public static void checkApproveStateBuyer(String oldEnCode, String newEnCode) {

        // 결제완료 -> 취소신청
        if (Objects.equals(oldEnCode, OrderStateEnum.PAY_DONE.getEnCode()) &&
                Objects.equals(newEnCode, OrderStateEnum.CANCEL_REQUEST.getEnCode())) {
            return;
        }

        // 배송중 -> 환불신청
        if (Objects.equals(oldEnCode, OrderStateEnum.SHIPPING.getEnCode())
                && Objects.equals(newEnCode, OrderStateEnum.REFUND_REQUEST.getEnCode())) {
            return;
        }

        // 배송완료 -> 구매확정, 환불신청
        if (Objects.equals(oldEnCode, OrderStateEnum.DELIVERY_COMPLETED.getEnCode())
                && (Objects.equals(newEnCode, OrderStateEnum.ORDER_CONFIRM.getEnCode())
                || Objects.equals(newEnCode, OrderStateEnum.REFUND_REQUEST.getEnCode()))) {
            return;
        }

        throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "회원 : [" + oldEnCode + "]에서 [" + newEnCode + "]변경 불가.");
    }

    // admin 의 경우 변경 가능 여부
    public static void checkApproveStateAdmin(String oldEnCode, String newEnCode) {

        // 결제완료 -> 배송중, 취소승인
        if (Objects.equals(oldEnCode, OrderStateEnum.PAY_DONE.getEnCode())
                && (Objects.equals(newEnCode, OrderStateEnum.SHIPPING.getEnCode())
                || Objects.equals(newEnCode, OrderStateEnum.CANCEL_DONE.getEnCode()))) {
            return;
        }

        // 배송중 -> 배송중, 배송완료
        if (Objects.equals(oldEnCode, OrderStateEnum.SHIPPING.getEnCode())
                && (Objects.equals(newEnCode, OrderStateEnum.SHIPPING.getEnCode())
                || Objects.equals(newEnCode, OrderStateEnum.DELIVERY_COMPLETED.getEnCode()))) {
            return;
        }

        // 배송완료 -> 환불승인, 구매확정
        if (Objects.equals(oldEnCode, OrderStateEnum.DELIVERY_COMPLETED.getEnCode())
                && (Objects.equals(newEnCode, OrderStateEnum.REFUND_DONE.getEnCode())
                || Objects.equals(newEnCode, OrderStateEnum.ORDER_CONFIRM.getEnCode()))) {
            return;
        }

        // 취소신청 -> 결제완료, 배송중, 배송완료, 취소승인
        if (Objects.equals(oldEnCode, OrderStateEnum.CANCEL_REQUEST.getEnCode())
                && (Objects.equals(newEnCode, OrderStateEnum.PAY_DONE.getEnCode())
                || Objects.equals(newEnCode, OrderStateEnum.SHIPPING.getEnCode())
                || Objects.equals(newEnCode, OrderStateEnum.DELIVERY_COMPLETED.getEnCode())
                || Objects.equals(newEnCode, OrderStateEnum.CANCEL_DONE.getEnCode()))) {
            return;
        }

        // 환불신청 -> 배송완료, 환불승인 (기획요청 : 환불신청 단계에서 관리자는 배송완료, 환불승인으로 밖에 바꿀 수 없음.)
        if (Objects.equals(oldEnCode, OrderStateEnum.REFUND_REQUEST.getEnCode())
                && (Objects.equals(newEnCode, OrderStateEnum.DELIVERY_COMPLETED.getEnCode())
                || Objects.equals(newEnCode, OrderStateEnum.REFUND_DONE.getEnCode()))) {
            return;
        }

        // 주문오류 -> 주문취소
        if (Objects.equals(oldEnCode, OrderStateEnum.PAYMENT_ERROR.getEnCode())
                && (Objects.equals(newEnCode, OrderStateEnum.CANCEL_DONE.getEnCode()))) {
            return;
        }

        throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "관리자 : [" + oldEnCode + "]에서 [" + newEnCode + "]변경 불가.");
    }

}
