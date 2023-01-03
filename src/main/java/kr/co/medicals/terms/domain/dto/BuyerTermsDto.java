package kr.co.medicals.terms.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.medicals.common.util.SessionUtil;
import kr.co.medicals.terms.domain.entity.BuyerTerms;
import kr.co.medicals.terms.domain.entity.Terms;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BuyerTermsDto {

    private Long seq;
    private String buyerCode;
    private TermsDto terms;
    private String agreeYn;
    private String delYn;
    private String createdId;
    private LocalDateTime createdDatetime;
    private String modifiedId;
    private LocalDateTime modifiedDatetime;

    public BuyerTermsDto(){
    }

    @Builder(builderClassName = "byCreate", builderMethodName = "byCreate")
    public BuyerTermsDto(Long seq, String buyerCode, TermsDto terms, String agreeYn, String delYn, String createdId, String modifiedId, LocalDateTime createdDatetime, LocalDateTime modifiedDatetime) {
        this.seq = seq;
        this.buyerCode = buyerCode;
        this.terms = terms;
        this.agreeYn = agreeYn;
        this.delYn = delYn;
        this.createdId = createdId;
        this.createdDatetime = createdDatetime;
        this.modifiedId = modifiedId;
        this.modifiedDatetime = modifiedDatetime;
    }

    @Builder(builderClassName = "selectByEntity", builderMethodName = "selectByEntity")
    public BuyerTermsDto(BuyerTerms buyerTerms, Terms terms) {
        this.seq = buyerTerms.getSeq();
        this.buyerCode = buyerTerms.getBuyerCode();
        this.terms = TermsDto.selectByEntity().terms(terms).build();
        this.agreeYn = buyerTerms.getAgreeYn();
        this.delYn = buyerTerms.getDelYn();
        this.createdId = buyerTerms.getCreatedId();
        this.createdDatetime = buyerTerms.getCreatedDatetime();
        this.modifiedId = buyerTerms.getModifiedId();
        this.modifiedDatetime = buyerTerms.getModifiedDatetime();
    }

    public BuyerTerms insertEntity(){
        return BuyerTerms
                .builder()
                .buyerCode(this.buyerCode)
                .terms(Terms.builder().seq(this.terms.getSeq()).build())
                .agreeYn(this.agreeYn)
                .delYn("N")
                .createdId(SessionUtil.getUserCode())
                .createdDatetime(LocalDateTime.now())
                .build();
    }

    public BuyerTerms updateEntity(){
        return BuyerTerms
                .builder()
                .seq(this.seq)
                .buyerCode(this.buyerCode)
                .terms(Terms.builder().seq(this.terms.getSeq()).build())
                .agreeYn(this.agreeYn)
                .delYn(this.delYn)
                .createdId(this.createdId)
                .createdDatetime(this.createdDatetime)
                .modifiedId(SessionUtil.getUserCode())
                .modifiedDatetime(LocalDateTime.now())
                .build();
    }

}
