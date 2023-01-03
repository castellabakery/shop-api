package kr.co.medicals.sales;

import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ObjectMapperUtil;
import kr.co.medicals.order.OrderService;
import kr.co.medicals.order.domain.dto.OrderInfoDto;
import kr.co.medicals.sales.domain.SalesDto;
import kr.co.medicals.sales.domain.SalesSearchDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class SalesService {

    private final OrderService orderService;

    @Autowired
    public SalesService(OrderService orderService) {
        this.orderService = orderService;
    }

    private SalesSearchDto salesApiRequest(ApiRequest apiRequest) {

        SalesSearchDto searchDto = new SalesSearchDto();

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (map.containsKey("paymentMethod")) {
            searchDto.setPaymentMethod(map.get("paymentMethod").toString());
        }

        if (map.containsKey("startDate") && map.containsKey("endDate")) {
            searchDto.setStartDate(map.get("startDate").toString());
            searchDto.setEndDate(map.get("endDate").toString());
        }

        return searchDto;
    }

    private SalesDto changeToHeader(List<OrderInfoDto> orderInfoList) {

        Long totalAmount = 0L;
        Long totalApproveAmount = 0L;
        Long totalCancelPoint = 0L;
        Long totalCancelAmount = 0L;
        Long totalUsePoint = 0L;

        for (OrderInfoDto dto : orderInfoList) {

//            int getAmount = dto.getAmount(); // 주문한 금액
//            int getApproveAmount = dto.getApproveAmount(); // 카드 또는  결제 금액
//            int getCancelAmount = dto.getCancelAmount(); // 카드 또는  취소 금액
//            int getCancelPoint = dto.getCancelPoint(); // 포인트 취소금액

            totalAmount = totalAmount + dto.getAmount(); // 주문한 금액
            totalApproveAmount = totalApproveAmount + dto.getApproveAmount(); // 카드 또는  결제 금액
            totalCancelPoint = totalCancelPoint + dto.getCancelPoint(); // 취소 포인트
            totalUsePoint = totalUsePoint + dto.getUsePoint(); // 사용 포인트
            totalCancelAmount = totalCancelAmount + dto.getCancelAmount(); // 취소 된 카드 또는  금액

        }

        return SalesDto
                .builder()
                .totalAmount(totalAmount)
                .totalApproveAmount(totalApproveAmount)
                .totalCancelPoint(totalCancelPoint)
                .totalCancelAmount(totalCancelAmount)
                .totalUsePoint(totalUsePoint)
                .amount(totalAmount - totalCancelAmount - totalCancelPoint)
                .build();
    }

    public Page<OrderInfoDto> getSalesList(ApiRequest apiRequest) {
        SalesSearchDto searchDto = this.salesApiRequest(apiRequest);
        Pageable pageable = PageRequest.of(apiRequest.getPage(), apiRequest.getPageSize());
        return orderService.searchSalesList(searchDto, pageable);
    }

    public SalesDto getSalesHeader(ApiRequest apiRequest) {
        SalesSearchDto searchDto = this.salesApiRequest(apiRequest);
        List<OrderInfoDto> orderInfoList = orderService.searchSalesHeaderList(searchDto);
        return this.changeToHeader(orderInfoList);
    }

}
