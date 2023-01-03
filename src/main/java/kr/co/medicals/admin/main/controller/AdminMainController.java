package kr.co.medicals.admin.main.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.co.medicals.admin.main.dto.AdminMenuDto;
import kr.co.medicals.admin.main.service.AdminMainService;
import kr.co.medicals.common.util.ApiListResponse;
import kr.co.medicals.common.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminMainController {

    private final AdminMainService adminMainService;

    public AdminMainController(AdminMainService adminMainService) {
        this.adminMainService = adminMainService;
    }

    @GetMapping("/menu/side")
    public ApiListResponse<AdminMenuDto> getAdminSideMenu(HttpServletRequest request) throws JsonProcessingException {
        List<AdminMenuDto> result = adminMainService.getAdminSideMenu("");
        return new ApiListResponse<>(result, result.size());
    }

    @GetMapping("/menu/my")
    public ApiListResponse<AdminMenuDto> getAdminMyMenu() {
        List<AdminMenuDto> result = adminMainService.getAdminMyMenu("");
        return new ApiListResponse<>(result, result.size());
    }

    @GetMapping("/my/info")
    public ResponseEntity<ApiResponse> getAdminInfo() {
        return ResponseEntity.ok(ApiResponse.success(adminMainService.getAdminInfoByCode()));
    }

}
