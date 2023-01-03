package kr.co.medicals.buyer;

import kr.co.medicals.buyer.domain.dto.BuyerIdentificationDto;
import kr.co.medicals.buyer.domain.dto.BuyerVaccountDto;
import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import kr.co.medicals.common.constants.IdentificationTypeConstants;
import kr.co.medicals.common.constants.PathConstants;
import kr.co.medicals.common.util.*;
import kr.co.medicals.order.OrderService;
import kr.co.medicals.order.domain.dto.OrderItemDto;
import kr.co.medicals.point.PointService;
import kr.co.medicals.point.domain.dto.PointDto;
import kr.co.medicals.common.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Objects;


@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class RemoveBuyerService {

    private final BuyerService buyerService;
    private final OrderService orderService;
    private final PointService pointService;
    private final WebClientUtils webClientUtils;

    @Autowired
    public RemoveBuyerService(BuyerService buyerService, OrderService orderService, PointService pointService, WebClientUtils webClientUtils) {
        this.buyerService = buyerService;
        this.orderService = orderService;
        this.pointService = pointService;
        this.webClientUtils = webClientUtils;
    }

    // 회원 탈퇴 - 사용자만 가능함(메인계정 탈퇴 요청시 부계정까지 전부 처리). admin 사용하면 안됨.
    public ApiResponse deleteBuyerIdentification(ApiRequest apiRequest) { // 사용자 - 회원 탈퇴 잘됨. // 관리자 회원 탈퇴 - 회원 조회 안됨.

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("buyerIdentificationCode")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "buyerIdentificationCode");
        }

        // 파라미터. - 삭제할 계정의 identificationCode 값.
        String buyerIdentificationCode = map.get("buyerIdentificationCode").toString();
        String requestBuyerIdentificationCode = map.get("buyerIdentificationCode").toString();

        if (Objects.equals("buyer", SessionUtil.getUserType())) {
            requestBuyerIdentificationCode = SessionUtil.getUserCode();
        }

        // 요청한 회원 계정 정보
        BuyerIdentificationDto requestDto = buyerService.getBuyerInfoByIdentificationCode(requestBuyerIdentificationCode);

        // 삭제를 요청한 ID가 서브 계정이면 Exception
        if (Objects.equals(IdentificationTypeConstants.SUB, requestDto.getIdentificationType())) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.AUTHENTICATION_FAILED, "AUTHENTICATION_FAILED", "서브계정 삭제는 메인계정만 가능합니다.");
        }

        // 삭제 될 계정 정보
        BuyerIdentificationDto deleteDto = buyerService.getBuyerInfoByIdentificationCode(buyerIdentificationCode);

        String requestBuyerCode = requestDto.getBuyer().getBuyerCode();
        String deleteBuyerCode = deleteDto.getBuyer().getBuyerCode();

        // 삭제 될 계정 정보의 소유자가 아니면 Exception
        if (!Objects.equals(requestBuyerCode, deleteBuyerCode)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.AUTHENTICATION_FAILED, "AUTHENTICATION_FAILED", "정지하려는 계정의 상위 계정이 아닙니다.");
        }

        // Main 일때만 주문내역, 포인트 검사 진행.
        if (Objects.equals(deleteDto.getIdentificationType(), IdentificationTypeConstants.MAIN)) {

            ////////============= 실행중인 주문건이 있는지 확인 ===============///////////////////
            List<OrderItemDto> orderItemDtoList = orderService.getOrderItemListForBuyerDelete(requestBuyerCode);

            log.info("buyerCode : {}, orderCount : {}", deleteBuyerCode, orderItemDtoList.size());
            if (orderItemDtoList.size() > 0) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.IS_EXISTS, "EXISTS_ORDER");
            }

            //////////============= 포인트 남아있는지 확인. ===============///////////////////
            PointDto pointDto = pointService.getBuyerPointHeaderBuyer(deleteBuyerCode);
            log.info("buyerCode : {}, point : {}", deleteBuyerCode, pointDto.getSavePoint());
            if (pointDto.getSavePoint() > 0) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.IS_EXISTS, "EXISTS_POINT");
            }

        }

        // 삭제 API 호출.
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.BUYER)
                .queryParam("buyerIdentificationCode", buyerIdentificationCode)
                .queryParam("userCode", SessionUtil.getUserCode())
                .encode();

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.DELETE, uriComponentsBuilder.build().toUri());
        return response.block().getBody();
    }

}
