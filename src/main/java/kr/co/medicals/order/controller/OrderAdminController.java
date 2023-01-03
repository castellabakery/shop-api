package kr.co.medicals.order.controller;

import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ApiResponse;
import kr.co.medicals.order.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/buyer/order")
public class OrderAdminController {

    private final OrderService orderService;

    @Autowired
    public OrderAdminController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse> searchOrderItemListAll(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(ApiResponse.success(orderService.searchOrderItemList(apiRequest)));
    }

    @PostMapping("/detail")
    public ResponseEntity<ApiResponse> getOrderDetail(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderDetail(apiRequest)));
    }

    @PostMapping("/modify/info")
    public ResponseEntity<ApiResponse> modifyOrderState(@RequestBody ApiRequest apiRequest) {
        orderService.modifyOrderInfoByAdmin(apiRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
