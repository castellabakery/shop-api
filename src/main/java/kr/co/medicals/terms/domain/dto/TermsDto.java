package kr.co.medicals.terms.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.medicals.common.util.SessionUtil;
import kr.co.medicals.common.util.ObjectCheck;
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
public class TermsDto {

    private Long seq;
    private String title;
    private String content;
    private String optionYn;
    private String createdId;
    private LocalDateTime createdDatetime;
    private String modifiedId;
    private LocalDateTime modifiedDatetime;

    public TermsDto() {
    }

    @Builder(builderClassName = "byCreate", builderMethodName = "byCreate")
    public TermsDto(Long seq, String title, String content, String optionYn, String createdId, String modifiedId, LocalDateTime createdDatetime, LocalDateTime modifiedDatetime) {
        this.seq = seq;
        this.title = title;
        this.content = content;
        this.optionYn = optionYn;
        this.createdId = createdId;
        this.createdDatetime = createdDatetime;
        this.modifiedId = modifiedId;
        this.modifiedDatetime = modifiedDatetime;
    }

    @Builder(builderClassName = "selectByEntity", builderMethodName = "selectByEntity")
    public TermsDto(Terms terms) {
        this.seq = terms.getSeq();
        this.title = terms.getTitle();
        this.content = terms.getContent();
        this.optionYn = terms.getOptionYn();
        this.createdId = terms.getCreatedId();
        this.createdDatetime = terms.getCreatedDatetime();
        this.modifiedId = terms.getModifiedId();
        this.modifiedDatetime = terms.getModifiedDatetime();
    }

    public Terms insertEntity() {
        this.checkInsertRequire();
        return Terms
                .builder()
                .title(this.title)
                .content(this.content)
                .optionYn(this.optionYn)
                .createdId(SessionUtil.getUserCode())
                .createdDatetime(LocalDateTime.now())
                .build();
    }

    public Terms updateEntity(TermsDto newDto) {
        this.setUpdate(newDto);
        return Terms
                .builder()
                .seq(this.seq)
                .title(this.title)
                .content(this.content)
                .optionYn(this.optionYn)
                .createdId(this.createdId)
                .createdDatetime(this.createdDatetime)
                .modifiedId(SessionUtil.getUserCode())
                .modifiedDatetime(LocalDateTime.now())
                .build();
    }

    public TmpBuyerTermsDto convertTmpBuyerTerms(String agreeYn){
        return TmpBuyerTermsDto
                .byCreate()
                .terms(TermsDto.byCreate().seq(this.seq).build())
                .agreeYn(agreeYn)
                .build();
    }

    private void checkInsertRequire() {
        ObjectCheck.isBlankStringException(this.title, "title");
        ObjectCheck.isBlankStringException(this.content, "content");
        ObjectCheck.checkYnException(this.optionYn, "optionYn");
    }

    public void checkUpdateRequire() {
        ObjectCheck.isBlankLongException(this.seq, "seq");
        ObjectCheck.isBlankStringException(this.title, "title");
        ObjectCheck.isBlankStringException(this.content, "content");
        ObjectCheck.checkYnException(this.optionYn, "optionYn");
    }

    private void setUpdate(TermsDto newDto) {
        this.title = newDto.getTitle();
        this.content = newDto.getContent();
        this.optionYn = newDto.getOptionYn();
    }

}
