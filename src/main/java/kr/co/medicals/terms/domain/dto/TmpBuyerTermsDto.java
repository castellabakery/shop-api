package kr.co.medicals.terms.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.medicals.common.util.ObjectCheck;
import kr.co.medicals.common.util.SessionUtil;
import kr.co.medicals.terms.domain.entity.Terms;
import kr.co.medicals.terms.domain.entity.TmpBuyerTerms;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmpBuyerTermsDto {

    private Long seq;
    private Long tmpSeq;
    private TermsDto terms;
    private String agreeYn;
    private String delYn;
    private String createdId;
    private LocalDateTime createdDatetime;
    private String modifiedId;
    private LocalDateTime modifiedDatetime;

    public TmpBuyerTermsDto() {
    }


    @Builder(builderClassName = "byCreate", builderMethodName = "byCreate")
    public TmpBuyerTermsDto(Long seq, Long tmpSeq, TermsDto terms, String agreeYn, String delYn, String createdId, LocalDateTime createdDatetime, String modifiedId, LocalDateTime modifiedDatetime) {
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

    @Builder(builderClassName = "selectByEntity", builderMethodName = "selectByEntity")
    public TmpBuyerTermsDto(TmpBuyerTerms tmpBuyerTerms, Terms terms) {
        this.seq = tmpBuyerTerms.getSeq();
        this.tmpSeq = tmpBuyerTerms.getTmpSeq();
        this.terms = TermsDto.selectByEntity().terms(terms).build();
        this.agreeYn = tmpBuyerTerms.getAgreeYn();
        this.delYn = tmpBuyerTerms.getDelYn();
        this.createdId = tmpBuyerTerms.getCreatedId();
        this.createdDatetime = tmpBuyerTerms.getCreatedDatetime();
        this.modifiedId = tmpBuyerTerms.getModifiedId();
        this.modifiedDatetime = tmpBuyerTerms.getModifiedDatetime();
    }

    public TmpBuyerTerms insertEntity(Long tmpSeq, String buyerIdentificationCode) {

        ObjectCheck.isBlankLongException(tmpSeq, "TMP_SEQ");
        ObjectCheck.checkYnException(this.agreeYn, "AGREE_YN");

        return TmpBuyerTerms
                .builder()
                .tmpSeq(tmpSeq)
                .terms(Terms.builder().seq(this.terms.getSeq()).build())
                .agreeYn(this.agreeYn)
                .delYn("N")
                .createdId(buyerIdentificationCode)
                .createdDatetime(LocalDateTime.now())
                .build();
    }

    public BuyerTermsDto convertBuyerDto(String buyerCode) {
        return BuyerTermsDto
                .byCreate()
                .buyerCode(buyerCode)
                .terms(TermsDto.byCreate().seq(this.terms.getSeq()).build())
                .agreeYn(this.agreeYn)
                .build();
    }

    public TmpBuyerTerms updateUseYnEntity(){
        return TmpBuyerTerms
                .builder()
                .seq(this.seq)
                .tmpSeq(this.tmpSeq)
                .terms(Terms.builder().seq(this.terms.getSeq()).build())
                .agreeYn(this.agreeYn)
                .delYn("Y")
                .createdId(this.createdId)
                .createdDatetime(this.createdDatetime)
                .modifiedId(SessionUtil.getUserCode())
                .modifiedDatetime(LocalDateTime.now())
                .build();

    }

}
