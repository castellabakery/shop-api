package kr.co.medicals.sales.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SalesDto {

    // 총 매출
    private Long totalAmount;
    // 총 현금
    private Long totalApproveAmount;
    // 취소 된 총 포인트
    private Long totalCancelPoint;
    // 취소된 총 현금
    private Long totalCancelAmount;
    // 사용된 총 포인트
    private Long totalUsePoint;

    private Long amount; // 매출 - 취소

    public SalesDto() {
    }

    @Builder
    public SalesDto(Long totalAmount, Long totalApproveAmount, Long totalCancelPoint, Long totalCancelAmount, Long totalUsePoint, Long amount) {
        this.totalAmount = totalAmount;
        this.totalApproveAmount = totalApproveAmount;
        this.totalCancelPoint = totalCancelPoint;
        this.totalCancelAmount = totalCancelAmount;
        this.totalUsePoint = totalUsePoint;
        this.amount = amount;
    }




}
