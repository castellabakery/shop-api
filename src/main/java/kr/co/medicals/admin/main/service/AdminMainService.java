package kr.co.medicals.admin.main.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kr.co.medicals.admin.main.dto.AdminDto;
import kr.co.medicals.admin.main.dto.AdminMenuDto;
import kr.co.medicals.common.constants.PathConstants;
import kr.co.medicals.common.util.*;
import kr.co.medicals.common.util.ApiResponse;
import kr.co.medicals.common.util.ObjectMapperUtil;
import kr.co.medicals.common.util.SessionUtil;
import kr.co.medicals.common.util.WebClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdminMainService {

    private final WebClientUtils webClientUtils;

    @Autowired
    public AdminMainService(WebClientUtils webClientUtils) {
        this.webClientUtils = webClientUtils;
    }

    public List<AdminMenuDto> getAdminSideMenu(String adminType) throws JsonProcessingException {
        List<AdminMenuDto> adminMenuDtoList = new ArrayList<>();
        List<AdminMenuDto> buyerInnerMenuList = new ArrayList<>();
        List<AdminMenuDto> productInnerMenuList = new ArrayList<>();
        List<AdminMenuDto> orderInnerMenuList = new ArrayList<>();
        List<AdminMenuDto> benefitInnerMenuList = new ArrayList<>();
        List<AdminMenuDto> AdminBoardInnerMenuList = new ArrayList<>();
        List<AdminMenuDto> VaccountInnerMenuList = new ArrayList<>();

        // === 회원관리 ===
        buyerInnerMenuList.add(
                AdminMenuDto.builder().key("buyerSub1").labelLink("/admin/buyer/list").labelName("유저관리").build()
        );
        buyerInnerMenuList.add(
                AdminMenuDto.builder().key("buyerSub3").labelLink("/admin/approve/list").labelName("승인관리").build()
        );
        adminMenuDtoList.add(AdminMenuDto.builder().key("buyer1").labelName("회원관리")
            .innerMenu(
                    buyerInnerMenuList
            )
        .build());

        // === 상품관리 ===
        productInnerMenuList.add(
                AdminMenuDto.builder().key("medicineSub1").labelLink("/admin/medicine/list").labelName("의약품관리").build()
        );
        adminMenuDtoList.add(AdminMenuDto.builder().key("medicine1").labelName("상품관리")
            .innerMenu(
                    productInnerMenuList
            )
        .build());

        // === 주문관리 ===
        orderInnerMenuList.add(
                AdminMenuDto.builder().key("orderSub1").labelLink("/admin/order/list").labelName("주문리스트").build()
        );
        orderInnerMenuList.add(
                AdminMenuDto.builder().key("orderSub2").labelLink("/admin/order/statistics").labelName("매출조회").build()
        );
        adminMenuDtoList.add(AdminMenuDto.builder().key("order1").labelName("주문관리")
                .innerMenu(
                        orderInnerMenuList
                )
                .build());

        // === 혜택관리 ===
        benefitInnerMenuList.add(
                AdminMenuDto.builder().key("benefitSub1").labelLink("/admin/point/list").labelName("포인트관리").build()
        );
        adminMenuDtoList.add(AdminMenuDto.builder().key("benefit1").labelName("혜택관리")
                .innerMenu(
                        benefitInnerMenuList
                )
                .build());

        // === 공지사항 관리 ===
        AdminBoardInnerMenuList.add(
                AdminMenuDto.builder().key("AdminBoardSub1").labelLink("/admin/board/list").labelName("공지사항관리").build()
        );
        adminMenuDtoList.add(AdminMenuDto.builder().key("AdminBoard1").labelName("공지사항관리")
                .innerMenu(
                        AdminBoardInnerMenuList
                )
                .build());

        // ===  관리 ===
        VaccountInnerMenuList.add(
                AdminMenuDto.builder().key("VaccountSub1").labelLink("/admin/vaccount").labelName("").build()
        );
        adminMenuDtoList.add(AdminMenuDto.builder().key("Vaccount1").labelName("")
                .innerMenu(
                        VaccountInnerMenuList
                )
                .build());

        return adminMenuDtoList;
    }

    public List<AdminMenuDto> getAdminMyMenu(String adminType) {
        List<AdminMenuDto> adminMenuDtoList = new ArrayList<>();
        adminMenuDtoList.add(AdminMenuDto.builder().key("adminLogout").labelLink("/admin/logout").labelName("로그아웃").build());
//        adminMenuDtoList.add(AdminMenuDto.builder().key("adminMypage").labelLink("/admin/mypage").labelName("마이페이지").build());
//        adminMenuDtoList.add(AdminMenuDto.builder().key("adminManage").labelLink("/admin/manage").labelName("관리자 관리").build());
        return adminMenuDtoList;
    }

    public AdminDto getAdminInfoByCode() {

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.ADMIN_INFO)
                .queryParam("adminCode", SessionUtil.getUserCode())
                .encode();

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.GET, uriComponentsBuilder.build().toUri());
        return ObjectMapperUtil.responseConvertDto(response.block().getBody(), AdminDto.class);
    }

}
