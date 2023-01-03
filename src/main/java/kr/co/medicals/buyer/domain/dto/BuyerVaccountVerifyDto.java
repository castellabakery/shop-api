package kr.co.medicals.buyer.domain.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@RequiredArgsConstructor
public class BuyerVaccountVerifyDto {

    private String verifyWord;
    private int amount;
    private String orderNo;
    private int usePoint;

}
