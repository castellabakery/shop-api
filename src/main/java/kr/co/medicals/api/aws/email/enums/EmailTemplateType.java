package kr.co.medicals.api.aws.email.enums;

import lombok.Getter;

@Getter
public enum EmailTemplateType {
    SIGNUP_ACCEPT ("signUpAccept.html", "[] 가입완료 안내해드립니다."),
    SIGNUP_REJECT ("signUpReject.html", "[] 가입반려 안내해드립니다."),
    ITEM_PURCHASE ("itemPurchase.html", "[] 결제하신 내역을 안내해드립니다."),
    ITEM_PURCHASE_CANCEL ("itemPurchaseCancel.html", "[] 결제취소 안내입니다."),
    ITEM_PURCHASE_REFUND ("itemPurchaseRefund.html", "[] 결제하신 상품의 환불 안내입니다.");

    public String templateFileName;
    public String subject;

    EmailTemplateType(String templateFileName, String subject) {
        this.templateFileName = templateFileName;
        this.subject = subject;
    }
}
