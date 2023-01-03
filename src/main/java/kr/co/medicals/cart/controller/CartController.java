package kr.co.medicals.cart.controller;

import kr.co.medicals.cart.CartService;
import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ApiResponse;
import kr.co.medicals.common.util.ObjectMapperUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/buyer/cart")
public class CartController {

    private final CartService cartService;

    @Autowired
    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse> getCartList() {
        return ResponseEntity.ok(ApiResponse.success(cartService.myCartList()));
    }

    @PostMapping("/add-modify")
    public ResponseEntity<ApiResponse> addCart(@RequestBody ApiRequest apiRequest) {
        cartService.addModifyCart(ObjectMapperUtil.requestGetMap(apiRequest));
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse> deleteCart(@RequestBody ApiRequest apiRequest) { // 장바구니 시퀀스
        cartService.deleteCart(apiRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
