package kr.co.medicals.buyer.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuyerVaccountWithdrawDto {

    private String buyerCode;
    private String accountBankCode;
    private String accountNo;
    private String birth;
    private String accountHolder;

}
