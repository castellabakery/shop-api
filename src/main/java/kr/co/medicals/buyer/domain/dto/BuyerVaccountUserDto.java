package kr.co.medicals.buyer.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuyerVaccountUserDto {

    private String buyerCode;
    private String birth;
    private String accountHolder;
    private String userType;
    private String bizNumber;
    private String bizName;
    private String ceoName;
    private String bizTel;
    private String phoneNo;

}
