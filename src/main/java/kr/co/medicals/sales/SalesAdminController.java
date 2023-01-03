package kr.co.medicals.sales;

import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/sales")
public class SalesAdminController {


    private final SalesService salesService;

    @Autowired
    public SalesAdminController(SalesService salesService) {
        this.salesService = salesService;
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse> getSalesListAdmin(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(ApiResponse.success(salesService.getSalesList(apiRequest)));
    }

    @PostMapping("/header")
    public ResponseEntity<ApiResponse> getSalesHeaderAdmin(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(ApiResponse.success(salesService.getSalesHeader(apiRequest)));
    }

}
