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
@RequestMapping("/buyer")
public class BuyerController {

    private final BuyerService buyerService;
    private final RemoveBuyerService removeBuyerService;

    @Autowired
    public BuyerController(BuyerService buyerService, RemoveBuyerService removeBuyerService) {
        this.buyerService = buyerService;
        this.removeBuyerService = removeBuyerService;
    }

    /**
     * 회원 메인 상세 조회
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/detail")
    public ResponseEntity<ApiResponse> getBuyerIdentificationMain(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(buyerService.getBuyerIdentificationInfo(apiRequest));
    }

    /**
     * 회원 코드 존재 여부 확인.
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/exists/buyer-code")
    public ResponseEntity<ApiResponse> existsBuyerCode(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(buyerService.existsBuyerCode(apiRequest));
    }

    /**
     * 회원 아이디 존재 여부 확인.
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/exists/buyer-identification-id")
    public ResponseEntity<ApiResponse> existsBuyerIdentificationId(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(buyerService.existsBuyerIdentificationId(apiRequest));
    }

    /**
     * 회원 이메일 존재 여부 확인.
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/exists/email")
    public ResponseEntity<ApiResponse> existsBuyerEmail(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(buyerService.existsBuyerEmail(apiRequest));
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
     * 서브 회원 등록
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/sub/add")
    public ResponseEntity<ApiResponse> addSubBuyer(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(buyerService.addSubBuyer(apiRequest));
    }

    /**
     * 서브 회원 수정
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/sub/modify")
    public ResponseEntity<ApiResponse> modifySubBuyer(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(buyerService.modifySubBuyer(apiRequest));
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


    /**
     * 회원 패스워드 변경
     *
     * @param apiRequest
     * @return
     */
    @PostMapping("/modify/password")
    public ResponseEntity<ApiResponse> modifyPassword(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(buyerService.modifyPassword(apiRequest));
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

}
