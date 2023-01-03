package kr.co.medicals.point.controller;

import kr.co.medicals.buyer.BuyerService;
import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ApiResponse;
import kr.co.medicals.common.util.SessionUtil;
import kr.co.medicals.point.PointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/buyer/point")
public class PointController {

    private final PointService pointService;
    private final BuyerService buyerService;

    @Autowired
    public PointController(PointService pointService, BuyerService buyerService) {
        this.pointService = pointService;
        this.buyerService = buyerService;
    }

    @PostMapping("/header")
    public ResponseEntity<ApiResponse> getBuyerPointHeader() { // 검색필터에 영향 받지 않는 포인트 헤더 조회.
        return ResponseEntity.ok(ApiResponse.success(pointService.getBuyerPointHeaderBuyer(SessionUtil.getPreUserCode())));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse> getBuyerPointHistory(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(ApiResponse.success(pointService.getPointHistoryForBuyer(apiRequest)));
    }

}
