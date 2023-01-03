package kr.co.medicals.terms.controller;

import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ApiResponse;
import kr.co.medicals.common.util.SessionUtil;
import kr.co.medicals.terms.BuyerTermsService;
import kr.co.medicals.terms.TermsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/buyer/terms")
public class TermsBuyerController {

    private final TermsService termsService;
    private final BuyerTermsService buyerTermsService;

    @Autowired
    public TermsBuyerController(TermsService termsService, BuyerTermsService buyerTermsService) {
        this.termsService = termsService;
        this.buyerTermsService = buyerTermsService;
    }

    /**
     * 약관 상세 조회
     * @param apiRequest
     * @return TermsDto
     */
    @PostMapping("/detail")
    public ResponseEntity<ApiResponse> getTermsDetail(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(ApiResponse.success(termsService.getTermsDetail(apiRequest)));
    }

    /**
     * 약관 동의 여부 리스트
     * @return List<BuyerTermsDto>
     */
    @PostMapping("/agree-list")
    public ResponseEntity<ApiResponse> getBuyerTermsList() {
        return ResponseEntity.ok(ApiResponse.success(buyerTermsService.getBuyerTermsList(SessionUtil.getUserCode(), null)));
    }

//    /**
//     * 약관 동의 여부 상세
//     * @return BuyerTermsDto
//     */
//    @PostMapping("/agree-detail")
//    public ResponseEntity<ApiResponse> getBuyerTermsDetail(@RequestBody ApiRequest apiRequest) {
//        return ResponseEntity.ok(ApiResponse.success(buyerTermsService.getBuyerTermsDetail(apiRequest)));
//    }

}
