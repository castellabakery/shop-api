package kr.co.medicals.buyer.domain.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuyerVaccountMerchantDto {

    private String buyerIdentificationId;
    private String buyerCode;

    @Builder
    public BuyerVaccountMerchantDto(String buyerIdentificationId, String buyerCode) {
        this.buyerIdentificationId = buyerIdentificationId;
        this.buyerCode = buyerCode;
    }
}
