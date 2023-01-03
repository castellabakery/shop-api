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
@Table(name = "ORDER_ITEM")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_INFO_SEQ", nullable = false)
    private OrderInfo orderInfo;
    private int itemSeq;
    private Long productSeq;
    private String standardCode;
    private String productName;
    private String productDisplayName;
    private String standard;
    private String factory;
    private int productAmount;
    private int orderItemAmount;
    private int orderItemState;
    private int pointSave;
    private int orderQuantity;
    private String deliveryCompany;
    private String deliveryNo;
    private String cancelYn;
    private String createdId;
    private String modifiedId;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifiedDatetime;

    @Builder
    public OrderItem(Long seq, int itemSeq, OrderInfo orderInfo, Long productSeq, String standardCode, String productName, String productDisplayName, String standard, String factory, int productAmount, int orderItemAmount, int orderItemState, int pointSave, int orderQuantity, String deliveryCompany, String deliveryNo, String cancelYn, String createdId, String modifiedId, LocalDateTime createdDatetime, LocalDateTime modifiedDatetime) {
        this.seq = seq;
        this.itemSeq = itemSeq;
        this.orderInfo = orderInfo;
        this.productSeq = productSeq;
        this.standardCode = standardCode;
        this.productName = productName;
        this.productDisplayName = productDisplayName;
        this.standard = standard;
        this.factory = factory;
        this.productAmount = productAmount;
        this.orderItemAmount = orderItemAmount;
        this.orderItemState = orderItemState;
        this.pointSave = pointSave;
        this.orderQuantity = orderQuantity;
        this.deliveryCompany = deliveryCompany;
        this.deliveryNo = deliveryNo;
        this.cancelYn = cancelYn;
        this.createdId = createdId;
        this.modifiedId = modifiedId;
        this.createdDatetime = createdDatetime;
        this.modifiedDatetime = modifiedDatetime;
    }

}
