package kr.co.medicals.buyer.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.medicals.common.util.Encrypt;
import kr.co.medicals.file.domain.dto.FileManagerDto;
import kr.co.medicals.terms.domain.dto.TmpBuyerTermsDto;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TmpBuyerDto {

    private Long seq;
    private Long buyerSeq;
    private String buyerCode;
    private String buyerIdentificationCode;
    private String state;

    private String buyerIdentificationId;
    private String buyerType;
    private String password;
    private String passwordChk;

    private String corpName;
    private String corpStaffName;
    private String corpAddress;
    private String addressPostNo;
    private String addressDetail;
    private String corpTelNo;
    private String corpFaxNo;
    private String corpNo;

    private String healthcareFacilityCode;
    private String staffName;
    private String staffPhoneNo;
    private String staffEmail;

    private String recommender;
    private String ci;
    private String pharmacyNo;

    private String corpEmail;
    private String corpShippingAddress;
    private String shippingAddressPostNo;
    private String shippingAddressDetail;
    private String rejectedMsg;
    private String createdId;
    private String modifiedId;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifiedDatetime;
    private List<FileManagerDto> fileList = new ArrayList<>();
    private List<TmpBuyerTermsDto> tmpBuyerTermsList;

    private String searchKeyword;

    public TmpBuyerDto(){

    }

    @Builder(builderClassName = "byCreate", builderMethodName = "byCreate")
    public TmpBuyerDto(Long seq, Long buyerSeq, String buyerCode, String buyerIdentificationCode, String state, String buyerIdentificationId, String buyerType, String password, String corpName, String corpStaffName, String corpAddress, String addressPostNo, String addressDetail, String corpTelNo, String corpFaxNo, String corpNo, String healthcareFacilityCode, String staffName, String staffPhoneNo, String staffEmail, String recommender, String ci, String pharmacyNo, String corpEmail, String corpShippingAddress, String shippingAddressPostNo, String shippingAddressDetail, String rejectedMsg, String createdId, String modifiedId, LocalDateTime createdDatetime, LocalDateTime modifiedDatetime, List<FileManagerDto> fileList, List<TmpBuyerTermsDto> tmpBuyerTermsList) {
        this.seq = seq;
        this.buyerSeq = buyerSeq;
        this.buyerCode = buyerCode;
        this.buyerIdentificationCode = buyerIdentificationCode;
        this.state = state;
        this.buyerIdentificationId = buyerIdentificationId;
        this.buyerType = buyerType;
        this.password = password;
        this.corpName = corpName;
        this.corpStaffName = corpStaffName;
        this.corpAddress = corpAddress;
        this.addressPostNo = addressPostNo;
        this.addressDetail = addressDetail;
        this.corpTelNo = corpTelNo;
        this.corpFaxNo = corpFaxNo;
        this.corpNo = corpNo;
        this.healthcareFacilityCode = healthcareFacilityCode;
        this.staffName = staffName;
        this.staffPhoneNo = staffPhoneNo;
        this.staffEmail = staffEmail;
        this.recommender = recommender;
        this.ci = ci;
        this.pharmacyNo = pharmacyNo;
        this.corpEmail = corpEmail;
        this.corpShippingAddress = corpShippingAddress;
        this.shippingAddressPostNo = shippingAddressPostNo;
        this.shippingAddressDetail = shippingAddressDetail;
        this.rejectedMsg = rejectedMsg;
        this.createdId = createdId;
        this.modifiedId = modifiedId;
        this.createdDatetime = createdDatetime;
        this.modifiedDatetime = modifiedDatetime;
        this.fileList = fileList;
        this.tmpBuyerTermsList = tmpBuyerTermsList;
    }

    public TmpBuyerDto encryptPassword() {
        if (!ObjectUtils.isEmpty(password)) {
            this.password = Encrypt.sha256(password);
        }
        return this;
    }

}
