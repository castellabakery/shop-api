package kr.co.medicals.point.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.medicals.common.enums.PointStateEnum;
import kr.co.medicals.order.domain.dto.OrderItemDto;
import kr.co.medicals.order.domain.entity.OrderInfo;
import kr.co.medicals.order.domain.entity.OrderItem;
import kr.co.medicals.point.domain.entity.PointHistory;
import kr.co.medicals.product.domain.dto.ProductDto;
import kr.co.medicals.product.domain.entity.Product;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PointHistoryDto {

    private Long seq;
    private String buyerCode;
    private String orderNo;
    private String pointState;
    private int applyPoint;
    private int payAmount;
    private String content;
    private int beforeSavePoint;
    private int afterSavePoint;
    private int beforeSaveExpectPoint;
    private int afterSaveExpectPoint;
    private int beforeUsePoint;
    private int afterUsePoint;
    private String createdId;
    private LocalDateTime createdDatetime;

    // 추가
    private ProductDto productDto;
    private OrderItemDto orderItemDto;

    public PointHistoryDto(){
    }

    @Builder(builderClassName = "byCreate", builderMethodName = "byCreate")
    public PointHistoryDto(Long seq, String buyerCode, String orderNo, String pointState, int applyPoint, int payAmount, String content, int beforeSavePoint, int afterSavePoint, int beforeSaveExpectPoint, int afterSaveExpectPoint, int beforeUsePoint, int afterUsePoint, String createdId, LocalDateTime createdDatetime) {
        this.seq = seq;
        this.buyerCode = buyerCode;
        this.orderNo = orderNo;
        this.pointState = pointState;
        this.applyPoint = applyPoint;
        this.payAmount = payAmount;
        this.content = content;
        this.beforeSavePoint = beforeSavePoint;
        this.afterSavePoint = afterSavePoint;
        this.beforeSaveExpectPoint = beforeSaveExpectPoint;
        this.afterSaveExpectPoint = afterSaveExpectPoint;
        this.beforeUsePoint = beforeUsePoint;
        this.afterUsePoint = afterUsePoint;
        this.createdId = createdId;
        this.createdDatetime = createdDatetime;
    }

    @Builder(builderClassName = "selectByEntity", builderMethodName = "selectByEntity")
    public PointHistoryDto(PointHistory pointHistory) {
//        this.seq = pointHistory.getSeq();
        this.buyerCode = pointHistory.getBuyerCode();
        this.orderNo = pointHistory.getOrderNo();
        this.pointState = PointStateEnum.getEnCode(pointHistory.getPointState());
        this.applyPoint = pointHistory.getApplyPoint();
        this.payAmount = pointHistory.getPayAmount();
        this.content = pointHistory.getContent();
        this.beforeSavePoint = pointHistory.getBeforeSavePoint();
        this.afterSavePoint = pointHistory.getAfterSavePoint();
        this.beforeSaveExpectPoint = pointHistory.getBeforeSaveExpectPoint();
        this.afterSaveExpectPoint = pointHistory.getAfterSaveExpectPoint();
        this.beforeUsePoint = pointHistory.getBeforeUsePoint();
        this.afterUsePoint = pointHistory.getAfterUsePoint();
        this.createdId = pointHistory.getCreatedId();
        this.createdDatetime = pointHistory.getCreatedDatetime();
    }

    @Builder(builderClassName = "selectByEntity", builderMethodName = "selectByEntity")
    public PointHistoryDto(PointHistory pointHistory, OrderInfo orderInfo, OrderItem orderItem, Product product) {
        this.seq = pointHistory.getSeq();
        this.buyerCode = pointHistory.getBuyerCode();
        this.orderNo = pointHistory.getOrderNo();
        this.pointState = PointStateEnum.getEnCode(pointHistory.getPointState());
        this.applyPoint = pointHistory.getApplyPoint();
        this.payAmount = pointHistory.getPayAmount();
        this.content = pointHistory.getContent();
        this.beforeSavePoint = pointHistory.getBeforeSavePoint();
        this.afterSavePoint = pointHistory.getAfterSavePoint();
        this.beforeSaveExpectPoint = pointHistory.getBeforeSaveExpectPoint();
        this.afterSaveExpectPoint = pointHistory.getAfterSaveExpectPoint();
        this.beforeUsePoint = pointHistory.getBeforeUsePoint();
        this.afterUsePoint = pointHistory.getAfterUsePoint();
        this.createdId = pointHistory.getCreatedId();
        this.createdDatetime = pointHistory.getCreatedDatetime();
        this.productDto = ProductDto.selectByEntity().product(product).build();
        this.orderItemDto = OrderItemDto.selectByEntityOrderInfo().orderInfo(orderInfo).orderItem(orderItem).build();
    }

    public PointHistory insertEntity(String buyerIdentificationCode, int amount){
        return PointHistory
                .builder()
                .buyerCode(this.buyerCode)
                .orderNo(this.orderNo)
                .pointState(this.pointState)
                .applyPoint(this.applyPoint)
                .payAmount(amount)
                .content(this.content)
                .beforeSavePoint(this.beforeSavePoint)
                .afterSavePoint(this.afterSavePoint)
                .beforeSaveExpectPoint(this.beforeSaveExpectPoint)
                .afterSaveExpectPoint(this.afterSaveExpectPoint)
                .beforeUsePoint(this.beforeUsePoint)
                .afterUsePoint(this.afterUsePoint)
                .createdId(buyerIdentificationCode)
                .createdDatetime(LocalDateTime.now())
                .build();
    }

}
