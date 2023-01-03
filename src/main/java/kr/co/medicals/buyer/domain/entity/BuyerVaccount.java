package kr.co.medicals.buyer.domain.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@ToString
@Entity
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class BuyerVaccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;

    @Column(name = "buyer_code", length = 20, nullable = false)
    private String buyerCode;

    @Column(name = "vaccount_guid", length = 50, nullable = false)
    private String vaccountGuid;
    @Column(name = "vaccount_withdraw_tid", length = 50)
    @Setter
    private String vaccountWithdrawTid;

    @CreatedDate
    private LocalDateTime createdDatetime;

    @LastModifiedDate
    private LocalDateTime modifiedDatetime;

    @Builder
    public BuyerVaccount(Long seq, String buyerCode, String vaccountGuid, String vaccountWithdrawTid, LocalDateTime createdDatetime, LocalDateTime modifiedDatetime) {
        this.seq = seq;
        this.buyerCode = buyerCode;
        this.vaccountGuid = vaccountGuid;
        this.vaccountWithdrawTid = vaccountWithdrawTid;
        this.createdDatetime = createdDatetime;
        this.modifiedDatetime = modifiedDatetime;
    }
}
