package kr.co.medicals.order.domain.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@ToString
@Entity
@NoArgsConstructor
@DynamicUpdate
@Table(name = "ORDER_INFO")
public class OrderInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;
    private String buyerCode;
    private String orderIdentificationCode;
    private String staffId;
    private String staffName;
    private String staffPhoneNo;
    private String staffEmail;
    private String corpName;
    private String corpTelNo;
    private String corpEmail;
    private String orderNo;
    private String orderName;
    private int orderInfoState;
    private String shippingAddress;
    private String shippingAddressPostNo;
    private String shippingAddressDetail;
    private String orderMemo;
    private int amount;
    private int savePoint;
    private int usePoint;
    private int cancelPoint;
    private String orgOrderNo;
    private String approveNo;
    private int approveAmount;
    private int cancelAmount;
    private String paymentMethod;
    private LocalDateTime tradeReqDatetime;
    private String tradeResult;
    private String tradeState;
    private String tradeResultMsg;
    private String taxbillYn;
    private String createdId;
    private String modifiedId;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifiedDatetime;

    @Builder
    public OrderInfo(Long seq, String buyerCode, String orderIdentificationCode, String staffId, String staffName, String staffPhoneNo, String staffEmail, String corpEmail, String corpName, String corpTelNo, String orderNo, String orderName, int orderInfoState, String shippingAddress, String shippingAddressPostNo, String shippingAddressDetail, String orderMemo, int amount, int savePoint, int usePoint, int cancelPoint, String orgOrderNo, String approveNo, int approveAmount, int cancelAmount, String paymentMethod, LocalDateTime tradeReqDatetime, String tradeResult, String tradeState, String tradeResultMsg, String taxbillYn, String createdId, String modifiedId, LocalDateTime createdDatetime, LocalDateTime modifiedDatetime) {
        this.seq = seq;
        this.buyerCode = buyerCode;
        this.orderIdentificationCode = orderIdentificationCode;
        this.staffId = staffId;
        this.staffName = staffName;
        this.staffPhoneNo = staffPhoneNo;
        this.staffEmail = staffEmail;
        this.corpEmail = corpEmail;
        this.corpName = corpName;
        this.corpTelNo = corpTelNo;
        this.orderNo = orderNo;
        this.orderName = orderName;
        this.orderInfoState = orderInfoState;
        this.shippingAddress = shippingAddress;
        this.shippingAddressPostNo = shippingAddressPostNo;
        this.shippingAddressDetail = shippingAddressDetail;
        this.orderMemo = orderMemo;
        this.amount = amount;
        this.savePoint = savePoint;
        this.usePoint = usePoint;
        this.cancelPoint = cancelPoint;
        this.orgOrderNo = orgOrderNo;
        this.approveNo = approveNo;
        this.approveAmount = approveAmount;
        this.cancelAmount = cancelAmount;
        this.paymentMethod = paymentMethod;
        this.tradeReqDatetime = tradeReqDatetime;
        this.tradeResult = tradeResult;
        this.tradeState = tradeState;
        this.tradeResultMsg = tradeResultMsg;
        this.createdId = createdId;
        this.modifiedId = modifiedId;
        this.createdDatetime = createdDatetime;
        this.modifiedDatetime = modifiedDatetime;
        this.taxbillYn = taxbillYn;
    }

}
