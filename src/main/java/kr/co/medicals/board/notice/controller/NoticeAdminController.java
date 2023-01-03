package kr.co.medicals.board.notice.controller;

import kr.co.medicals.board.notice.service.NoticeService;
import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/notice")
public class NoticeAdminController {

    private final NoticeService noticeService;

    public NoticeAdminController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping("/list")
    public ResponseEntity<ApiResponse> searchNoticeList(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(ApiResponse.success(noticeService.searchNoticeList(apiRequest)));
    }

    @PostMapping("/detail")
    public ResponseEntity<ApiResponse> getOrderDetail(@RequestBody ApiRequest apiRequest) {
        return ResponseEntity.ok(ApiResponse.success(noticeService.getNoticeDetailBySeq(apiRequest)));
    }

    @PostMapping("/add-modify")
    public ResponseEntity<ApiResponse> addModify(@RequestBody ApiRequest apiRequest) {
        noticeService.addModifyAdminBoard(apiRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiResponse> delete(@RequestBody ApiRequest apiRequest) {
        noticeService.deleteAdminBoard(apiRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
