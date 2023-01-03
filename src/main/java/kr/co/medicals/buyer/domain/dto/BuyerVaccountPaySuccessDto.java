package kr.co.medicals.buyer.domain.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BuyerVaccountPaySuccessDto {

    private final String tid;
    private final String orderNo;

}
