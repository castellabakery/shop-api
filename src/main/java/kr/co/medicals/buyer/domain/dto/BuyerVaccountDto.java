package kr.co.medicals.buyer.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuyerVaccountDto {

    private String phoneNo;
    private String vaccount;
    private String vaccountBankName;
    private String vaccountGuid;
    private String withdrawAccount;
    private String withdrawAccountBankName;
    private int balance;
}
