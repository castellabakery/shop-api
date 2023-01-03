package kr.co.medicals.order;

import kr.co.medicals.api.aws.email.service.EmailService;
import kr.co.medicals.buyer.BuyerService;
import kr.co.medicals.buyer.domain.dto.BuyerIdentificationDto;
import kr.co.medicals.buyer.domain.entity.BuyerVaccount;
import kr.co.medicals.buyer.domain.repository.BuyerVaccountRepository;
import kr.co.medicals.cart.CartService;
import kr.co.medicals.cart.domain.dto.CartDto;
import kr.co.medicals.cart.domain.repository.CartRepository;
import kr.co.medicals.cart.domain.repository.CartRepositorySupport;
import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import kr.co.medicals.common.constants.BuyerTypeConstants;
import kr.co.medicals.common.enums.FileTypeEnum;
import kr.co.medicals.common.enums.OrderStateEnum;
import kr.co.medicals.common.util.*;
import kr.co.medicals.xx.service.XXService;
import kr.co.medicals.file.AwsFileService;
import kr.co.medicals.file.domain.dto.FileManagerDto;
import kr.co.medicals.order.domain.dto.OrderInfoDto;
import kr.co.medicals.order.domain.dto.OrderItemDto;
import kr.co.medicals.order.domain.entity.OrderItem;
import kr.co.medicals.order.domain.repository.OrderInfoRepository;
import kr.co.medicals.order.domain.repository.OrderInfoRepositorySupport;
import kr.co.medicals.order.domain.repository.OrderItemRepository;
import kr.co.medicals.order.domain.repository.OrderItemRepositorySupport;
import kr.co.medicals.point.PointService;
import kr.co.medicals.point.domain.dto.PointDto;
import kr.co.medicals.product.ProductService;
import kr.co.medicals.product.domain.dto.BuyerTypeProductAmountDto;
import kr.co.medicals.product.domain.dto.ProductDto;
import kr.co.medicals.sales.domain.SalesSearchDto;
import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ObjectCheck;
import kr.co.medicals.common.util.ObjectMapperUtil;
import kr.co.medicals.common.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderService {

    private final OrderInfoRepository orderInfoRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderInfoRepositorySupport orderInfoRepositorySupport;
    private final OrderItemRepositorySupport orderItemRepositorySupport;
    private final BuyerService buyerService;
    private final CartService cartService;
    private final CartRepositorySupport cartRepositorySupport;
    private final CartRepository cartRepository;
    private final XXService XXService;
    private final PointService pointService;
    private final ProductService productService;
    private final AwsFileService awsFileService;
    private final BuyerVaccountRepository buyerVaccountRepository;

    private final EmailService emailService;

    @Autowired
    public OrderService(OrderInfoRepository orderInfoRepository, OrderItemRepository orderItemRepository, OrderInfoRepositorySupport orderInfoRepositorySupport, OrderItemRepositorySupport orderItemRepositorySupport, BuyerService buyerService, CartService cartService, CartRepositorySupport cartRepositorySupport, CartRepository cartRepository, XXService XXService, PointService pointService, ProductService productService, AwsFileService awsFileService, AwsFileService awsFileService1, BuyerVaccountRepository buyerVaccountRepository, EmailService emailService) {
        this.orderInfoRepository = orderInfoRepository;
        this.orderItemRepository = orderItemRepository;
        this.orderInfoRepositorySupport = orderInfoRepositorySupport;
        this.orderItemRepositorySupport = orderItemRepositorySupport;
        this.buyerService = buyerService;
        this.cartService = cartService;
        this.cartRepositorySupport = cartRepositorySupport;
        this.cartRepository = cartRepository;
        this.XXService = XXService;
        this.pointService = pointService;
        this.productService = productService;
        this.awsFileService = awsFileService1;
        this.buyerVaccountRepository = buyerVaccountRepository;
        this.emailService = emailService;
    }

    /**
     * 상품상세 -> 바로구매 -> 장바구니 등록 -> 구매진행
     */
    public OrderInfoDto directOrder(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("productSeq") || !map.containsKey("quantity")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "productSeq, quantity");
        }

        // 장바구니 등록 or 업데이트
        Long cartSeq = cartService.addModifyCart(map);

        // cartDtoList 로 주문서 생성.
        return this.cartOrder(Arrays.asList(cartSeq));
    }

    /**
     * 장바구니 -> 구매 진행
     */
    public OrderInfoDto cartOrder(List<Long> cartSeqList) {
        // 세션에서 회원계정 코드 꺼내서 회원정보 조회. - 관리자는 구매 로직 없어서 분기 안태움.
        BuyerIdentificationDto buyerIdentificationDto = buyerService.getBuyerInfoByIdentificationCode(SessionUtil.getUserCode());

        String buyerCode = buyerIdentificationDto.getBuyer().getBuyerCode();
        List<CartDto> cartDtoList = cartRepositorySupport.myCartList(buyerCode, buyerIdentificationDto.getBuyer().getBuyerType(), cartSeqList); // 장바구니 시퀀스 내용 토대로 조회


        if (ObjectUtils.isEmpty(cartDtoList)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "cart list");
        }

        for (CartDto dto : cartDtoList) {
            if (Objects.equals(dto.getProduct().getDelYn(), "Y") || Objects.equals(dto.getProduct().getUseYn(), "N")) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "product");
            }
        }

        int amountAdd = 0; // 상품금액 쌓기 (info 금액).
        int savePoint = 0;
        OrderInfoDto orderInfoDto = new OrderInfoDto();
        List<OrderItemDto> orderItemDtoList = new ArrayList<>();
        // 장바구니 데이터 주문으로 이동.
        for (CartDto dto : cartDtoList) {
            int createOrderInfo = 0;

            ProductDto productDto = dto.getProduct();
            BuyerTypeProductAmountDto amountDto = dto.getBuyerTypeProductAmount();

            if (createOrderInfo < 1) {
                createOrderInfo++;
            }

            int quantity = dto.getQuantity();

            int itemAmount = quantity * amountDto.getAmount(); // 상품 금액 (item 금액).
            amountAdd = amountAdd + itemAmount;

            int itemPoint = quantity * amountDto.getSavePoint(); // 상품수량+포인트
            savePoint = savePoint + itemPoint;


            OrderItemDto orderItemDto =
                    OrderItemDto.insertStandby()
                            .quantity(quantity)
                            .itemAmount(itemAmount)
                            .pointSave(itemPoint)
                            .productDto(productDto)
                            .amountDto(amountDto)
                            .build();
            orderItemDtoList.add(orderItemDto); // 아이템 list. 주문 seq 넣어야함.
        }

        String orderName = orderItemDtoList.get(0).getProductDisplayName();

        int orderItemCnt = orderItemDtoList.size() - 1;

        if (orderItemCnt >= 1) {
            orderName = orderName + " 외" + orderItemCnt + "건";
        }

        Long orderSeq = orderInfoRepository.save(orderInfoDto.insertEntity(buyerIdentificationDto, amountAdd, savePoint, orderName)).getSeq();


        List<OrderItem> itemList = new ArrayList<>();
        int cnt = 1;
        for (OrderItemDto dto : orderItemDtoList) {
            itemList.add(dto.insertEntity(orderSeq, cnt));
            cnt++;
        }

        orderItemRepository.saveAll(itemList);

        // 주문서 내역 가져오기. 신규 생성.
        OrderInfoDto infoDto = orderInfoRepositorySupport.getOrderInfo(orderSeq, null, buyerCode, Arrays.asList(OrderStateEnum.ORDER_STANDBY.getCode()));
        List<OrderItemDto> itemDtoList = this.getOrderItemList(infoDto.getSeq(), buyerCode);
        infoDto.setOrderItemList(this.addFileToOrderItemList(itemDtoList));
        infoDto.setBuyerIdentification(buyerIdentificationDto); // 주문서 그릴때는 회원 전체 정보가 필요해서 전체 넣어줌.
        return infoDto;
    }

    /**
     * 주문 아이템(+주문정보) 리스트 조회.
     */
    public Page<OrderItemDto> searchOrderItemList(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        // page 정보 없으면 안됨.
        if ((ObjectUtils.isEmpty(apiRequest.getPage()) || apiRequest.getPage() < 0)
                || ObjectUtils.isEmpty(apiRequest.getPageSize()) || apiRequest.getPageSize() < 0) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "paging");
        }

        // 기타 정보
        String buyerCode = null;
        String staffId = null;
        String corpName = null;
        String staffName = null;
        Long buyerIdentificationSeq;
        String startDate;
        String endDate;
        String searchKeyword = null;
        List<Integer> orderState = new ArrayList<>();
        if (Objects.equals("admin", SessionUtil.getUserType())) {

            if (map.containsKey("staffId")) {
                staffId = map.get("staffId").toString();
            }
            if (map.containsKey("corpName")) {
                corpName = map.get("corpName").toString();
            }
            if (map.containsKey("staffName")) {
                staffName = map.get("staffName").toString();
            }
            if (map.containsKey("searchKeyword")) {
                searchKeyword = map.get("searchKeyword").toString();
                String searchStateCheck = searchKeyword.replaceAll(" ", "");
                if (searchStateCheck.contains(",")) {
                    List<String> searchList = List.of(searchStateCheck.split(","));
                    List<Integer> searchStateList = new ArrayList<>();
                    for (String word : searchList) {
                        int stateInt = OrderStateEnum.getCodeByKrCode(word);
                        if (!(stateInt == 0)) {
                            searchStateList.add(stateInt);
                        }
                    }
                    if (searchStateList.size() > 1) {
                        orderState = searchStateList;
                        searchKeyword = null;
                    }
                } else {
                    int stateInt = OrderStateEnum.getCodeByKrCode(searchStateCheck);
                    if (!(stateInt == 0)) {
                        orderState.add(stateInt);
                        searchKeyword = null;
                    }
                }
            }
            // startDatetime, endDatetime 있으면 세팅 없으면 당일.
            if (map.containsKey("startDate") && map.containsKey("endDate")) {
                startDate = map.get("startDate").toString();
                endDate = map.get("endDate").toString();
            } else {
                LocalDate now = LocalDate.now();
                startDate = now.toString();
                endDate = now.toString();
            }

            // 회원 상세에서의 주문 목록 조회 용도.
            if (map.containsKey("buyerIdentificationSeq")) {
                buyerIdentificationSeq = ObjectCheck.isBlankLongException(map.get("buyerIdentificationSeq"), "buyerIdentificationSeq");
                BuyerIdentificationDto dto = buyerService.getBuyerInfoByBuyerIdentificationSeq(buyerIdentificationSeq);
                buyerCode = dto.getBuyer().getBuyerCode();
                startDate = null;
                endDate = null;
            }
        } else {
            buyerCode = SessionUtil.getPreUserCode();
            if (map.containsKey("startDate") && map.containsKey("endDate")) {
                startDate = map.get("startDate").toString();
                endDate = map.get("endDate").toString();
            } else {
                LocalDate searchEnd = LocalDate.now();
                LocalDate searchStart = searchEnd.minusMonths(1);
                startDate = searchStart.toString();
                endDate = searchEnd.toString();
            }
        }
        if (map.containsKey("orderStateList") && !ObjectUtils.isEmpty(map.get("orderStateList"))) {
            List<String> orderStateListStr = List.of(map.get("orderStateList").toString().split(","));
            for (String state : orderStateListStr) {
                int orderStateInt = OrderStateEnum.getCodeByEnCode(state.replaceAll(" ", ""));
                orderState.add(orderStateInt);
            }
        }

        Pageable pageable = PageRequest.of(apiRequest.getPage(), apiRequest.getPageSize());
        OrderInfoDto infoDto = orderItemRepositorySupport.searchOrderList(buyerCode, staffId, corpName, staffName, startDate, endDate, searchKeyword, orderState, pageable);
        List<OrderItemDto> orderItemDtoList = this.addFileToOrderItemList(infoDto.getOrderItemList());

        return new PageImpl<>(orderItemDtoList, pageable, infoDto.getListCnt());
    }

    /**
     * 주문 상세 조회. 주문 한 건에 귀속 된 주문 아이템 정보.
     */
    public OrderInfoDto getOrderDetail(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("orderInfoSeq")) { // 값이 없으면
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "orderInfoSeq");
        }
        Long orderInfoSeq = Long.valueOf(map.get("orderInfoSeq").toString());

        // 사용자일때는 최상위 계정 정보 가져오기. 관리자일때는 상관없이 조회.
        String buyerCode = null;
        if (Objects.equals(SessionUtil.getUserType(), "buyer")) {
            buyerCode = SessionUtil.getPreUserCode();
        }

        OrderInfoDto infoDto = orderInfoRepositorySupport.getOrderInfo(orderInfoSeq, null, buyerCode, Arrays.asList());
        List<OrderItemDto> itemDtoList = this.getOrderItemList(infoDto.getSeq(), buyerCode);
        infoDto.setOrderItemList(this.addFileToOrderItemList(itemDtoList));
        return infoDto;
    }

    /**
     * 결제 완료 + 적립 포인트 내역 생성
     */
    public Map<String, String> successOrderPay(Map<String, Object> returnMap) {

        Map<String, Object> payResultMap = null;
        try {
            // 결제 금액 검증 - 'orderinfo에 있는 amount'와 'request로 넘어온 승인요청금액 + 포인트'의 합을 비교
            OrderInfoDto infoDto = orderInfoRepositorySupport.getOrderInfo(null, returnMap.get("orderno").toString(), null, Arrays.asList(OrderStateEnum.ORDER_STANDBY.getCode()));

            String buyerIdentificationCode = returnMap.get("reserved02").toString();
            BuyerIdentificationDto buyerIdentificationDto = buyerService.getBuyerInfoByIdentificationCode(buyerIdentificationCode); // SessionUtil.getUserCode()
            String buyerCode = buyerIdentificationDto.getBuyer().getBuyerCode();
            PointDto pointDto = pointService.getBuyerPointByBuyerCode(buyerCode);

            if (pointDto.getSavePoint() < Integer.parseInt(returnMap.get("reserved01").toString())) {
                log.error("[successOrderPay-ERROR] >>>> 결제 금액 검증 오류(사용 가능 포인트 초과) : {}, {} ", returnMap, infoDto.getAmount());

                infoDto.setOrderItemList(this.getOrderItemList(infoDto.getSeq(), null));

                List<OrderItemDto> orderItemDtoList = infoDto.getOrderItemList();
                List<OrderItem> updateItemList = new ArrayList<>();
                for (OrderItemDto orderItemDto : orderItemDtoList) {
                    updateItemList.add(orderItemDto.updateOrderStateEntity(OrderStateEnum.ORDER_ERROR.getEnCode(), "ERROR"));
                }

                // 가지도 못한 내용 저장
                orderItemRepository.saveAll(updateItemList);
                orderInfoRepository.save(infoDto.updateError(OrderStateEnum.ORDER_ERROR.getCode(), "error", "전 주문 실패."));

                Map<String, String> resultData = new HashMap<>();
                resultData.put("orderState", OrderStateEnum.ORDER_ERROR.getEnCode());
                return resultData;
            }

            int amt = 0;
            if (returnMap.containsKey("orderType") && Objects.equals("pointOnly", returnMap.get("orderType"))) {
                amt = Integer.parseInt(returnMap.get("approvamt").toString());
            } else {
                amt = Integer.parseInt(returnMap.get("buy_reqamt").toString());
            }

            int requestTotalAmount = amt + Integer.parseInt(returnMap.get("reserved01").toString());
            if (!Objects.equals(infoDto.getAmount(), requestTotalAmount)) {
                log.error("[successOrderPay-ERROR] >>>> 결제 금액 검증 오류(총금액 불일치) : {}, {} ", returnMap, infoDto.getAmount());

                infoDto.setOrderItemList(this.getOrderItemList(infoDto.getSeq(), null));

                List<OrderItemDto> orderItemDtoList = infoDto.getOrderItemList();
                List<OrderItem> updateItemList = new ArrayList<>();
                for (OrderItemDto orderItemDto : orderItemDtoList) {
                    updateItemList.add(orderItemDto.updateOrderStateEntity(OrderStateEnum.ORDER_ERROR.getEnCode(), "ERROR"));
                }

                // 가지도 못한 내용 저장
                orderItemRepository.saveAll(updateItemList);
                orderInfoRepository.save(infoDto.updateError(OrderStateEnum.ORDER_ERROR.getCode(), "error", "전 주문 실패."));

                Map<String, String> resultData = new HashMap<>();
                resultData.put("orderState", OrderStateEnum.ORDER_ERROR.getEnCode());
                return resultData;
            }

            log.info("결제 인증 응답 : {}", returnMap);
            if (returnMap.containsKey("orderType") && Objects.equals("pointOnly", returnMap.get("orderType"))) {
                payResultMap = returnMap;
            } else {
                payResultMap = XXService.paymentReturn(returnMap);
            }

            if (!ObjectUtils.isEmpty(payResultMap)) {

                log.info("결제 승인 응답 : {}", payResultMap);

                String resultCode = payResultMap.get("result_code").toString(); // 가맹점 결과 코드
                String resultMsg = payResultMap.get("result_msg").toString(); // 가맹점 결과 메시지
                String dResultCode = payResultMap.get("dresult_code").toString(); // 사용자 결과 코드
                String dResultMsg = payResultMap.get("dresult_msg").toString(); // 사용자 결과 메시지

                // 승인 실패의 경우 - 취소할 내용 없음. 관리자에서 내역 관리만.
                if (!(Objects.equals("EC0000", resultCode) && Objects.equals("EC0000", dResultCode))) { // 결제 실패로 응답.
                    log.error("[successOrderPay-ERROR] >>>> 실패 응답코드 [" + dResultCode + "] : {} ", payResultMap);

                    String orderNo = returnMap.get("orderno").toString(); // 주문 번호

//                    OrderInfoDto infoDto = orderInfoRepositorySupport.getOrderInfo(null, orderNo, null, Arrays.asList(OrderStateEnum.ORDER_STANDBY.getCode()));
                    infoDto.setOrderItemList(this.getOrderItemList(infoDto.getSeq(), null));

                    List<OrderItemDto> orderItemDtoList = infoDto.getOrderItemList();
                    List<OrderItem> updateItemList = new ArrayList<>();
                    for (OrderItemDto orderItemDto : orderItemDtoList) {
                        updateItemList.add(orderItemDto.updateOrderStateEntity(OrderStateEnum.ORDER_ERROR.getEnCode(), "ERROR"));
                    }

                    // 실패로 응답 온 내역 저장.
                    orderItemRepository.saveAll(updateItemList); // 주문 아이템 상태 업데이트 값 저장.
                    orderInfoRepository.save(infoDto.updateError(OrderStateEnum.ORDER_ERROR.getCode(), resultCode, "[실패 응답]" + resultMsg));

                    Map<String, String> resultData = new HashMap<>();
                    resultData.put("orderState", OrderStateEnum.ORDER_ERROR.getEnCode());
                    return resultData;
                }

                // 승인 완료 일때만
                String tid = payResultMap.get("tid").toString(); // 원결제 거래번호
                String payMethod = payResultMap.get("pay_method").toString(); // 결제수단코드
                int amount = Integer.valueOf(payResultMap.get("approvamt").toString()); // 금액
                String orderNo = payResultMap.get("orderno").toString(); // 주문 번호
                String reserved01 = payResultMap.get("reserved01").toString(); // 포인트 사용금액
                String reserved02 = payResultMap.get("reserved02").toString(); // 가맹점 예약 필드 2

//                OrderInfoDto infoDto = orderInfoRepositorySupport.getOrderInfo(null, orderNo, null, Arrays.asList(OrderStateEnum.ORDER_STANDBY.getCode()));
                infoDto.setOrderItemList(this.getOrderItemList(infoDto.getSeq(), null));

//                String buyerIdentificationCode = reserved02;
//                BuyerIdentificationDto buyerIdentificationDto = buyerService.getBuyerInfoByIdentificationCode(buyerIdentificationCode); // SessionUtil.getUserCode()
//                String buyerCode = buyerIdentificationDto.getBuyer().getBuyerCode();

                List<OrderItemDto> orderItemDtoList = infoDto.getOrderItemList();
                List<Long> productSeqList = new ArrayList<>();
                List<OrderItem> updateItemList = new ArrayList<>();
                for (OrderItemDto orderItemDto : orderItemDtoList) {
                    updateItemList.add(orderItemDto.updateOrderStateEntity(OrderStateEnum.PAY_DONE.getEnCode(), buyerIdentificationCode));
                    productSeqList.add(orderItemDto.getProductSeq());
                }

                // 주문 결제 성공시!
                int usePoint = Integer.valueOf(reserved01);


                // 장바구니 - 내역 삭제로 업데이트.
                List<CartDto> cartDtoList = Optional.ofNullable(cartRepositorySupport.findCartByProductSeqList(productSeqList, buyerCode))
                        .orElseThrow(() -> new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "cart list"));
                cartRepository.saveAll(cartDtoList.stream().map(list -> list.updateDelY(buyerIdentificationCode)).collect(Collectors.toList()));

                // 포인트 적립 예정 생성.
                PointDto nowPoint = pointService.getBuyerPointByBuyerCode(buyerCode);
                nowPoint = pointService.payDoneExpectPoint(infoDto, nowPoint, infoDto.getAmount(), buyerIdentificationCode);
                pointService.payDoneUsePoint(infoDto, nowPoint, usePoint, infoDto.getAmount(), buyerIdentificationCode);

                //재고관리
                for (OrderItemDto dto : orderItemDtoList) {
                    Long productSeq = dto.getProductSeq();
                    productService.quantityManager(productSeq, -(dto.getOrderQuantity()));
                }

                // 주문 - 결제성공으로 업데이트.
                orderItemRepository.saveAll(updateItemList); // 주문 아이템 상태 업데이트 값 저장.
                orderInfoRepository.save(infoDto.updateRes(OrderStateEnum.PAY_DONE.getCode(), amount, usePoint, tid, payMethod, resultCode, resultMsg, buyerIdentificationCode)); // 주문 상태 업데이트 값 + PG 응답 저장.

                // 주문내역 이메일 발송
                emailService.sendForItemPurchase(buyerIdentificationDto.getBuyer().getCorpEmail(), buyerIdentificationDto.getBuyer().getCorpName(), returnMap.get("orderno").toString(),
                        infoDto.getOrderName(), reserved01, String.valueOf(amount), "0");

                Map<String, String> resultData = new HashMap<>();
                resultData.put("corpName", infoDto.getCorpName());
                resultData.put("orderNo", infoDto.getOrderNo());
                resultData.put("orderState", OrderStateEnum.PAY_DONE.getEnCode());
                return resultData;

            } else {

                log.error("[successOrderPay-ERROR] >>>> 통신 실패 : {} ", payResultMap);

                String orderNo = returnMap.get("orderno").toString(); // 주문 번호

//                OrderInfoDto infoDto = orderInfoRepositorySupport.getOrderInfo(null, orderNo, null, Arrays.asList(OrderStateEnum.ORDER_STANDBY.getCode()));
                infoDto.setOrderItemList(this.getOrderItemList(infoDto.getSeq(), null));

                List<OrderItemDto> orderItemDtoList = infoDto.getOrderItemList();
                List<OrderItem> updateItemList = new ArrayList<>();
                for (OrderItemDto orderItemDto : orderItemDtoList) {
                    updateItemList.add(orderItemDto.updateOrderStateEntity(OrderStateEnum.ORDER_ERROR.getEnCode(), "ERROR"));
                }

                // 가지도 못한 내용 저장
                orderItemRepository.saveAll(updateItemList);
                orderInfoRepository.save(infoDto.updateError(OrderStateEnum.ORDER_ERROR.getCode(), "error", "전 주문 실패."));

                Map<String, String> resultData = new HashMap<>();
                resultData.put("orderState", OrderStateEnum.ORDER_ERROR.getEnCode());
                return resultData;
            }

        } catch (Exception e) {

            log.error("[successOrderPay-ERROR] >>>> 성공 이후 실패 : {} ", payResultMap.toString());

            String orderNo = returnMap.get("orderno").toString();

            OrderInfoDto infoDto = orderInfoRepositorySupport.getOrderInfo(null, orderNo, null, Arrays.asList(OrderStateEnum.ORDER_STANDBY.getCode()));
            infoDto.setOrderItemList(this.getOrderItemList(infoDto.getSeq(), null));

            String tid = payResultMap.get("tid").toString();
            String paymentMethod = payResultMap.get("pay_method").toString();
            int approveAmount = Integer.valueOf(payResultMap.get("approvamt").toString());
            int usePoint = Integer.valueOf(payResultMap.get("reserved01").toString());
            String tradeResult = payResultMap.get("result_code").toString(); // 가맹점 결과 코드
            String tradeResultMsg = payResultMap.get("result_msg").toString(); // 가맹점 결과 메시지


            List<OrderItemDto> orderItemDtoList = infoDto.getOrderItemList();
            for (OrderItemDto orderItemDto : orderItemDtoList) {
//              // 취소하다가 실패함.
                orderItemRepository.save(orderItemDto.updateOrderStateEntity(OrderStateEnum.PAYMENT_ERROR.getEnCode(), "ERROR"));
            }

            // 취소하다가 실패함.
            orderInfoRepository.save(infoDto.updateRes(OrderStateEnum.PAYMENT_ERROR.getCode(), approveAmount, usePoint, tid, paymentMethod, tradeResult, tradeResultMsg, "ERROR"));

            Map<String, String> resultData = new HashMap<>();
            resultData.put("orderState", OrderStateEnum.PAYMENT_ERROR.getEnCode());
            return resultData;
        }

    }

    // 구매확정  -> 보유포인트(증가), 적립예정포인트(감소)
    public void orderConfirm(String orderStateEnCode, OrderItemDto orderItemDto) {
        // 아이템 업데이트
        orderItemRepository.save(orderItemDto.updateOrderStateEntity(orderStateEnCode, SessionUtil.getUserCode()));
        PointDto nowPoint = pointService.getBuyerPointByBuyerCode(orderItemDto.getOrderInfo().getBuyerCode());
        // 포인트 적립
        pointService.orderConfirmPoint(orderItemDto, nowPoint, orderItemDto.getOrderInfo().getAmount());
    }

    //  배송중
    public void modifyOrderDelivery(String orderStateEnCode, OrderItemDto oldItemDto, OrderItemDto newItemDto) {
        oldItemDto.setDeliveryNo(newItemDto.getDeliveryNo());
        oldItemDto.setDeliveryCompany(newItemDto.getDeliveryCompany());
        orderItemRepository.save(oldItemDto.updateOrderStateEntity(orderStateEnCode, SessionUtil.getUserCode()));
    }

    // 취소신청, 환불신청, 배송완료 -> 포인트 변동 없음.
    public void modifyOrderState(String orderStateEnCode, OrderItemDto orderItemDto) {
        orderItemRepository.save(orderItemDto.updateOrderStateEntity(orderStateEnCode, SessionUtil.getUserCode()));
    }

    // 취소승인, 환불승인 -> 사용포인트(감소), 보유포인트(증가) || 적립예정포인트(감소) -- 히스토리 두번 쌓기.(사용취소, 적립예정취소)
    public void orderCancelOrRefund(String orderStateEnCode, OrderItemDto orderItemDto, String newPaymentMethod) {
        log.info("==========[취소 로직 시작]=========================");

        log.info("========= 취소할 주문의 결제 수단 ======================== {}", newPaymentMethod);
        int amount = orderItemDto.getOrderInfo().getAmount();
        log.info("========= 총 결제 금액 ======================== {}", amount);
        int approveAmount = orderItemDto.getOrderInfo().getApproveAmount();
        log.info("========= " + newPaymentMethod + " 승인 금액 ====================== {}", approveAmount);
        int cancelRequestAmount = orderItemDto.getOrderItemAmount();
        log.info("========= 취소 요청 금액 ====================== {}", cancelRequestAmount);
        int cancelAmount = orderItemDto.getOrderInfo().getCancelAmount();
        log.info("========= " + newPaymentMethod + " 취소 누적 금액 ================== {}", cancelAmount);
        int availableAmount = approveAmount - cancelAmount;
        log.info("========= " + newPaymentMethod + " 취소 가능 금액 ================== {}", availableAmount);
        boolean cardCancel = Objects.equals(availableAmount, 0) ? false : (availableAmount >= cancelRequestAmount);
        log.info("========= " + newPaymentMethod + " 취소 가능 여부 ================== {}", cardCancel);

        String tid = orderItemDto.getOrderInfo().getOrgOrderNo();
        int upCancelAmount = cancelAmount;
        int upCancelPoint = orderItemDto.getOrderInfo().getCancelPoint();
        OrderInfoDto orderInfoDto = orderItemDto.getOrderInfo();
        PointDto nowPoint = pointService.getBuyerPointByBuyerCode(orderInfoDto.getBuyerCode());

        if (!Objects.equals(newPaymentMethod, "VA")) {
            if (cardCancel) {// 카드취소만으로 가능
                Map<String, Object> payResultMap = XXService.paymentCancel(orderInfoDto.getPaymentMethod(), String.valueOf(cancelRequestAmount), tid);
                log.info("========= 결제취소 승인 응답 : {}", payResultMap);
                upCancelAmount = cancelAmount + cancelRequestAmount;
                log.info("========= 카드 취소 누적 금액 ================== {}", upCancelAmount);

            } else {// 카드취소만으로는 불가능
                if (availableAmount > 0) { // 카드 취소 금액이 있을때는 호출
                    Map<String, Object> payResultMap = XXService.paymentCancel(orderInfoDto.getPaymentMethod(), String.valueOf(availableAmount), tid);
                    log.info("====================== 결제 취소 시작 =========================");
                    log.info("========= 결제취소 승인 응답 : {}", payResultMap);
                    int realCancelAmt = Integer.valueOf(payResultMap.get("cancel_amt").toString());
                    log.info("========= 취소 승인 금액 ====================== {}", realCancelAmt);
                    upCancelAmount = cancelAmount + realCancelAmt;
                    log.info("========= 카드 취소 누적 금액 ================== {}", upCancelAmount);
                }

                // 포인트 취소 시작
                log.info("====================== 포인트 취소 시작 =========================");
                int requestSavePoint = cancelRequestAmount - availableAmount;
                log.info("========= 남은 취소 요청 금액 ================== {}", requestSavePoint);
                int usePoint = orderItemDto.getOrderInfo().getUsePoint();
                log.info("========= 사용 포인트 ========================= {}", usePoint);
                log.info("========= 누적 취소 포인트(전) ===================== {}", upCancelPoint);
                int availablePoint = usePoint - upCancelPoint;
                log.info("========= 취소 가능 포인트 ===================== {}", availablePoint);
                boolean pointCancel = Objects.equals(availablePoint, 0) ? false : (availablePoint >= requestSavePoint);
                log.info("========= 포인트 환급 가능 여부 ================= {}", availablePoint);


                if (!pointCancel) {
                    throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "지급 가능한 포인트 범위를 초과했습니다.");
                }

                // 취소에 따른 포인트 재적립.
                nowPoint = pointService.cancelDoneUsePoint(orderItemDto, nowPoint, requestSavePoint, amount);
                upCancelPoint = upCancelPoint + requestSavePoint;
                log.info("========= 누적 취소 포인트(후) ===================== {}", upCancelPoint);
            }
        } else {
            if (cardCancel) {
                String buyerCode = orderInfoDto.getBuyerCode();
                Optional<BuyerVaccount> buyerVaccountOptional = buyerVaccountRepository.findByBuyerCode(buyerCode);
                if (buyerVaccountOptional.isEmpty()) {
                    log.error("not exists buyer - buyerCode: {}", buyerCode);
                    throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "서비스에 등록되지 않은 사용자입니다.");
                }
                BuyerVaccount bv = buyerVaccountOptional.get();
                try {
                    upCancelAmount = cancelAmount + cancelRequestAmount;
                    log.info("========= 취소 누적 금액 ================== {}", upCancelAmount);
                } catch (Exception e) {
                    log.error("Transaction ERROR - {}", e);
                    throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUESTED_FAIL, "결제 취소 요청이 실패하였습니다.");
                }
            } else {// 취소만으로는 불가능
                if (availableAmount > 0) { // 취소 금액이 있을때는 호출
                    String buyerCode = orderInfoDto.getBuyerCode();
                    Optional<BuyerVaccount> buyerVaccountOptional = buyerVaccountRepository.findByBuyerCode(buyerCode);
                    if (buyerVaccountOptional.isEmpty()) {
                        log.error("not exists buyer - buyerCode: {}", buyerCode);
                        throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "서비스에 등록되지 않은 사용자입니다.");
                    }
                    BuyerVaccount bv = buyerVaccountOptional.get();
                    try {
                        upCancelAmount = cancelAmount + availableAmount;
                        log.info("========= 취소 누적 금액 ================== {}", upCancelAmount);
                    } catch (Exception e) {
                        log.error("Transaction ERROR - {}", e);
                        throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUESTED_FAIL, "결제 취소 요청이 실패하였습니다.");
                    }
                }

                // 포인트 취소 시작
                log.info("====================== 포인트 취소 시작 =========================");
                int requestSavePoint = cancelRequestAmount - availableAmount;
                log.info("========= 남은 취소 요청 금액 ================== {}", requestSavePoint);
                int usePoint = orderItemDto.getOrderInfo().getUsePoint();
                log.info("========= 사용 포인트 ========================= {}", usePoint);
                log.info("========= 누적 취소 포인트(전) ===================== {}", upCancelPoint);
                int availablePoint = usePoint - upCancelPoint;
                log.info("========= 취소 가능 포인트 ===================== {}", availablePoint);
                boolean pointCancel = Objects.equals(availablePoint, 0) ? false : (availablePoint >= requestSavePoint);
                log.info("========= 포인트 환급 가능 여부 ================= {}", availablePoint);


                if (!pointCancel) {
                    throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "지급 가능한 포인트 범위를 초과했습니다.");
                }

                // 취소에 따른 포인트 재적립.
                nowPoint = pointService.cancelDoneUsePoint(orderItemDto, nowPoint, requestSavePoint, amount);
                upCancelPoint = upCancelPoint + requestSavePoint;
                log.info("========= 누적 취소 포인트(후) ===================== {}", upCancelPoint);
            }
        }
        int upInfoSavePoint = orderItemDto.getOrderInfo().getSavePoint();
        int itemPointSave = orderItemDto.getPointSave();
        log.info("========= 적립되어야했던 포인트 ================= {}", upInfoSavePoint);
        log.info("========= 적립에서 제외할 포인트 ================= {}", itemPointSave);
        upInfoSavePoint = upInfoSavePoint - itemPointSave;
        log.info("========= 업데이트 될 적립 포인트 ================= {}", upInfoSavePoint);

        // 주문 정보 업데이트
        orderInfoDto.setCancelAmount(upCancelAmount);
        orderInfoDto.setCancelPoint(upCancelPoint);
        orderInfoDto.setSavePoint(upInfoSavePoint);
        orderInfoRepository.save(orderInfoDto.updateOrderInfo());
        log.info("==============================================");
        log.info("========= 업데이트 총 취소 금액 ================= {}", upCancelAmount);
        log.info("========= 업데이트 총 취소 포인트 =============== {}", upCancelPoint);
        log.info("========= 업데이트 적립 포인트 =============== {}", upInfoSavePoint);
        log.info("==============================================");


        // 주문 아이템 업데이트
        orderItemDto.setCancelYn("Y");
        orderItemRepository.save(orderItemDto.updateOrderStateEntity(orderStateEnCode, SessionUtil.getUserCode()));

        // 취소에 따른 포인트 적립예정 취소.
        pointService.cancelDoneExpectPoint(orderItemDto, nowPoint, amount);

        // 상품 재고관리
        productService.quantityManager(orderItemDto.getProductSeq(), orderItemDto.getOrderQuantity());

        // 취소 or 환불시 이메일 전송
        if (orderStateEnCode.equals(OrderStateEnum.REFUND_DONE.getEnCode()))
            emailService.sendForItemPurchaseRefund(orderInfoDto.getCorpEmail(), orderInfoDto.getStaffName(), orderInfoDto.getOrderNo(), orderItemDto.getProductName(),
                    String.valueOf(upCancelPoint), String.valueOf(upCancelAmount), newPaymentMethod);
        if (orderStateEnCode.equals(OrderStateEnum.CANCEL_DONE.getEnCode()))
            emailService.sendForItemPurchaseCancel(orderInfoDto.getCorpEmail(), orderInfoDto.getStaffName(), orderInfoDto.getOrderNo(), orderItemDto.getProductName(),
                    String.valueOf(upCancelPoint), String.valueOf(upCancelAmount), newPaymentMethod);
    }

    /**
     * 배송지 수정.
     */
    public void modifyShippingAddress(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("orderNo") || !map.containsKey("orderMemo")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "orderNo or orderMemo");
        }

        String shippingAddress;
        String shippingAddressPostNo;
        String shippingAddressDetail;
        String shippingTel = null;

        BuyerIdentificationDto identificationDto = buyerService.getBuyerInfoByIdentificationCode(SessionUtil.getUserCode());

        if (map.containsKey("shippingAddress")
                && map.containsKey("shippingAddressPostNo")
                && map.containsKey("shippingAddressDetail")
                && map.containsKey("shippingTel")) {
            shippingAddress = map.get("shippingAddress").toString();
            shippingAddressPostNo = map.get("shippingAddressPostNo").toString();
            shippingAddressDetail = map.get("shippingAddressDetail").toString();
            shippingTel = map.get("shippingTel").toString();
        } else {
            shippingAddress = identificationDto.getBuyer().getCorpShippingAddress();
            shippingAddressPostNo = identificationDto.getBuyer().getShippingAddressPostNo();
            shippingAddressDetail = identificationDto.getBuyer().getShippingAddressDetail();
            shippingTel = identificationDto.getBuyer().getCorpTelNo();
        }

        String orderNo = map.get("orderNo").toString();
        String orderMemo = map.get("orderMemo").toString();

        List<Integer> list = new ArrayList<>();
        list.add(OrderStateEnum.ORDER_STANDBY.getCode());
        list.add(OrderStateEnum.PAY_DONE.getCode());

        OrderInfoDto infoDto = orderInfoRepositorySupport.getOrderInfo(null, orderNo, identificationDto.getBuyer().getBuyerCode(), list);

        if (ObjectUtils.isEmpty(infoDto)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "order info");
        }
        if (!Objects.equals(OrderStateEnum.ORDER_STANDBY.getEnCode(), infoDto.getOrderInfoState())
                && !Objects.equals(OrderStateEnum.PAY_DONE.getEnCode(), infoDto.getOrderInfoState())) { // 여기에 결제 완료도 추가하면 배송전까지 변경 가능.
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "NOT_MATCH", "order state");
        }
        // buyerType이 아니면 배송지 변경 안함.
        if (Objects.equals(BuyerTypeConstants.WHOLESALE, identificationDto.getBuyer().getBuyerType())) {
            infoDto.setShippingAddress(shippingAddress);
            infoDto.setShippingAddressPostNo(shippingAddressPostNo);
            infoDto.setShippingAddressDetail(shippingAddressDetail);
            infoDto.setCorpTelNo(shippingTel);
        }
        infoDto.setOrderMemo(orderMemo);
        orderInfoRepository.save(infoDto.updateOrderInfo());
    }

    /**
     * 관리자의 주문 상태 변경 요청.
     */
    public void modifyOrderInfoByAdmin(ApiRequest apiRequest) {

        OrderInfoDto orderInfoDto = ObjectMapperUtil.requestConvertDto(apiRequest, OrderInfoDto.class);

        // OrderInfo의 변경된 데이터 저장 - 세금계산서 값 변경용.
        if (!ObjectUtils.isEmpty(orderInfoDto) && !ObjectUtils.isEmpty(orderInfoDto.getSeq()) && !ObjectUtils.isEmpty(orderInfoDto.getTaxbillYn())) {
            OrderInfoDto orderInfoDtoForUpdate = orderInfoRepositorySupport.getOrderInfo(orderInfoDto.getSeq(), null, null, Arrays.asList());
            orderInfoDtoForUpdate.setTaxbillYn(orderInfoDto.getTaxbillYn());
            orderInfoRepository.save(orderInfoDtoForUpdate.updateOrderInfo());
        } else {
            log.info("데이터 누락으로 세금계산서 변경은 처리되지 않았습니다. - seq: {}, taxbillYn: {}", orderInfoDto.getSeq(), orderInfoDto.getTaxbillYn());
        }

        List<OrderItemDto> orderItemDtoList = orderInfoDto.getOrderItemList();

        for (OrderItemDto newItemDto : orderItemDtoList) {

            if (ObjectUtils.isEmpty(newItemDto.getSeq()) || newItemDto.getSeq() <= 0) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "seq");
            }

            // 현재 DB 주문 내역
            OrderItemDto oldItemDto = orderItemRepositorySupport.getOrderItemWithOrderInfo(newItemDto.getSeq(), null);
            if (ObjectUtils.isEmpty(oldItemDto)) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "order item");
            }

            String oldEnCode = oldItemDto.getOrderItemState();
            String newEnCode = newItemDto.getOrderItemState();

            // [이전 상태 : 결제완료 / 변경 상태 : 빈값] (=배송중으로 세팅)
            if ((Objects.equals(OrderStateEnum.PAY_DONE.getEnCode(), oldEnCode) || Objects.equals(OrderStateEnum.SHIPPING.getEnCode(), oldEnCode))
                    && ObjectUtils.isEmpty(newEnCode)
                    && (!ObjectUtils.isEmpty(newItemDto.getDeliveryCompany()) && !ObjectUtils.isEmpty(newItemDto.getDeliveryNo()))) {
                newEnCode = OrderStateEnum.SHIPPING.getEnCode();
            }

            // 변경 할 상태 없으면 Exception
            if (ObjectUtils.isEmpty(newEnCode)) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "orderItemState");
            }

            // 상태가 같으면 변경에서 제외. 단, shipping -> shipping 으로 변경은 가능.
            if (!Objects.equals(OrderStateEnum.SHIPPING.getEnCode(), newEnCode)) {
                if (Objects.equals(newEnCode, oldEnCode)) {
                    continue;
                }
            }

            // admin 이 변경할 수 있는 상태인지 체크.
            OrderStateEnum.checkApproveStateAdmin(oldEnCode, newEnCode);

            // 배송중으로 변경하려고 할때. 송장번호, 택배사 체크.
            if (Objects.equals(newEnCode, OrderStateEnum.SHIPPING.getEnCode())) {

                // 택배사가 없는 경우. 필수 값 오류.
                String deliveryCompany = !ObjectUtils.isEmpty(newItemDto.getDeliveryCompany()) ? newItemDto.getDeliveryCompany() : oldItemDto.getDeliveryCompany();
                deliveryCompany = deliveryCompany.replaceAll(" ", "");

                if (ObjectUtils.isEmpty(deliveryCompany)) { // 배송중으로 변경하려고 하는데 택배사가 빈값으로 온 경우.
                    throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "deliveryCompany");
                } else {
                    newItemDto.setDeliveryCompany(deliveryCompany);
                }

                // 송장번호가 없는 경우(직접배송 제외). 필수 값 오류.
                String deliveryNo = "";
                if (!Objects.equals("직접배송", deliveryCompany)) {
                    if (ObjectUtils.isEmpty(newItemDto.getDeliveryNo())) { // 직접 배송이 아닌데 송장번호가 없는 경우 오류 메시지.
                        throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "deliveryNo");
                    }
                    deliveryNo = newItemDto.getDeliveryNo().replaceAll("[^0-9]", "");
                }
                newItemDto.setDeliveryNo(deliveryNo);
            }

            // 상태별 업데이트 호출.
            this.modifyOrderInfoByState(oldEnCode, newEnCode, oldItemDto, newItemDto);
        }

    }

    /**
     * 회원의 주문 상태 변경 요청.
     */
    public void modifyOrderInfoByBuyer(ApiRequest apiRequest) {

        OrderInfoDto orderInfoDto = ObjectMapperUtil.requestConvertDto(apiRequest, OrderInfoDto.class);

        List<OrderItemDto> orderItemDtoList = orderInfoDto.getOrderItemList();

        for (OrderItemDto dto : orderItemDtoList) {
            if (ObjectUtils.isEmpty(dto.getSeq()) || ObjectUtils.isEmpty(dto.getOrderItemState())) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "seq or orderItemState");
            }
            Long seq = dto.getSeq();
            String newEnCode = dto.getOrderItemState();

            OrderStateEnum.getCodeByEnCode(newEnCode);

            OrderItemDto orderItemDto = orderItemRepositorySupport.getOrderItemWithOrderInfo(seq, SessionUtil.getPreUserCode());
            if (ObjectUtils.isEmpty(orderItemDto)) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "order item");
            }
            String oldEnCode = orderItemDto.getOrderItemState(); // 기존상태

            // 상태가 같으면 변경에서 제외. 바뀔 상태가 shipping 인 경우 변경 가능.
            if (Objects.equals(newEnCode, oldEnCode)
                    && !(Objects.equals(OrderStateEnum.SHIPPING.getEnCode(), newEnCode))) {
                continue;
            }

            // buyer 변경하려는 상태에 대한 기본 체크.
            OrderStateEnum.checkApproveStateBuyer(oldEnCode, newEnCode);

            OrderItemDto newItemDto = new OrderItemDto();
            newItemDto.setOrderItemState(newEnCode);
            newItemDto.setSeq(seq);

            // 상태별 업데이트 호출.
            this.modifyOrderInfoByState(oldEnCode, newEnCode, orderItemDto, newItemDto);
        }

    }

    /**
     * 주문 아이템 상태 변경 + 포인트 변동 + 결제 취소
     */
    private void modifyOrderInfoByState(String oldEnCode, String newEnCode, OrderItemDto oldItemDto, OrderItemDto newItemDto) {

        // 취소승인, 환불승인, 구매확정 건은 다른상태로 변경하거나 되돌릴 수 없음.
        if (Objects.equals(oldEnCode, OrderStateEnum.CANCEL_DONE.getEnCode())
                || Objects.equals(oldEnCode, OrderStateEnum.REFUND_DONE.getEnCode())
                || Objects.equals(oldEnCode, OrderStateEnum.ORDER_CONFIRM.getEnCode())
        ) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "상태변경을 할 수 없는 주문건 입니다. [" + oldEnCode + "]");
        }


        if (Objects.equals(newEnCode, OrderStateEnum.ORDER_CONFIRM.getEnCode())) { // 구매확정
            this.orderConfirm(newEnCode, oldItemDto);

        } else if (Objects.equals(newEnCode, OrderStateEnum.SHIPPING.getEnCode())) { // 배송중
            this.modifyOrderDelivery(newEnCode, oldItemDto, newItemDto);

        } else if (Objects.equals(newEnCode, OrderStateEnum.CANCEL_REQUEST.getEnCode())
                || Objects.equals(newEnCode, OrderStateEnum.REFUND_REQUEST.getEnCode())
                || Objects.equals(newEnCode, OrderStateEnum.PAY_DONE.getEnCode())
                || Objects.equals(newEnCode, OrderStateEnum.DELIVERY_COMPLETED.getEnCode())) { // 취소신청, 환불신청, 배송중, 배송완료 : 단순 업데이트
            this.modifyOrderState(newEnCode, oldItemDto);

        } else if (Objects.equals(newEnCode, OrderStateEnum.CANCEL_DONE.getEnCode())
                || Objects.equals(newEnCode, OrderStateEnum.REFUND_DONE.getEnCode())) { // 취소승인, 환불승인
            this.orderCancelOrRefund(newEnCode, oldItemDto, newItemDto.getOrderInfo().getPaymentMethod());

        } else {// 그 외
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "변경 할 수 없는 상태 값.", newEnCode);
        }

    }


    /**
     * 주문 한 건에 묶여있는 아이템 리스트 조회.
     */
    private List<OrderItemDto> getOrderItemList(Long orderInfoSeq, String buyerCode) {
        return orderItemRepositorySupport.getOrderItemByInfoSeq(orderInfoSeq, buyerCode);
    }

    /**
     * 회원 삭제를 위해 주문 진행중인 아이템 리스트 조회.
     */
    public List<OrderItemDto> getOrderItemListForBuyerDelete(String buyerCode) {
        return orderItemRepositorySupport.getOrderItemByBuyerCodeAndStateList(buyerCode, OrderStateEnum.searchOrderInProgress());
    }

    /**
     * 주문 아이템 별 파일 정보 추가.
     */
    public List<OrderItemDto> addFileToOrderItemList(List<OrderItemDto> orderItemDtoList) {
        List<OrderItemDto> resultList = new ArrayList<>();
        for (OrderItemDto dto : orderItemDtoList) {
            Long productSeq = dto.getProductSeq();
            List<FileManagerDto> fileList = awsFileService.getFileListByRelationSeqAndFileTypes(productSeq, FileTypeEnum.getProductMainFileType());
            if (!ObjectUtils.isEmpty(fileList)) {
                dto.setFileList(fileList);
            }
            resultList.add(dto);
        }
        return resultList;
    }

    /**
     * 매출 해더 연산을 위한 리스트 조회.
     */
    public List<OrderInfoDto> searchSalesHeaderList(SalesSearchDto searchDto) {
        return orderInfoRepositorySupport.searchSalesHeaderList(searchDto);
    }

    /**
     * 매출 리스트 조회.
     */
    public Page<OrderInfoDto> searchSalesList(SalesSearchDto searchDto, Pageable pageable) {
        return orderInfoRepositorySupport.searchSalesList(searchDto, pageable);
    }


    /**
     * 결제 시, 처리 로직
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public OrderInfoDto orderFor(int amount, String orderNo, int usePoint) throws Exception {

        if (amount <= 0 || ObjectUtils.isEmpty(orderNo)) {
            throw new Exception("결제정보가 유효하지 않습니다.");
        }

        String buyerCode = buyerService.getBuyerCode();
        // todo 결제할때 들어가는 정보는 현재 로그인한 사용자의 bi 값
        BuyerIdentificationDto buyerIdentificationDto = buyerService.getBuyerIdentificationByBuyerCode(buyerCode);
        String buyerIdentificationCode = buyerIdentificationDto.getBuyerIdentificationCode();

        OrderInfoDto infoDto = orderInfoRepositorySupport.getOrderInfo(null, orderNo, buyerCode, Arrays.asList(OrderStateEnum.ORDER_STANDBY.getCode()));

        if (!Objects.equals(infoDto.getAmount() - usePoint, amount)) {
            log.error("invalid amout: requested: {}, actual: {} ", amount, infoDto.getAmount());
            throw new Exception("결제 요청 금액이 유효하지 않습니다.");
        }

        infoDto.setOrderItemList(this.getOrderItemList(infoDto.getSeq(), null));

        List<OrderItemDto> orderItemDtoList = infoDto.getOrderItemList();
        List<Long> productSeqList = new ArrayList<>();
        List<OrderItem> updateItemList = new ArrayList<>();
        for (OrderItemDto orderItemDto : orderItemDtoList) {
            updateItemList.add(orderItemDto.updateOrderStateEntity(OrderStateEnum.PAY_DONE.getEnCode(), buyerIdentificationCode));
            productSeqList.add(orderItemDto.getProductSeq());
        }

        // 장바구니 - 내역 삭제로 업데이트.
        List<CartDto> cartDtoList = Optional.ofNullable(cartRepositorySupport.findCartByProductSeqList(productSeqList, buyerCode))
                .orElseThrow(() -> new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "cart list"));
        cartRepository.saveAll(cartDtoList.stream().map(list -> list.updateDelY(buyerIdentificationCode)).collect(Collectors.toList()));

        // 포인트 적립 예정 생성.
        PointDto nowPoint = pointService.getBuyerPointByBuyerCode(buyerCode);
        nowPoint = pointService.payDoneExpectPoint(infoDto, nowPoint, infoDto.getAmount(), buyerIdentificationCode);
        pointService.payDoneUsePoint(infoDto, nowPoint, usePoint, infoDto.getAmount(), buyerIdentificationCode);

        //재고관리
        for (OrderItemDto dto : orderItemDtoList) {
            Long productSeq = dto.getProductSeq();
            productService.quantityManager(productSeq, -(dto.getOrderQuantity()));
        }

        final String payMethod = "VA"; // 결제수단 코드 (VA: )

        // 주문 - 결제성공으로 업데이트.
        orderItemRepository.saveAll(updateItemList); // 주문 아이템 상태 업데이트 값 저장.
        orderInfoRepository.save(infoDto.updateRes(OrderStateEnum.PAY_DONE.getCode(), amount, usePoint, null, payMethod, null, null, buyerIdentificationCode)); // 주문 상태 업데이트 값 + PG 응답 저장.

        // 주문내역 이메일 발송
        emailService.sendForItemPurchase(buyerIdentificationDto.getBuyer().getCorpEmail(), buyerIdentificationDto.getBuyer().getCorpStaffName(), orderNo,
                infoDto.getOrderName(), String.valueOf(nowPoint.getUsePoint()), "0", String.valueOf(amount));

        return infoDto;
    }

    /**
     * 결제 결과 처리 로직
     */
    public void resultOrderFor(String tid, String resultCode, String resultMessage, String orderNo, boolean isSuccess) {

        orderInfoRepository.setOrderInfoWithSuccess(tid, resultCode, resultMessage, orderNo, isSuccess ? OrderStateEnum.PAY_DONE.getCode() : OrderStateEnum.ORDER_STANDBY.getCode());

    }

}
