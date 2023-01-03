package kr.co.medicals.product.domain.entity;

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
@Table(name = "BUYER_TYPE_PRODUCT_AMOUNT")
public class BuyerTypeProductAmount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_SEQ", nullable = false)
    private Product product;
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

    @Builder
    public BuyerTypeProductAmount(Long seq, Product product, String buyerType, int amount, int originPoint, String savePointType, int savePoint, String delYn, String createdId, String modifiedId, LocalDateTime createdDatetime, LocalDateTime modifiedDatetime) {
        this.seq = seq;
        this.product = product;
        this.buyerType = buyerType;
        this.amount = amount;
        this.originPoint = originPoint;
        this.savePointType = savePointType;
        this.savePoint = savePoint;
        this.delYn = delYn;
        this.createdId = createdId;
        this.modifiedId = modifiedId;
        this.createdDatetime = createdDatetime;
        this.modifiedDatetime = modifiedDatetime;
    }
}
