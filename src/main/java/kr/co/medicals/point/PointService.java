package kr.co.medicals.point;

import kr.co.medicals.buyer.BuyerService;
import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import kr.co.medicals.common.enums.PointStateEnum;
import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ObjectMapperUtil;
import kr.co.medicals.common.util.SessionUtil;
import kr.co.medicals.order.domain.dto.OrderInfoDto;
import kr.co.medicals.order.domain.dto.OrderItemDto;
import kr.co.medicals.point.domain.dto.PointDto;
import kr.co.medicals.point.domain.dto.PointHistoryDto;
import kr.co.medicals.point.domain.entity.Point;
import kr.co.medicals.point.domain.entity.PointHistory;
import kr.co.medicals.point.domain.repository.PointHistoryRepository;
import kr.co.medicals.point.domain.repository.PointHistoryRepositorySupport;
import kr.co.medicals.point.domain.repository.PointRepository;
import kr.co.medicals.point.domain.repository.PointRepositorySupport;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class PointService {

    public final PointRepository pointRepository;
    public final PointHistoryRepository pointHistoryRepository;
    public final PointHistoryRepositorySupport pointHistoryRepositorySupport;
    public final BuyerService buyerService;
    public final PointRepositorySupport pointRepositorySupport;

    @Autowired
    public PointService(PointRepository pointRepository, PointHistoryRepository pointHistoryRepository, PointHistoryRepositorySupport pointHistoryRepositorySupport, BuyerService buyerService, PointRepositorySupport pointRepositorySupport) {
        this.pointRepository = pointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.pointHistoryRepositorySupport = pointHistoryRepositorySupport;
        this.buyerService = buyerService;
        this.pointRepositorySupport = pointRepositorySupport;
    }

    // 회원의 포인트 헤더 조회. - 회원
    public PointDto getBuyerPointHeaderBuyer(String buyerCode) {
        PointDto pointDto = pointRepositorySupport.getBuyerPoint(buyerCode);
        return pointDto;
    }

    // 회원의 포인트 헤더 조회. - 관리자
    public PointDto getBuyerPointHeaderAdmin(ApiRequest apiRequest) {
        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);
        if (ObjectUtils.isEmpty(map) || !map.containsKey("buyerSeq")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "buyerSeq");
        }
        Long buyerSeq = Long.valueOf(map.get("buyerSeq").toString());
        PointDto pointDto = pointRepositorySupport.getBuyerPoint(buyerService.getBuyerCodeByBuyerSeq(buyerSeq));
        return pointDto;
    }

    // 가입시 포인트 신규 생성.
    public void registerBuyerPoint(String buyerCode) {
        pointRepository.save(new PointDto().setDefaultPoint(buyerCode));
    }

    // 회원 - 회원의 포인트 이력 조회.
    public Page<PointHistoryDto> getPointHistoryForBuyer(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        // page 정보 없으면 안됨.
        if ((ObjectUtils.isEmpty(apiRequest.getPage()) || apiRequest.getPage() < 0)
                || ObjectUtils.isEmpty(apiRequest.getPageSize()) || apiRequest.getPageSize() < 0) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "pageInfo");
        }

        String startDate = "";
        String endDate = "";
        List<String> pointState = new ArrayList<>();

        if (!ObjectUtils.isEmpty(map)) {
            if (map.containsKey("startDate")) {
                startDate = map.get("startDate").toString();
            }
            if (map.containsKey("endDate")) {
                endDate = map.get("endDate").toString();
            }
            if (map.containsKey("pointState")) {
                String pointStateStr = map.get("pointState").toString();
                pointState = PointStateEnum.getByCodeList(pointStateStr);
            }
        }

        if ((!ObjectUtils.isEmpty(startDate) && ObjectUtils.isEmpty(endDate))
                || (ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate))) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "startDate, endDate");
        }

        if (ObjectUtils.isEmpty(startDate) && ObjectUtils.isEmpty(endDate)) {
            LocalDate searchEnd = LocalDate.now();
            LocalDate searchStart = searchEnd.minusMonths(1);// -- 회원 한달, 관리자 일주일
            startDate = searchStart.toString();
            endDate = searchEnd.toString();
        }

        Pageable pageable = PageRequest.of(apiRequest.getPage(), apiRequest.getPageSize());
        return pointHistoryRepositorySupport.getPointHistoryByBuyer(SessionUtil.getPreUserCode(), startDate, endDate, pointState, pageable);
    }

    // 관리자 - 회원의 포인트 이력 조회.
    public Page<PointHistoryDto> getPointHistoryForAdmin(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        // page 정보 없으면 안됨.
        if ((ObjectUtils.isEmpty(apiRequest.getPage()) || apiRequest.getPage() < 0)
                || ObjectUtils.isEmpty(apiRequest.getPageSize()) || apiRequest.getPageSize() < 0) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "pageInfo");
        }

        String startDate = "";
        String endDate = "";
        if (!ObjectUtils.isEmpty(map)) {
            // -------------startDate, endDate 검색 조건.
            if (map.containsKey("startDate")) {
                startDate = map.get("startDate").toString();
            }
            if (map.containsKey("endDate")) {
                endDate = map.get("endDate").toString();
            }
        }

        if ((!ObjectUtils.isEmpty(startDate) && ObjectUtils.isEmpty(endDate))
                || (ObjectUtils.isEmpty(startDate) && !ObjectUtils.isEmpty(endDate))) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "startDate, endDate");
        }

        if (ObjectUtils.isEmpty(startDate) && ObjectUtils.isEmpty(endDate)) {
            LocalDate searchEnd = LocalDate.now();
            LocalDate searchStart = searchEnd.minusWeeks(1);// -- 회원 한달, 관리자 일주일
            startDate = searchStart.toString();
            endDate = searchEnd.toString();
        }

        // ------------- pointState 검색 조건.
        List<String> pointState = new ArrayList<>();
        if (map.containsKey("pointState")) {
            String pointStateStr = map.get("pointState").toString();
            if (!ObjectUtils.isEmpty(pointStateStr)) {
                pointState = PointStateEnum.getByCodeList(pointStateStr);
                if (ObjectUtils.isEmpty(pointState) || pointState.size() == 0) {
                    pointState = Arrays.asList(pointStateStr);
                }
            } else {
                pointState = null;
            }
        }

        // ------------- orderNo 검색 조건.
        String orderNo = "";
        if (map.containsKey("orderNo")) {
            orderNo = map.get("orderNo").toString();
        }

        // ------------- buyerIdentificationId 검색 조건.
        String buyerIdentificationId = "";
        if (map.containsKey("buyerIdentificationId")) {
            buyerIdentificationId = map.get("buyerIdentificationId").toString();
        }

        // ------------- productDisplayName 검색 조건.
        String productDisplayName = "";
        if (map.containsKey("productDisplayName")) {
            productDisplayName = map.get("productDisplayName").toString();
        }

        // ------------- searchKeyword 검색 조건.
        String searchKeyword = "";
        if (map.containsKey("searchKeyword")) {
            searchKeyword = map.get("searchKeyword").toString();
            List<String> stateList = PointStateEnum.getByCodeList(searchKeyword);
            if (!ObjectUtils.isEmpty(stateList) && stateList.size() > 0) {
                pointState = stateList;
                searchKeyword = null;
            }
        }

        Pageable pageable = PageRequest.of(apiRequest.getPage(), apiRequest.getPageSize());

        PointDto pointDto = pointHistoryRepositorySupport.getPointHistoryByAdmin(startDate, endDate, pointState, orderNo, productDisplayName, buyerIdentificationId, searchKeyword, pageable);
        List<PointHistoryDto> pointHistoryDtoList = pointDto.getPointHistoryDtoList();

        return new PageImpl<>(pointHistoryDtoList, pageable, pointDto.getListCnt());
    }


    // 회원의 현재 포인트 상태 조회. 이력 업데이트 용.
    public PointDto getBuyerPointByBuyerCode(String buyerCode) {
        return pointRepositorySupport.getBuyerPoint(buyerCode);
    }

    // 결제완료 : 적립예정건 생성.
    public PointDto payDoneExpectPoint(OrderInfoDto orderInfoDto, PointDto nowPoint, int amount, String buyerIdentificationCode) {
        PointDto pointDto = this.expectPoint(orderInfoDto, nowPoint);
        if (ObjectUtils.isEmpty(pointDto)) {
            return nowPoint;
        }
        PointHistoryDto pointHistoryDto = pointDto.getPointHistoryDto();
        Point point = pointDto.updateEntity(buyerIdentificationCode);
        pointRepository.save(point);
        PointHistory pointHistory = pointHistoryDto.insertEntity(buyerIdentificationCode, amount);
        pointHistoryRepository.save(pointHistory);
        return pointDto;
    }

    // 결제완료 : 사용포인트 증가, 보유 포인트 차감.
    public PointDto payDoneUsePoint(OrderInfoDto orderInfoDto, PointDto nowPoint, int usePoint, int amount, String buyerIdentificationCode) {
        PointDto pointDto = this.usePoint(orderInfoDto, nowPoint, usePoint, buyerIdentificationCode);
        if (Objects.equals(usePoint, 0)) {
            return nowPoint;
        }
        PointHistoryDto pointHistoryDto = pointDto.getPointHistoryDto();
        Point point = pointDto.updateEntity(buyerIdentificationCode);
        pointRepository.save(point);
        PointHistory pointHistory1 = pointHistoryDto.insertEntity(buyerIdentificationCode, amount);
        pointHistoryRepository.save(pointHistory1);
        return pointDto;
    }

    //취소승인&환불승인 : 적립예정(감소)
    public PointDto cancelDoneExpectPoint(OrderItemDto orderItemDto, PointDto nowPoint, int amount) {
        if (orderItemDto.getPointSave() == 0) {
            return nowPoint;
        }
        PointDto pointDto = this.expectCancelPoint(orderItemDto, nowPoint);
        PointHistoryDto pointHistoryDto = pointDto.getPointHistoryDto();
        Point point = pointDto.updateEntity(SessionUtil.getUserCode());
        pointRepository.save(point);
        pointHistoryRepository.save(pointHistoryDto.insertEntity(SessionUtil.getUserCode(), amount));
        return pointDto;
    }

    //취소승인&환불승인 : 사용취소
    public PointDto cancelDoneUsePoint(OrderItemDto orderItemDto, PointDto nowPoint, int requestSavePoint, int amount) {
        if (Objects.equals(requestSavePoint, 0)) {
            return nowPoint;
        }
        PointDto pointDto = this.useCancelPoint(orderItemDto, nowPoint, requestSavePoint);
        PointHistoryDto pointHistoryDto = pointDto.getPointHistoryDto();
        Point point = pointDto.updateEntity(SessionUtil.getUserCode());
        pointRepository.save(point);
        pointHistoryRepository.save(pointHistoryDto.insertEntity(SessionUtil.getUserCode(), amount));
        return pointDto;
    }


    //구매확정 : 적립예정(감소) & 적립포인트(증가)
    public PointDto orderConfirmPoint(OrderItemDto orderItemDto, PointDto nowPoint, int amount) {
        if (Objects.equals(orderItemDto.getPointSave(), 0)) {
            return nowPoint;
        }
        PointDto pointDto = this.savePoint(orderItemDto, nowPoint);
        PointHistoryDto pointHistoryDto = pointDto.getPointHistoryDto();
        Point point = pointDto.updateEntity(SessionUtil.getUserCode());
        pointRepository.save(point);
        pointHistoryRepository.save(pointHistoryDto.insertEntity(SessionUtil.getUserCode(), amount));
        return pointDto;
    }

    // 결제 완료 : 적립예정
    public PointDto expectPoint(OrderInfoDto orderInfoDto, PointDto nowPoint) {

        int applyPoint = 0;

        for (OrderItemDto dto : orderInfoDto.getOrderItemList()) {
            applyPoint = applyPoint + dto.getPointSave();
        }

        if (Objects.equals(applyPoint, 0)) {
            return null;
        }

        String orderName = orderInfoDto.getOrderName();

        int beforeSaveExpectPoint = nowPoint.getSaveExpectPoint();
        int afterSaveExpectPoint = beforeSaveExpectPoint + applyPoint;

        PointHistoryDto pointHistoryDto = PointHistoryDto
                .byCreate()
                .buyerCode(orderInfoDto.getBuyerCode())

                .orderNo(orderInfoDto.getOrderNo())
                .content(orderName)

                .applyPoint(applyPoint)

                .pointState(PointStateEnum.SAVE_EXPECT.getCode())

                .beforeSavePoint(nowPoint.getSavePoint())
                .afterSavePoint(nowPoint.getSavePoint())

                .beforeSaveExpectPoint(beforeSaveExpectPoint)
                .afterSaveExpectPoint(afterSaveExpectPoint)

                .beforeUsePoint(nowPoint.getUsePoint())
                .afterUsePoint(nowPoint.getUsePoint())

                .createdId(orderInfoDto.getBuyerCode())
                .createdDatetime(LocalDateTime.now())

                .build();
        nowPoint.setPointHistoryDto(pointHistoryDto);

        nowPoint.setSaveExpectPoint(afterSaveExpectPoint);
        return nowPoint;
    }

    public PointDto usePoint(OrderInfoDto orderInfoDto, PointDto nowPoint, int usePoint, String buyerIdentificationCode) {

        String buyerCode = orderInfoDto.getBuyerCode();

        int applyPoint = -(usePoint); // 사용하게되는 포인트여서 마이너스 붙여줌
        String orderName = orderInfoDto.getOrderName();

        // 사용포인트 증가
        int beforeUsePoint = nowPoint.getUsePoint();
        int afterUsePoint = beforeUsePoint - applyPoint;

        // 보유포인트 차감
        int beforeSavePoint = nowPoint.getSavePoint();
        int afterSavePoint = beforeSavePoint + applyPoint;

        PointHistoryDto pointHistoryDto = PointHistoryDto
                .byCreate()
                .buyerCode(buyerCode)

                .orderNo(orderInfoDto.getOrderNo())
                .content(orderName)

                .applyPoint(applyPoint)

                .pointState(PointStateEnum.USE_POINT.getCode())

                .beforeSavePoint(beforeSavePoint)
                .afterSavePoint(afterSavePoint)

                .beforeSaveExpectPoint(nowPoint.getSaveExpectPoint())
                .afterSaveExpectPoint(nowPoint.getSaveExpectPoint())

                .beforeUsePoint(beforeUsePoint)
                .afterUsePoint(afterUsePoint)

                .createdId(buyerIdentificationCode)
                .createdDatetime(LocalDateTime.now())

                .build();
        nowPoint.setPointHistoryDto(pointHistoryDto);

        nowPoint.setSavePoint(afterSavePoint);
        nowPoint.setUsePoint(afterUsePoint);
        return nowPoint;
    }

    // 취소&환불 승인 : 적립예정금액 감소.
    public PointDto expectCancelPoint(OrderItemDto orderItemDto, PointDto nowPoint) {

        int applyPoint = -(orderItemDto.getPointSave()); // 상품에 따른 포인트
        String orderName = orderItemDto.getOrderInfo().getOrderName(); // 주문명

        // 적립 예정 포인트
        int beforeSaveExpectPoint = nowPoint.getSaveExpectPoint();
        int afterSaveExpectPoint = beforeSaveExpectPoint + applyPoint;

        PointHistoryDto pointHistoryDto = PointHistoryDto
                .byCreate()
                .buyerCode(orderItemDto.getOrderInfo().getBuyerCode())

                .orderNo(orderItemDto.getOrderInfo().getOrderNo())
                .content(orderName)

                .applyPoint(applyPoint)

                .pointState(PointStateEnum.CANCEL_EXPECT.getCode())

                .beforeSavePoint(nowPoint.getSavePoint())
                .afterSavePoint(nowPoint.getSavePoint())

                .beforeSaveExpectPoint(beforeSaveExpectPoint)
                .afterSaveExpectPoint(afterSaveExpectPoint)

                .beforeUsePoint(nowPoint.getUsePoint())
                .afterUsePoint(nowPoint.getUsePoint())

                .createdId(SessionUtil.getUserCode())
                .createdDatetime(LocalDateTime.now())

                .build();
        nowPoint.setPointHistoryDto(pointHistoryDto);

        nowPoint.setSaveExpectPoint(afterSaveExpectPoint);
        return nowPoint;
    }

    // 취소&환불 승인 : 사용금액(감소) & 보유금액(증가)
    public PointDto useCancelPoint(OrderItemDto orderItemDto, PointDto nowPoint, int requestSavePoint) {

        int applyPoint = requestSavePoint;
        String orderName = orderItemDto.getOrderInfo().getOrderName();

        // 보유 포인트 = add
        int beforeSavePoint = nowPoint.getSavePoint();
        int afterSavePoint = beforeSavePoint + applyPoint;

        // 사용 포인트 = minus
        int beforeUsePoint = nowPoint.getUsePoint();
        int afterUsePoint = beforeUsePoint - applyPoint;

        PointHistoryDto pointHistoryDto = PointHistoryDto
                .byCreate()
                .buyerCode(orderItemDto.getOrderInfo().getBuyerCode())

                .orderNo(orderItemDto.getOrderInfo().getOrderNo())
                .content(orderName)

                .applyPoint(applyPoint)

                .pointState(PointStateEnum.USE_CANCEL.getCode())

                .beforeSavePoint(beforeSavePoint)
                .afterSavePoint(afterSavePoint)

                .beforeSaveExpectPoint(nowPoint.getSaveExpectPoint())
                .afterSaveExpectPoint(nowPoint.getSaveExpectPoint())

                .beforeUsePoint(beforeUsePoint)
                .afterUsePoint(afterUsePoint)

                .createdId(SessionUtil.getUserCode())
                .createdDatetime(LocalDateTime.now())

                .build();
        nowPoint.setPointHistoryDto(pointHistoryDto);
        nowPoint.setSavePoint(afterSavePoint);
        nowPoint.setUsePoint(afterUsePoint);
        return nowPoint;
    }

    // 구매확정 : 적립 + 적립예정금액 감소.
    public PointDto savePoint(OrderItemDto orderItemDto, PointDto nowPoint) {

        int applyPoint = orderItemDto.getPointSave();
        String orderName = orderItemDto.getOrderInfo().getOrderName();

        // 사용가능 포인트
        int beforeSavePoint = nowPoint.getSavePoint();
        int afterSavePoint = beforeSavePoint + applyPoint;

        // 적립 예정 포인트
        int beforeSaveExpectPoint = nowPoint.getSaveExpectPoint();
        int afterSaveExpectPoint = beforeSaveExpectPoint - applyPoint;

        PointHistoryDto pointHistoryDto = PointHistoryDto
                .byCreate()
                .buyerCode(orderItemDto.getOrderInfo().getBuyerCode())

                .orderNo(orderItemDto.getOrderInfo().getOrderNo())
                .content(orderName)

                .applyPoint(applyPoint)

                .pointState(PointStateEnum.SAVE_POINT.getCode())

                .beforeSavePoint(beforeSavePoint)
                .afterSavePoint(afterSavePoint)

                .beforeSaveExpectPoint(beforeSaveExpectPoint)
                .afterSaveExpectPoint(afterSaveExpectPoint)

                .beforeUsePoint(nowPoint.getUsePoint())
                .afterUsePoint(nowPoint.getUsePoint())

                .createdId(SessionUtil.getUserCode())
                .createdDatetime(LocalDateTime.now())

                .build();

        nowPoint.setPointHistoryDto(pointHistoryDto);

        nowPoint.setSavePoint(afterSavePoint);
        nowPoint.setSaveExpectPoint(afterSaveExpectPoint);
        return nowPoint;
    }

}
