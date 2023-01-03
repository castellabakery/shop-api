package kr.co.medicals.buyer.controller;

import kr.co.medicals.buyer.BuyerService;
import kr.co.medicals.buyer.RemoveBuyerService;
import kr.co.medicals.common.constants.IdentificationTypeConstants;
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
@RequestMapping("/admin/buyer")
public class BuyerAdminController {

    private final BuyerService buyerService;
    private final RemoveBuyerService removeBuyerService;

    @Autowired
    public BuyerAdminController(BuyerService buyerService, RemoveBuyerService removeBuyerService) {
        this.buyerService = buyerService;
        this.removeBuyerService = removeBuyerService;
    }

    /**
     * 회원 리스트 조회
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/list")
    public ResponseEntity<ApiResponse> getBuyerList(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(buyerService.getBuyerList(apiRequest, IdentificationTypeConstants.MAIN));
    }

    /**
     * 회원 상세 조회
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/detail")
    public ResponseEntity<ApiResponse> getBuyerInfo(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(buyerService.getBuyerIdentificationInfo(apiRequest));
    }


    /**
     * 회원 상태 변경
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/modify/buyer-state")
    public ResponseEntity<ApiResponse> modifyBuyerState(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(buyerService.modifyStateBuyer(apiRequest));
    }

    /**
     * 회원 서브 리스트 조회
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/sub/list")
    public ResponseEntity<ApiResponse> getBuyerSubList(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(buyerService.getBuyerList(apiRequest, IdentificationTypeConstants.SUB));
    }


    /**
     * 회원 패스워드 초기화
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/init/password")
    public ResponseEntity<ApiResponse> initBuyerPassword(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(buyerService.initPassword(apiRequest));
    }

    /**
     * 회원 삭제 - 메인삭제, 서브삭제 둘 다 사용.
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse> deleteBuyerIdentification(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(removeBuyerService.deleteBuyerIdentification(apiRequest));
    }


}
