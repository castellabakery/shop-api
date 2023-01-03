package kr.co.medicals.buyer.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.medicals.terms.domain.dto.BuyerTermsDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BuyerDto {

    private Long seq;
    private String buyerCode;
    private String buyerType;
    private String buyerState;
    private String healthcareFacilityCode;
    private String pharmacyNo;
    private String corpStaffName;
    private String corpTelNo;
    private String corpPhoneNo;
    private String corpFaxNo;
    private String corpEmail;
    private String corpName;
    private String corpNo;
    private String corpAddress;
    private String addressPostNo;
    private String addressDetail;
    private String corpShippingAddress;
    private String shippingAddressPostNo;
    private String shippingAddressDetail;
    private String ci;
    private String recommender;
    private String createdId;
    private String modifiedId;
    private LocalDateTime createdDatetime;
    private LocalDateTime approveDatetime;
    private LocalDateTime modifiedDatetime;
    private List<BuyerTermsDto> buyerTermsList;

    public BuyerDto() {
    }

}
