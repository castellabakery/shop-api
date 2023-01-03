package kr.co.medicals.buyer.controller;

import kr.co.medicals.buyer.ShippingAddressService;
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
@RequestMapping("/buyer/shipping/address/list")
public class ShippingAddressController {

    private final ShippingAddressService shippingAddressService;

    @Autowired
    public ShippingAddressController(ShippingAddressService shippingAddressService) {
        this.shippingAddressService = shippingAddressService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> getShippingAddress() {
        return ResponseEntity.ok(ApiResponse.success(shippingAddressService.getShippingAddress()));
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addShippingAddress(@RequestBody ApiRequest apiRequest) {
        shippingAddressService.addShippingAddress(apiRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/modify")
    public ResponseEntity<ApiResponse> modifyShippingAddress(@RequestBody ApiRequest apiRequest) {
        shippingAddressService.modifyShippingAddress(apiRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }


    @PostMapping("/delete")
    public ResponseEntity<ApiResponse> deleteShippingAddress(@RequestBody ApiRequest apiRequest) {
        shippingAddressService.deleteShippingAddress(apiRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
