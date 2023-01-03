package kr.co.medicals.buyer.domain.entity;

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
@Table(name = "SHIPPING_ADDRESS")
public class ShippingAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;
    private String buyerCode;
    private int addressSeq;
    private String address;
    private String addressPostNo;
    private String addressDetail;
    private String addressName;
    private String addressTel;
    private String delYn;
    private String createdId;
    private LocalDateTime createdDatetime;
    private String modifiedId;
    private LocalDateTime modifiedDatetime;

    @Builder
    public ShippingAddress(Long seq, String buyerCode, int addressSeq, String address, String addressPostNo, String addressDetail, String addressName, String addressTel, String delYn, String createdId, LocalDateTime createdDatetime, String modifiedId, LocalDateTime modifiedDatetime) {
        this.seq = seq;
        this.buyerCode = buyerCode;
        this.addressSeq = addressSeq;
        this.addressName = addressName;
        this.addressTel = addressTel;
        this.address = address;
        this.addressPostNo = addressPostNo;
        this.addressDetail = addressDetail;
        this.delYn = delYn;
        this.createdId = createdId;
        this.createdDatetime = createdDatetime;
        this.modifiedId = modifiedId;
        this.modifiedDatetime = modifiedDatetime;
    }

}
