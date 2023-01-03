package kr.co.medicals.order.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.medicals.common.enums.OrderStateEnum;
import kr.co.medicals.common.util.SessionUtil;
import kr.co.medicals.file.domain.dto.FileManagerDto;
import kr.co.medicals.order.domain.entity.OrderInfo;
import kr.co.medicals.order.domain.entity.OrderItem;
import kr.co.medicals.product.domain.dto.BuyerTypeProductAmountDto;
import kr.co.medicals.product.domain.dto.ProductDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderItemDto {

    private Long seq;
    private OrderInfoDto orderInfo;
    private int itemSeq;
    private Long productSeq;
    private String standardCode;
    private String productName;
    private String productDisplayName;
    private String standard;
    private String factory;
    private int productAmount;
    private int orderItemAmount;
    private String orderItemState;
    private int pointSave;
    private int orderQuantity;
    private String deliveryCompany;
    private String deliveryNo;
    private String cancelYn;
    private String createdId;
    private String modifiedId;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifiedDatetime;
    private List<FileManagerDto> fileList = new ArrayList<>();

    public OrderItemDto() {
    }

    @Builder(builderClassName = "selectByEntity", builderMethodName = "selectByEntity")
    public OrderItemDto(OrderItem orderItem) {
        this.seq = orderItem.getSeq();
        this.itemSeq = orderItem.getItemSeq();
        this.orderInfo = OrderInfoDto.byCreate().seq(orderItem.getOrderInfo().getSeq()).build();
        this.productSeq = orderItem.getProductSeq();
        this.standardCode = orderItem.getStandardCode();
        this.productName = orderItem.getProductName();
        this.productDisplayName = orderItem.getProductDisplayName();
        this.standard = orderItem.getStandard();
        this.factory = orderItem.getFactory();
        this.productAmount = orderItem.getProductAmount();
        this.orderItemAmount = orderItem.getOrderItemAmount();
        this.orderItemState = OrderStateEnum.getEnCodeByCode(orderItem.getOrderItemState());
        this.pointSave = orderItem.getPointSave();
        this.orderQuantity = orderItem.getOrderQuantity();
        this.deliveryCompany = orderItem.getDeliveryCompany();
        this.deliveryNo = orderItem.getDeliveryNo();
        this.cancelYn = orderItem.getCancelYn();
        this.createdId = orderItem.getCreatedId();
        this.modifiedId = orderItem.getModifiedId();
        this.createdDatetime = orderItem.getCreatedDatetime();
        this.modifiedDatetime = orderItem.getModifiedDatetime();
    }


    @Builder(builderClassName = "selectByEntityOrderInfo", builderMethodName = "selectByEntityOrderInfo")
    public OrderItemDto(OrderItem orderItem, OrderInfo orderInfo) {
        this.seq = orderItem.getSeq();
        this.itemSeq = orderItem.getItemSeq();
        this.orderInfo = OrderInfoDto.selectByEntity().orderInfo(orderInfo).build();
        this.productSeq = orderItem.getProductSeq();
        this.standardCode = orderItem.getStandardCode();
        this.productName = orderItem.getProductName();
        this.productDisplayName = orderItem.getProductDisplayName();
        this.standard = orderItem.getStandard();
        this.factory = orderItem.getFactory();
        this.productAmount = orderItem.getProductAmount();
        this.orderItemAmount = orderItem.getOrderItemAmount();
        this.orderItemState = OrderStateEnum.getEnCodeByCode(orderItem.getOrderItemState());
        this.pointSave = orderItem.getPointSave();
        this.orderQuantity = orderItem.getOrderQuantity();
        this.deliveryCompany = orderItem.getDeliveryCompany();
        this.deliveryNo = orderItem.getDeliveryNo();
        this.cancelYn = orderItem.getCancelYn();
        this.createdId = orderItem.getCreatedId();
        this.modifiedId = orderItem.getModifiedId();
        this.createdDatetime = orderItem.getCreatedDatetime();
        this.modifiedDatetime = orderItem.getModifiedDatetime();
    }

    @Builder(builderClassName = "insertStandby", builderMethodName = "insertStandby")
    public OrderItemDto(int quantity, int itemAmount, int pointSave, ProductDto productDto, BuyerTypeProductAmountDto amountDto) {
        this.productSeq = productDto.getSeq();
        this.standardCode = productDto.getStandardCode();
        this.productName = productDto.getProductName();
        this.productDisplayName = productDto.getProductDisplayName();
        this.standard = productDto.getStandard();
        this.factory = productDto.getFactory();
        this.productAmount = amountDto.getAmount();
        this.orderItemAmount = itemAmount;
        this.pointSave = pointSave;
        this.orderQuantity = quantity;
    }

    public OrderItem insertEntity(Long orderSeq, int cnt) {
        return OrderItem
                .builder()
                .itemSeq(cnt)
                .orderInfo(OrderInfo.builder().seq(orderSeq).build())
                .productSeq(this.productSeq)
                .standardCode(this.standardCode)
                .productName(this.productName)
                .productDisplayName(this.productDisplayName)
                .standard(this.standard)
                .factory(this.factory)
                .orderItemAmount(this.orderItemAmount)
                .productAmount(this.productAmount)
                .pointSave(this.pointSave)
                .orderQuantity(this.orderQuantity)
                .orderItemState(OrderStateEnum.ORDER_STANDBY.getCode())
                .cancelYn("N")
                .createdId(SessionUtil.getUserCode())
                .createdDatetime(LocalDateTime.now())
                .build();
    }

    // 단순 상태 업데이트용으로만 사용.
    public OrderItem updateOrderStateEntity(String orderStateEnCode, String buyerIdentificationCode) {
        return OrderItem
                .builder()
                .seq(this.seq)
                .itemSeq(this.itemSeq)
                .orderInfo(OrderInfo.builder().seq(this.orderInfo.getSeq()).build())
                .productSeq(this.productSeq)
                .standardCode(this.standardCode)
                .productName(this.productName)
                .productDisplayName(this.productDisplayName)
                .standard(this.standard)
                .factory(this.factory)
                .productAmount(this.productAmount)
                .orderItemAmount(this.orderItemAmount)
                .orderItemState(OrderStateEnum.getCodeByEnCode(orderStateEnCode))
                .pointSave(this.pointSave)
                .orderQuantity(this.orderQuantity)
                .deliveryCompany(this.deliveryCompany)
                .deliveryNo(this.deliveryNo)
                .cancelYn(this.cancelYn)
                .createdId(this.createdId)
                .createdDatetime(this.createdDatetime)
                .modifiedId(buyerIdentificationCode)
                .modifiedDatetime(LocalDateTime.now())
                .build();
    }


}
