package kr.co.medicals.buyer.controller;

import kr.co.medicals.buyer.TmpBuyerService;
import kr.co.medicals.common.constants.BuyerStateConstants;
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
@RequestMapping("/buyer/tmp")
public class TmpBuyerController {

    private final TmpBuyerService tmpBuyerService;

    @Autowired
    public TmpBuyerController(TmpBuyerService tmpBuyerService) {
        this.tmpBuyerService = tmpBuyerService;
    }


    /**
     * 회원 수정 신청 리스트 조회
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/list")
    public ResponseEntity<ApiResponse> getTmpBuyerList(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(tmpBuyerService.buyerGetTmpBuyerList(apiRequest));
    }

    /**
     * 회원 수정 신청 상세 조회
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/detail")
    public ResponseEntity<ApiResponse> getTmpBuyerInfo(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(tmpBuyerService.getTmpBuyerInfo(apiRequest));
    }

    /**
     * 회원 가입 요청
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> reqJoinBuyer(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(tmpBuyerService.registerTmpBuyer(apiRequest, BuyerStateConstants.READY));
    }

    /**
     * 회원 수정 요청
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/modify")
    public ResponseEntity<ApiResponse> reqModifyBuyer(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(tmpBuyerService.registerTmpBuyer(apiRequest, BuyerStateConstants.WAIT));
    }


}
