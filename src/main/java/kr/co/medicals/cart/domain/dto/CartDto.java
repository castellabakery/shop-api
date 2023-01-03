package kr.co.medicals.cart.domain.dto;

import kr.co.medicals.cart.domain.entity.Cart;
import kr.co.medicals.common.util.SessionUtil;
import kr.co.medicals.file.domain.dto.FileManagerDto;
import kr.co.medicals.product.domain.dto.BuyerTypeProductAmountDto;
import kr.co.medicals.product.domain.dto.ProductDto;
import kr.co.medicals.product.domain.entity.BuyerTypeProductAmount;
import kr.co.medicals.product.domain.entity.Product;
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
public class CartDto {
    private Long seq;
    private String buyerCode;
    private ProductDto product;
    private List<FileManagerDto> fileManagerDto = new ArrayList<>();
    private int quantity;
    private BuyerTypeProductAmountDto buyerTypeProductAmount;
    private String delYn;
    private String createdId;
    private String modifiedId;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifiedDatetime;

    public CartDto() {
    }

    @Builder(builderClassName = "entityConvertDto", builderMethodName = "entityConvertDto")
    public CartDto(Cart cart) {
        this.seq = cart.getSeq();
        this.buyerCode = cart.getBuyerCode();
        this.product = ProductDto.byCreate().seq(cart.getProduct().getSeq()).build();
        this.quantity = cart.getQuantity();
        this.createdId = cart.getCreatedId();
        this.modifiedId = cart.getModifiedId();
        this.createdDatetime = cart.getCreatedDatetime();
        this.modifiedDatetime = cart.getModifiedDatetime();
    }

    @Builder(builderClassName = "selectByEntity", builderMethodName = "selectByEntity")
    public CartDto(Cart cart, Product product, BuyerTypeProductAmount buyerTypeProductAmount) {
        this.seq = cart.getSeq();
        this.buyerCode = cart.getBuyerCode();
        this.product = ProductDto.selectByEntity().product(product).build();
        this.quantity = cart.getQuantity();
        this.buyerTypeProductAmount = BuyerTypeProductAmountDto.selectByEntity().buyerTypeProductAmount(buyerTypeProductAmount).build();
        this.delYn = cart.getDelYn();
        this.createdId = cart.getCreatedId();
        this.modifiedId = cart.getModifiedId();
        this.createdDatetime = cart.getCreatedDatetime();
        this.modifiedDatetime = cart.getModifiedDatetime();
    }

    public Cart insertEntity(String buyerCode, Long productSeq, int quantity) {
        return Cart
                .builder()
                .buyerCode(buyerCode)
                .product(Product.builder().seq(productSeq).build())
                .quantity(quantity)
                .delYn("N")
                .createdId(SessionUtil.getUserCode())
                .createdDatetime(LocalDateTime.now())
                .build();
    }

    public Cart updateQuantity(int quantity) {
        return Cart
                .builder()
                .seq(this.seq)
                .buyerCode(this.buyerCode)
                .product(Product.builder().seq(this.product.getSeq()).build())
                .quantity(quantity)
                .delYn(this.delYn)
                .createdId(this.createdId)
                .createdDatetime(this.createdDatetime)
                .modifiedId(SessionUtil.getUserCode())
                .modifiedDatetime(LocalDateTime.now())
                .build();
    }

    public Cart updateDelY(String buyerIdentificationCode) {
        return Cart
                .builder()
                .seq(this.seq)
                .buyerCode(this.buyerCode)
                .product(Product.builder().seq(this.product.getSeq()).build())
                .quantity(this.quantity)
                .delYn("Y")
                .createdId(this.createdId)
                .createdDatetime(this.createdDatetime)
                .modifiedId(buyerIdentificationCode)
                .modifiedDatetime(LocalDateTime.now())
                .build();
    }
}
