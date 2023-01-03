package kr.co.medicals.terms.controller;

import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ApiResponse;
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
@RequestMapping("/admin/terms")
public class TermsAdminController {

    private final TermsService termsService;

    @Autowired
    public TermsAdminController(TermsService termsService) {
        this.termsService = termsService;
    }

    /**
     * 약관 등록 
     * @param apiRequest
     * @return
     */
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addTerms(@RequestBody ApiRequest apiRequest) {
        termsService.addTerms(apiRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 약관 수정
     * @param apiRequest
     * @return
     */
    @PostMapping("/modify")
    public ResponseEntity<ApiResponse> modifyTerms(@RequestBody ApiRequest apiRequest) {
        termsService.modifyTerms(apiRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 약관 리스트 조회
     * @param apiRequest
     * @return
     */
    @PostMapping("/list")
    public ResponseEntity<ApiResponse> getTermsList(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(ApiResponse.success(termsService.getTermsListPage(apiRequest)));
    }

    /**
     * 약관 상세 조회
     * @param apiRequest
     * @return
     */
    @PostMapping("/detail")
    public ResponseEntity<ApiResponse> getTermsDetail(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(ApiResponse.success(termsService.getTermsDetail(apiRequest)));
    }

}
