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

    // ????????? ????????? ?????? ??????. - ??????
    public PointDto getBuyerPointHeaderBuyer(String buyerCode) {
        PointDto pointDto = pointRepositorySupport.getBuyerPoint(buyerCode);
        return pointDto;
    }

    // ????????? ????????? ?????? ??????. - ?????????
    public PointDto getBuyerPointHeaderAdmin(ApiRequest apiRequest) {
        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);
        if (ObjectUtils.isEmpty(map) || !map.containsKey("buyerSeq")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "buyerSeq");
        }
        Long buyerSeq = Long.valueOf(map.get("buyerSeq").toString());
        PointDto pointDto = pointRepositorySupport.getBuyerPoint(buyerService.getBuyerCodeByBuyerSeq(buyerSeq));
        return pointDto;
    }

    // ????????? ????????? ?????? ??????.
    public void registerBuyerPoint(String buyerCode) {
        pointRepository.save(new PointDto().setDefaultPoint(buyerCode));
    }

    // ?????? - ????????? ????????? ?????? ??????.
    public Page<PointHistoryDto> getPointHistoryForBuyer(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        // page ?????? ????????? ??????.
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
            LocalDate searchStart = searchEnd.minusMonths(1);// -- ?????? ??????, ????????? ?????????
            startDate = searchStart.toString();
            endDate = searchEnd.toString();
        }

        Pageable pageable = PageRequest.of(apiRequest.getPage(), apiRequest.getPageSize());
        return pointHistoryRepositorySupport.getPointHistoryByBuyer(SessionUtil.getPreUserCode(), startDate, endDate, pointState, pageable);
    }

    // ????????? - ????????? ????????? ?????? ??????.
    public Page<PointHistoryDto> getPointHistoryForAdmin(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        // page ?????? ????????? ??????.
        if ((ObjectUtils.isEmpty(apiRequest.getPage()) || apiRequest.getPage() < 0)
                || ObjectUtils.isEmpty(apiRequest.getPageSize()) || apiRequest.getPageSize() < 0) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "pageInfo");
        }

        String startDate = "";
        String endDate = "";
        if (!ObjectUtils.isEmpty(map)) {
            // -------------startDate, endDate ?????? ??????.
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
            LocalDate searchStart = searchEnd.minusWeeks(1);// -- ?????? ??????, ????????? ?????????
            startDate = searchStart.toString();
            endDate = searchEnd.toString();
        }

        // ------------- pointState ?????? ??????.
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

        // ------------- orderNo ?????? ??????.
        String orderNo = "";
        if (map.containsKey("orderNo")) {
            orderNo = map.get("orderNo").toString();
        }

        // ------------- buyerIdentificationId ?????? ??????.
        String buyerIdentificationId = "";
        if (map.containsKey("buyerIdentificationId")) {
            buyerIdentificationId = map.get("buyerIdentificationId").toString();
        }

        // ------------- productDisplayName ?????? ??????.
        String productDisplayName = "";
        if (map.containsKey("productDisplayName")) {
            productDisplayName = map.get("productDisplayName").toString();
        }

        // ------------- searchKeyword ?????? ??????.
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


    // ????????? ?????? ????????? ?????? ??????. ?????? ???????????? ???.
    public PointDto getBuyerPointByBuyerCode(String buyerCode) {
        return pointRepositorySupport.getBuyerPoint(buyerCode);
    }

    // ???????????? : ??????????????? ??????.
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

    // ???????????? : ??????????????? ??????, ?????? ????????? ??????.
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

    //????????????&???????????? : ????????????(??????)
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

    //????????????&???????????? : ????????????
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


    //???????????? : ????????????(??????) & ???????????????(??????)
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

    // ?????? ?????? : ????????????
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

        int applyPoint = -(usePoint); // ?????????????????? ??????????????? ???????????? ?????????
        String orderName = orderInfoDto.getOrderName();

        // ??????????????? ??????
        int beforeUsePoint = nowPoint.getUsePoint();
        int afterUsePoint = beforeUsePoint - applyPoint;

        // ??????????????? ??????
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

    // ??????&?????? ?????? : ?????????????????? ??????.
    public PointDto expectCancelPoint(OrderItemDto orderItemDto, PointDto nowPoint) {

        int applyPoint = -(orderItemDto.getPointSave()); // ????????? ?????? ?????????
        String orderName = orderItemDto.getOrderInfo().getOrderName(); // ?????????

        // ?????? ?????? ?????????
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

    // ??????&?????? ?????? : ????????????(??????) & ????????????(??????)
    public PointDto useCancelPoint(OrderItemDto orderItemDto, PointDto nowPoint, int requestSavePoint) {

        int applyPoint = requestSavePoint;
        String orderName = orderItemDto.getOrderInfo().getOrderName();

        // ?????? ????????? = add
        int beforeSavePoint = nowPoint.getSavePoint();
        int afterSavePoint = beforeSavePoint + applyPoint;

        // ?????? ????????? = minus
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

    // ???????????? : ?????? + ?????????????????? ??????.
    public PointDto savePoint(OrderItemDto orderItemDto, PointDto nowPoint) {

        int applyPoint = orderItemDto.getPointSave();
        String orderName = orderItemDto.getOrderInfo().getOrderName();

        // ???????????? ?????????
        int beforeSavePoint = nowPoint.getSavePoint();
        int afterSavePoint = beforeSavePoint + applyPoint;

        // ?????? ?????? ?????????
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
