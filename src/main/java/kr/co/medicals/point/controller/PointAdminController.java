package kr.co.medicals.point.controller;

import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ApiResponse;
import kr.co.medicals.point.PointService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/point")
public class PointAdminController {

    private final PointService pointService;
    public PointAdminController(PointService pointService) {
        this.pointService = pointService;
    }

    @PostMapping("/buyer/header")
    public ResponseEntity<ApiResponse> getBuyerPointHeader(@RequestBody ApiRequest apiRequest) { // 검색필터에 영향 받지 않는 포인트 헤더 조회.
        return ResponseEntity.ok(ApiResponse.success(pointService.getBuyerPointHeaderAdmin(apiRequest)));
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse> getBuyerPointHistory(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(ApiResponse.success(pointService.getPointHistoryForAdmin(apiRequest)));
    }

}
