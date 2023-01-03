package kr.co.medicals.terms.domain.entity;

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
@Table(name = "TMP_BUYER_TERMS")
public class TmpBuyerTerms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;
    private Long tmpSeq;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TERMS_SEQ", nullable = false)
    private Terms terms;
    private String agreeYn;
    private String delYn;
    private String createdId;
    private LocalDateTime createdDatetime;
    private String modifiedId;
    private LocalDateTime modifiedDatetime;

    @Builder
    public TmpBuyerTerms(Long seq, Long tmpSeq, Terms terms, String agreeYn, String delYn, String createdId, LocalDateTime createdDatetime, String modifiedId, LocalDateTime modifiedDatetime) {
        this.seq = seq;
        this.tmpSeq = tmpSeq;
        this.terms = terms;
        this.agreeYn = agreeYn;
        this.delYn = delYn;
        this.createdId = createdId;
        this.createdDatetime = createdDatetime;
        this.modifiedId = modifiedId;
        this.modifiedDatetime = modifiedDatetime;
    }
}
