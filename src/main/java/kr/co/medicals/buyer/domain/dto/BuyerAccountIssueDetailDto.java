package kr.co.medicals.buyer.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuyerAccountIssueDetailDto {

    private String vaccountGuid;
    private String adminApproveYn;
    private String withdrawTid;
    private String accountAuthYn;
    private String userType;
    private String phoneNo;
}
