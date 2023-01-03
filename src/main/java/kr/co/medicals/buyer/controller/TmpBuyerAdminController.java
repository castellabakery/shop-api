package kr.co.medicals.buyer.controller;

import kr.co.medicals.buyer.TmpBuyerService;
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
@RequestMapping("/admin/buyer/tmp")
public class TmpBuyerAdminController {

    private final TmpBuyerService tmpBuyerService;

    @Autowired
    public TmpBuyerAdminController(TmpBuyerService tmpBuyerService) {
        this.tmpBuyerService = tmpBuyerService;
    }

    /**
     * 임시회원 가입.수정 리스트 조회
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/list")
    public ResponseEntity<ApiResponse> getTmpBuyerList(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(tmpBuyerService.adminGetTmpBuyerList(apiRequest));
    }

    /**
     * 임시회원 가입.수정 상세 조회
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/detail")
    public ResponseEntity<ApiResponse> getTmpBuyerInfo(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(tmpBuyerService.getTmpBuyerInfo(apiRequest));
    }

    /**
     * 임시회원 가입.수정에 대한 승인 및 반려
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/modify/state")
    public ResponseEntity<ApiResponse> approveBuyer(@RequestBody ApiRequest apiRequest) {
        tmpBuyerService.approveBuyer(apiRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
