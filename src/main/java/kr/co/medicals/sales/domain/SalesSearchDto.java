package kr.co.medicals.sales.domain;

import kr.co.medicals.common.enums.OrderStateEnum;
import kr.co.medicals.common.util.DateFormatUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.ObjectUtils;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ToString
public class SalesSearchDto {

    private String startDate = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).format(DateFormatUtils.DASH_DATE);
    private String endDate = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).format(DateFormatUtils.DASH_DATE);
    private String paymentMethod = "";
    private List<Integer> orderStateList = Arrays.asList(OrderStateEnum.PAY_DONE.getCode());

    public SalesSearchDto(){}

    public void setPaymentMethod(String paymentMethod) {
        if(!ObjectUtils.isEmpty(paymentMethod)){
            this.paymentMethod = paymentMethod;
        }
    }
}
