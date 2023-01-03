package kr.co.medicals.product.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.medicals.common.util.SessionUtil;
import kr.co.medicals.product.domain.entity.BuyerTypeProductAmount;
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
public class BuyerTypeProductAmountDto {
    private Long seq;
    private ProductDto product;
    private String buyerType;
    private int amount;
    private int originPoint;
    private String savePointType;
    private int savePoint;
    private String delYn;
    private String createdId;
    private String modifiedId;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifiedDatetime;

    public BuyerTypeProductAmountDto() {
    }

    @Builder(builderClassName = "selectByEntity", builderMethodName = "selectByEntity")
    public BuyerTypeProductAmountDto(BuyerTypeProductAmount buyerTypeProductAmount) {
        this.seq = buyerTypeProductAmount.getSeq();
//        this.product = ProductDto.byCreate().seq(buyerTypeProductAmount.getProduct().getSeq()).build();
        this.buyerType = buyerTypeProductAmount.getBuyerType();
        this.amount = buyerTypeProductAmount.getAmount();
        this.originPoint = buyerTypeProductAmount.getOriginPoint();
        this.savePointType = buyerTypeProductAmount.getSavePointType();
        this.savePoint = buyerTypeProductAmount.getSavePoint();
//        this.delYn = buyerTypeProductAmount.getDelYn();
        this.createdId = buyerTypeProductAmount.getCreatedId();
        this.modifiedId = buyerTypeProductAmount.getModifiedId();
        this.createdDatetime = buyerTypeProductAmount.getCreatedDatetime();
        this.modifiedDatetime = buyerTypeProductAmount.getModifiedDatetime();
    }

    public BuyerTypeProductAmount insertAmount() {
        return BuyerTypeProductAmount
                .builder()
                .product(Product.builder().seq(this.product.getSeq()).build())
                .buyerType(this.buyerType)
                .amount(this.amount)
                .originPoint(this.originPoint)
                .savePointType(this.savePointType)
                .savePoint(this.savePoint)
                .delYn("N")
                .createdId(SessionUtil.getUserCode())
                .createdDatetime(LocalDateTime.now())
                .build();
    }

    public BuyerTypeProductAmount updateEntity() {
        return BuyerTypeProductAmount
                .builder()
                .seq(this.seq)
                .product(Product.builder().seq(this.product.getSeq()).build())
                .buyerType(this.buyerType)
                .amount(this.amount)
                .originPoint(this.originPoint)
                .savePoint(this.savePoint)
                .savePointType(this.savePointType)
                .delYn(this.delYn)
                .createdId(this.createdId)
                .createdDatetime(this.createdDatetime)
                .modifiedId(SessionUtil.getUserCode())
                .modifiedDatetime(LocalDateTime.now())
                .build();
    }

}
