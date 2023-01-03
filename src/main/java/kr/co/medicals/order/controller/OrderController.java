package kr.co.medicals.order.controller;

import kr.co.medicals.common.constants.PropertiesConstants;
import kr.co.medicals.common.enums.OrderStateEnum;
import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ApiResponse;
import kr.co.medicals.order.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/buyer/order")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 사용자 주문서 API. 상품상세 -> 바로구매시 사용.
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/direct")
    public ResponseEntity<ApiResponse> directOrder(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(ApiResponse.success(orderService.directOrder(apiRequest)));
    }

    /**
     * 사용자 주문서 API. 장바구니 -> 상품구매시 사용
     *
     * @param cartSeqList
     * @return
     */
    @PostMapping("/cart")
    public ResponseEntity<ApiResponse> cartOrder(@RequestParam List<Long> cartSeqList) {
        return ResponseEntity.ok(ApiResponse.success(orderService.cartOrder(cartSeqList)));
    }

    /**
     * 사용자 - 주문 리스트 조회. (최근조회 포함. 날짜 파라미터 없으면 기본 30일 세팅.)
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/list")
    public ResponseEntity<ApiResponse> searchOrderItemListAll(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(ApiResponse.success(orderService.searchOrderItemList(apiRequest)));
    }

    /**
     * 사용자 - 주문 상세 조회. (하나의 주문에 귀속 된 다른 아이템 모두.)
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/detail")
    public ResponseEntity<ApiResponse> getOrderDetail(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderDetail(apiRequest)));
    }

    /**
     * 고객 - 결제 승인 요청
     *
     * @return
     */
    @PostMapping("/return")
    public String successOrderPay(@RequestParam Map<String, Object> returnMap) {
        if (returnMap.containsKey("result_code")) { // 가맹점 결과 코드
            if (!"EC0000".equals(returnMap.get("result_code").toString())) {
                return "    <script language='javascript'>\n" +
                        "        window.onload = function(){\n" +
                        "            opener.location.href=\"" + PropertiesConstants.FRONT_ADDR + "/buyer/order/failed?orderNo=" + returnMap.get("orderNo") + "\";\n" +
                        "            self.close();\n" +
                        "        }\n" +
                        "    </script>";
            }
        }
        Map<String, String> resultData = orderService.successOrderPay(returnMap);

        String orderResultState = resultData.get("orderState");

        // 주문 정상
        if (ObjectUtils.equals(OrderStateEnum.PAY_DONE.getEnCode(), orderResultState)) {
            return "    <script language='javascript'>\n" +
                    "        window.onload = function(){\n" +
                    "            opener.location.href=\"" + PropertiesConstants.FRONT_ADDR + "/buyer/order/success?corpName=" + resultData.get("corpName") + "&orderNo=" + resultData.get("orderNo") + "\";\n" +
                    "            self.close();\n" +
                    "        }\n" +
                    "    </script>";

        }

        // 주문 오류
        return "    <script language='javascript'>\n" +
                "        window.onload = function(){\n" +
                "            opener.location.href=\"" + PropertiesConstants.FRONT_ADDR + "/buyer/order/failed?orderNo=" + resultData.get("orderNo") + "\";\n" +
                "            self.close();\n" +
                "        }\n" +
                "    </script>";

    }

    @PostMapping("/modify/state")
    public ResponseEntity<ApiResponse> modifyOrderState(@RequestBody ApiRequest apiRequest) {
        orderService.modifyOrderInfoByBuyer(apiRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 결제 직전 주문서 변경.
    @PostMapping("/modify/before-order")
    public ResponseEntity<ApiResponse> modifyShippingAddressByBeforePayment(@RequestBody ApiRequest apiRequest) {
        orderService.modifyShippingAddress(apiRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 결제 성공 시 데이터 호출
    @PostMapping("/success")
    public ResponseEntity<ApiResponse> orderSuccess(HttpSession session) {
        Map<String, String> result = new HashMap<>();
        result.put("corpName", (String) session.getAttribute("successCorpName"));
        result.put("orderNo", (String) session.getAttribute("successOrderNo"));
        return ResponseEntity.ok(ApiResponse.success(result));
    }

}
