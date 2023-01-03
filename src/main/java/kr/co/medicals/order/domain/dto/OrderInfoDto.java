package kr.co.medicals.order.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.medicals.buyer.domain.dto.BuyerIdentificationDto;
import kr.co.medicals.common.enums.OrderStateEnum;
import kr.co.medicals.common.util.DateFormatUtils;
import kr.co.medicals.common.util.SessionUtil;
import kr.co.medicals.order.domain.entity.OrderInfo;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderInfoDto {

    private Long seq;
    private String buyerCode;
    private String orderIdentificationCode;
    private String staffId;
    private String staffName;
    private String staffPhoneNo;
    private String staffEmail;
    private String corpEmail;
    private String corpName;
    private String corpTelNo;
    private String orderNo;
    private String orderName;
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
    private String orderInfoState;
    private String createdId;
    private String modifiedId;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifiedDatetime;
    private List<OrderItemDto> orderItemList;
    private BuyerIdentificationDto buyerIdentification;
    private Long listCnt;

    public OrderInfoDto() {
    }

    @Builder(builderClassName = "byCreate", builderMethodName = "byCreate")
    public OrderInfoDto(Long seq, String buyerCode, String orderIdentificationCode, String staffId, String staffName, String staffPhoneNo, String staffEmail, String corpEmail, String corpName, String corpTelNo, String orderNo, String orderName, String shippingAddress, String shippingAddressPostNo, String shippingAddressDetail, String orderMemo, int amount, int savePoint, int usePoint, int cancelPoint, String orgOrderNo, String approveNo, int approveAmount, int cancelAmount, String paymentMethod, LocalDateTime tradeReqDatetime, String tradeResult, String tradeState, String tradeResultMsg, String taxbillYn, String orderInfoState, String createdId, String modifiedId, LocalDateTime createdDatetime, LocalDateTime modifiedDatetime, List<OrderItemDto> orderItemList, BuyerIdentificationDto buyerIdentification) {
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
        this.orderInfoState = orderInfoState;
        this.createdId = createdId;
        this.modifiedId = modifiedId;
        this.createdDatetime = createdDatetime;
        this.modifiedDatetime = modifiedDatetime;
        this.orderItemList = orderItemList;
        this.buyerIdentification = buyerIdentification;
        this.taxbillYn = taxbillYn;
    }

    @Builder(builderClassName = "selectByEntity", builderMethodName = "selectByEntity")
    public OrderInfoDto(OrderInfo orderInfo) {
        this.seq = orderInfo.getSeq();
        this.buyerCode = orderInfo.getBuyerCode();
        this.orderIdentificationCode = orderInfo.getOrderIdentificationCode();
        this.staffId = orderInfo.getStaffId();
        this.staffName = orderInfo.getStaffName();
        this.staffPhoneNo = orderInfo.getStaffPhoneNo();
        this.staffEmail = orderInfo.getStaffEmail();
        this.corpEmail = orderInfo.getCorpEmail();
        this.corpName = orderInfo.getCorpName();
        this.corpTelNo = orderInfo.getCorpTelNo();
        this.orderNo = orderInfo.getOrderNo();
        this.orderName = orderInfo.getOrderName();
        this.orderInfoState = OrderStateEnum.getEnCodeByCode(orderInfo.getOrderInfoState());
        this.shippingAddress = orderInfo.getShippingAddress();
        this.shippingAddressPostNo = orderInfo.getShippingAddressPostNo();
        this.shippingAddressDetail = orderInfo.getShippingAddressDetail();
        this.orderMemo = orderInfo.getOrderMemo();
        this.amount = orderInfo.getAmount();
        this.savePoint = orderInfo.getSavePoint();
        this.usePoint = orderInfo.getUsePoint();
        this.cancelPoint = orderInfo.getCancelPoint();
        this.orgOrderNo = orderInfo.getOrgOrderNo();
        this.approveNo = orderInfo.getApproveNo();
        this.approveAmount = orderInfo.getApproveAmount();
        this.cancelAmount = orderInfo.getCancelAmount();
        this.paymentMethod = orderInfo.getPaymentMethod();
        this.tradeReqDatetime = orderInfo.getTradeReqDatetime();
        this.tradeState = orderInfo.getTradeState();
        this.tradeResult = orderInfo.getTradeResult();
        this.tradeResultMsg = orderInfo.getTradeResultMsg();
        this.createdId = orderInfo.getCreatedId();
        this.createdDatetime = orderInfo.getCreatedDatetime();
        this.modifiedId = orderInfo.getModifiedId();
        this.modifiedDatetime = orderInfo.getModifiedDatetime();
        this.taxbillYn = orderInfo.getTaxbillYn();
    }

    public OrderInfo insertEntity(BuyerIdentificationDto buyerInfo, int amount, int savePoint, String orderName) {

        String userCode = SessionUtil.getUserCode();

        String addNo = String.format("%05d", buyerInfo.getBuyer().getSeq());
        String orderNo = LocalDateTime.now().format(DateFormatUtils.DATETIME_CODE) + addNo;

        return OrderInfo
                .builder()
                .buyerCode(buyerInfo.getBuyer().getBuyerCode())
                .orderIdentificationCode(userCode)

                .staffId(buyerInfo.getBuyerIdentificationId())
                .staffName(buyerInfo.getStaffName())
                .staffPhoneNo(buyerInfo.getStaffPhoneNo())
                .staffEmail(buyerInfo.getStaffEmail())

                .corpName(buyerInfo.getBuyer().getCorpName())
                .corpTelNo(buyerInfo.getBuyer().getCorpTelNo())
                .corpEmail(buyerInfo.getBuyer().getCorpEmail())

                .orderNo(orderNo)
                .orderName(orderName)
                .orderInfoState(OrderStateEnum.ORDER_STANDBY.getCode())
                .shippingAddress(buyerInfo.getBuyer().getCorpShippingAddress())
                .shippingAddressPostNo(buyerInfo.getBuyer().getShippingAddressPostNo())
                .shippingAddressDetail(buyerInfo.getBuyer().getShippingAddressDetail())

                .amount(amount)
                .savePoint(savePoint)

                .createdId(userCode)
                .createdDatetime(LocalDateTime.now())
                .build();
    }

    public OrderInfo updateRes(int orderState, int approveAmount, int usePoint, String tid, String paymentMethod, String tradeResult, String tradeResultMsg, String buyerIdentificationCode) {
        return OrderInfo
                .builder()
                .seq(this.seq)
                .buyerCode(this.buyerCode)
                .orderIdentificationCode(this.orderIdentificationCode)

                .staffId(this.staffId)
                .staffName(this.staffName)
                .staffPhoneNo(this.staffPhoneNo)
                .staffEmail(this.staffEmail)
                .corpName(this.corpName)
                .corpTelNo(this.corpTelNo)
                .corpEmail(this.corpEmail)

                .orderNo(this.orderNo)
                .orderName(this.orderName)
                .orderInfoState(orderState)
                .shippingAddress(this.shippingAddress)
                .shippingAddressPostNo(this.shippingAddressPostNo)
                .shippingAddressDetail(this.shippingAddressDetail)

                .orderMemo(this.orderMemo)

                .amount(this.amount)
                .savePoint(this.savePoint)
                .usePoint(usePoint)
                .cancelPoint(this.cancelPoint)
                .orgOrderNo(tid)

                .approveNo(this.approveNo)
                .approveAmount(approveAmount)
                .cancelAmount(this.cancelAmount)
                .paymentMethod(paymentMethod)

                .tradeReqDatetime(this.tradeReqDatetime)
                .tradeState(tradeState)
                .tradeResult(tradeResult)
                .tradeResultMsg(tradeResultMsg)

                .createdId(this.createdId)
                .createdDatetime(this.createdDatetime)
                .modifiedId(buyerIdentificationCode)
                .modifiedDatetime(LocalDateTime.now())

                .build();
    }


    public OrderInfo updateError(int orderState, String tradeResult, String tradeResultMsg) {
        return OrderInfo
                .builder()
                .seq(this.seq)
                .buyerCode(this.buyerCode)
                .orderIdentificationCode(this.orderIdentificationCode)

                .staffId(this.staffId)
                .staffName(this.staffName)
                .staffPhoneNo(this.staffPhoneNo)
                .staffEmail(this.staffEmail)
                .corpName(this.corpName)
                .corpTelNo(this.corpTelNo)
                .corpEmail(this.corpEmail)

                .orderNo(this.orderNo)
                .orderName(this.orderName)
                .orderInfoState(orderState)
                .shippingAddress(this.shippingAddress)
                .shippingAddressPostNo(this.shippingAddressPostNo)
                .shippingAddressDetail(this.shippingAddressDetail)

                .orderMemo(this.orderMemo)

                .amount(this.amount)
                .savePoint(this.savePoint)
                .usePoint(this.usePoint)
                .cancelPoint(this.cancelPoint)
                .orgOrderNo(this.orgOrderNo)

                .approveNo(this.approveNo)
                .approveAmount(this.approveAmount)
                .cancelAmount(this.cancelAmount)
                .paymentMethod(this.paymentMethod)

                .tradeReqDatetime(this.tradeReqDatetime)
                .tradeState(this.tradeState)
                .tradeResult(tradeResult)
                .tradeResultMsg(tradeResultMsg)

                .createdId(this.createdId)
                .createdDatetime(this.createdDatetime)
                .modifiedId("ERROR")
                .modifiedDatetime(LocalDateTime.now())

                .build();
    }

    public OrderInfo updateOrderInfo() {
        return OrderInfo
                .builder()
                .seq(this.seq)
                .buyerCode(this.buyerCode)
                .orderIdentificationCode(this.orderIdentificationCode)

                .staffId(this.staffId)
                .staffName(this.staffName)
                .staffPhoneNo(this.staffPhoneNo)
                .staffEmail(this.staffEmail)

                .corpName(this.corpName)
                .corpTelNo(this.corpTelNo)
                .corpEmail(this.corpEmail)
                .orderNo(this.orderNo)
                .orderName(this.orderName)

                .orderInfoState(OrderStateEnum.getCodeByEnCode(this.orderInfoState))
                .shippingAddress(this.shippingAddress)
                .shippingAddressPostNo(this.shippingAddressPostNo)
                .shippingAddressDetail(this.shippingAddressDetail)

                .orderMemo(this.orderMemo)

                .amount(this.amount)
                .savePoint(this.savePoint)
                .usePoint(this.usePoint)
                .cancelPoint(this.cancelPoint)

                .orgOrderNo(this.orgOrderNo)
                .approveNo(this.approveNo)
                .approveAmount(this.approveAmount)
                .cancelAmount(this.cancelAmount)
                .paymentMethod(paymentMethod)

                .tradeReqDatetime(this.tradeReqDatetime)
                .tradeState(this.tradeState)
                .tradeResult(this.tradeResult)
                .tradeResultMsg(tradeResultMsg)
                .taxbillYn(this.taxbillYn)

                .createdId(this.createdId)
                .createdDatetime(this.createdDatetime)
                .modifiedId(SessionUtil.getUserCode())
                .modifiedDatetime(LocalDateTime.now())

                .build();
    }

    public OrderInfo updateOrderInfoForTaxbillYn() {
        return OrderInfo
                .builder()
                .seq(this.seq)
                .taxbillYn(this.taxbillYn)

                .modifiedId(SessionUtil.getUserCode())
                .modifiedDatetime(LocalDateTime.now())

                .build();
    }
}
