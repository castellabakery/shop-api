package kr.co.medicals.product.controller;

import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ApiResponse;
import kr.co.medicals.product.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/buyer/product")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }


    /**
     * 사용자 - 리스트 조회
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/list")
    public ResponseEntity<ApiResponse> getProductList(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductListAddRole(apiRequest)));
    }

    /**
     * 사용자 - 상품 단건 조회.
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/detail")
    public ResponseEntity<ApiResponse> getProduct(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductByRole(apiRequest, "Y")));
    }

}
