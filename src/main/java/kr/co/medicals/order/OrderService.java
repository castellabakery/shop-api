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
     * ???????????? -> ???????????? -> ???????????? ?????? -> ????????????
     */
    public OrderInfoDto directOrder(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("productSeq") || !map.containsKey("quantity")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "productSeq, quantity");
        }

        // ???????????? ?????? or ????????????
        Long cartSeq = cartService.addModifyCart(map);

        // cartDtoList ??? ????????? ??????.
        return this.cartOrder(Arrays.asList(cartSeq));
    }

    /**
     * ???????????? -> ?????? ??????
     */
    public OrderInfoDto cartOrder(List<Long> cartSeqList) {
        // ???????????? ???????????? ?????? ????????? ???????????? ??????. - ???????????? ?????? ?????? ????????? ?????? ?????????.
        BuyerIdentificationDto buyerIdentificationDto = buyerService.getBuyerInfoByIdentificationCode(SessionUtil.getUserCode());

        String buyerCode = buyerIdentificationDto.getBuyer().getBuyerCode();
        List<CartDto> cartDtoList = cartRepositorySupport.myCartList(buyerCode, buyerIdentificationDto.getBuyer().getBuyerType(), cartSeqList); // ???????????? ????????? ?????? ????????? ??????


        if (ObjectUtils.isEmpty(cartDtoList)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "cart list");
        }

        for (CartDto dto : cartDtoList) {
            if (Objects.equals(dto.getProduct().getDelYn(), "Y") || Objects.equals(dto.getProduct().getUseYn(), "N")) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "product");
            }
        }

        int amountAdd = 0; // ???????????? ?????? (info ??????).
        int savePoint = 0;
        OrderInfoDto orderInfoDto = new OrderInfoDto();
        List<OrderItemDto> orderItemDtoList = new ArrayList<>();
        // ???????????? ????????? ???????????? ??????.
        for (CartDto dto : cartDtoList) {
            int createOrderInfo = 0;

            ProductDto productDto = dto.getProduct();
            BuyerTypeProductAmountDto amountDto = dto.getBuyerTypeProductAmount();

            if (createOrderInfo < 1) {
                createOrderInfo++;
            }

            int quantity = dto.getQuantity();

            int itemAmount = quantity * amountDto.getAmount(); // ?????? ?????? (item ??????).
            amountAdd = amountAdd + itemAmount;

            int itemPoint = quantity * amountDto.getSavePoint(); // ????????????+?????????
            savePoint = savePoint + itemPoint;


            OrderItemDto orderItemDto =
                    OrderItemDto.insertStandby()
                            .quantity(quantity)
                            .itemAmount(itemAmount)
                            .pointSave(itemPoint)
                            .productDto(productDto)
                            .amountDto(amountDto)
                            .build();
            orderItemDtoList.add(orderItemDto); // ????????? list. ?????? seq ????????????.
        }

        String orderName = orderItemDtoList.get(0).getProductDisplayName();

        int orderItemCnt = orderItemDtoList.size() - 1;

        if (orderItemCnt >= 1) {
            orderName = orderName + " ???" + orderItemCnt + "???";
        }

        Long orderSeq = orderInfoRepository.save(orderInfoDto.insertEntity(buyerIdentificationDto, amountAdd, savePoint, orderName)).getSeq();


        List<OrderItem> itemList = new ArrayList<>();
        int cnt = 1;
        for (OrderItemDto dto : orderItemDtoList) {
            itemList.add(dto.insertEntity(orderSeq, cnt));
            cnt++;
        }

        orderItemRepository.saveAll(itemList);

        // ????????? ?????? ????????????. ?????? ??????.
        OrderInfoDto infoDto = orderInfoRepositorySupport.getOrderInfo(orderSeq, null, buyerCode, Arrays.asList(OrderStateEnum.ORDER_STANDBY.getCode()));
        List<OrderItemDto> itemDtoList = this.getOrderItemList(infoDto.getSeq(), buyerCode);
        infoDto.setOrderItemList(this.addFileToOrderItemList(itemDtoList));
        infoDto.setBuyerIdentification(buyerIdentificationDto); // ????????? ???????????? ?????? ?????? ????????? ???????????? ?????? ?????????.
        return infoDto;
    }

    /**
     * ?????? ?????????(+????????????) ????????? ??????.
     */
    public Page<OrderItemDto> searchOrderItemList(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        // page ?????? ????????? ??????.
        if ((ObjectUtils.isEmpty(apiRequest.getPage()) || apiRequest.getPage() < 0)
                || ObjectUtils.isEmpty(apiRequest.getPageSize()) || apiRequest.getPageSize() < 0) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "paging");
        }

        // ?????? ??????
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
            // startDatetime, endDatetime ????????? ?????? ????????? ??????.
            if (map.containsKey("startDate") && map.containsKey("endDate")) {
                startDate = map.get("startDate").toString();
                endDate = map.get("endDate").toString();
            } else {
                LocalDate now = LocalDate.now();
                startDate = now.toString();
                endDate = now.toString();
            }

            // ?????? ??????????????? ?????? ?????? ?????? ??????.
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
     * ?????? ?????? ??????. ?????? ??? ?????? ?????? ??? ?????? ????????? ??????.
     */
    public OrderInfoDto getOrderDetail(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("orderInfoSeq")) { // ?????? ?????????
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "orderInfoSeq");
        }
        Long orderInfoSeq = Long.valueOf(map.get("orderInfoSeq").toString());

        // ?????????????????? ????????? ?????? ?????? ????????????. ?????????????????? ???????????? ??????.
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
     * ?????? ?????? + ?????? ????????? ?????? ??????
     */
    public Map<String, String> successOrderPay(Map<String, Object> returnMap) {

        Map<String, Object> payResultMap = null;
        try {
            // ?????? ?????? ?????? - 'orderinfo??? ?????? amount'??? 'request??? ????????? ?????????????????? + ?????????'??? ?????? ??????
            OrderInfoDto infoDto = orderInfoRepositorySupport.getOrderInfo(null, returnMap.get("orderno").toString(), null, Arrays.asList(OrderStateEnum.ORDER_STANDBY.getCode()));

            String buyerIdentificationCode = returnMap.get("reserved02").toString();
            BuyerIdentificationDto buyerIdentificationDto = buyerService.getBuyerInfoByIdentificationCode(buyerIdentificationCode); // SessionUtil.getUserCode()
            String buyerCode = buyerIdentificationDto.getBuyer().getBuyerCode();
            PointDto pointDto = pointService.getBuyerPointByBuyerCode(buyerCode);

            if (pointDto.getSavePoint() < Integer.parseInt(returnMap.get("reserved01").toString())) {
                log.error("[successOrderPay-ERROR] >>>> ?????? ?????? ?????? ??????(?????? ?????? ????????? ??????) : {}, {} ", returnMap, infoDto.getAmount());

                infoDto.setOrderItemList(this.getOrderItemList(infoDto.getSeq(), null));

                List<OrderItemDto> orderItemDtoList = infoDto.getOrderItemList();
                List<OrderItem> updateItemList = new ArrayList<>();
                for (OrderItemDto orderItemDto : orderItemDtoList) {
                    updateItemList.add(orderItemDto.updateOrderStateEntity(OrderStateEnum.ORDER_ERROR.getEnCode(), "ERROR"));
                }

                // ????????? ?????? ?????? ??????
                orderItemRepository.saveAll(updateItemList);
                orderInfoRepository.save(infoDto.updateError(OrderStateEnum.ORDER_ERROR.getCode(), "error", "??? ?????? ??????."));

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
                log.error("[successOrderPay-ERROR] >>>> ?????? ?????? ?????? ??????(????????? ?????????) : {}, {} ", returnMap, infoDto.getAmount());

                infoDto.setOrderItemList(this.getOrderItemList(infoDto.getSeq(), null));

                List<OrderItemDto> orderItemDtoList = infoDto.getOrderItemList();
                List<OrderItem> updateItemList = new ArrayList<>();
                for (OrderItemDto orderItemDto : orderItemDtoList) {
                    updateItemList.add(orderItemDto.updateOrderStateEntity(OrderStateEnum.ORDER_ERROR.getEnCode(), "ERROR"));
                }

                // ????????? ?????? ?????? ??????
                orderItemRepository.saveAll(updateItemList);
                orderInfoRepository.save(infoDto.updateError(OrderStateEnum.ORDER_ERROR.getCode(), "error", "??? ?????? ??????."));

                Map<String, String> resultData = new HashMap<>();
                resultData.put("orderState", OrderStateEnum.ORDER_ERROR.getEnCode());
                return resultData;
            }

            log.info("?????? ?????? ?????? : {}", returnMap);
            if (returnMap.containsKey("orderType") && Objects.equals("pointOnly", returnMap.get("orderType"))) {
                payResultMap = returnMap;
            } else {
                payResultMap = XXService.paymentReturn(returnMap);
            }

            if (!ObjectUtils.isEmpty(payResultMap)) {

                log.info("?????? ?????? ?????? : {}", payResultMap);

                String resultCode = payResultMap.get("result_code").toString(); // ????????? ?????? ??????
                String resultMsg = payResultMap.get("result_msg").toString(); // ????????? ?????? ?????????
                String dResultCode = payResultMap.get("dresult_code").toString(); // ????????? ?????? ??????
                String dResultMsg = payResultMap.get("dresult_msg").toString(); // ????????? ?????? ?????????

                // ?????? ????????? ?????? - ????????? ?????? ??????. ??????????????? ?????? ?????????.
                if (!(Objects.equals("EC0000", resultCode) && Objects.equals("EC0000", dResultCode))) { // ?????? ????????? ??????.
                    log.error("[successOrderPay-ERROR] >>>> ?????? ???????????? [" + dResultCode + "] : {} ", payResultMap);

                    String orderNo = returnMap.get("orderno").toString(); // ?????? ??????

//                    OrderInfoDto infoDto = orderInfoRepositorySupport.getOrderInfo(null, orderNo, null, Arrays.asList(OrderStateEnum.ORDER_STANDBY.getCode()));
                    infoDto.setOrderItemList(this.getOrderItemList(infoDto.getSeq(), null));

                    List<OrderItemDto> orderItemDtoList = infoDto.getOrderItemList();
                    List<OrderItem> updateItemList = new ArrayList<>();
                    for (OrderItemDto orderItemDto : orderItemDtoList) {
                        updateItemList.add(orderItemDto.updateOrderStateEntity(OrderStateEnum.ORDER_ERROR.getEnCode(), "ERROR"));
                    }

                    // ????????? ?????? ??? ?????? ??????.
                    orderItemRepository.saveAll(updateItemList); // ?????? ????????? ?????? ???????????? ??? ??????.
                    orderInfoRepository.save(infoDto.updateError(OrderStateEnum.ORDER_ERROR.getCode(), resultCode, "[?????? ??????]" + resultMsg));

                    Map<String, String> resultData = new HashMap<>();
                    resultData.put("orderState", OrderStateEnum.ORDER_ERROR.getEnCode());
                    return resultData;
                }

                // ?????? ?????? ?????????
                String tid = payResultMap.get("tid").toString(); // ????????? ????????????
                String payMethod = payResultMap.get("pay_method").toString(); // ??????????????????
                int amount = Integer.valueOf(payResultMap.get("approvamt").toString()); // ??????
                String orderNo = payResultMap.get("orderno").toString(); // ?????? ??????
                String reserved01 = payResultMap.get("reserved01").toString(); // ????????? ????????????
                String reserved02 = payResultMap.get("reserved02").toString(); // ????????? ?????? ?????? 2

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

                // ?????? ?????? ?????????!
                int usePoint = Integer.valueOf(reserved01);


                // ???????????? - ?????? ????????? ????????????.
                List<CartDto> cartDtoList = Optional.ofNullable(cartRepositorySupport.findCartByProductSeqList(productSeqList, buyerCode))
                        .orElseThrow(() -> new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "cart list"));
                cartRepository.saveAll(cartDtoList.stream().map(list -> list.updateDelY(buyerIdentificationCode)).collect(Collectors.toList()));

                // ????????? ?????? ?????? ??????.
                PointDto nowPoint = pointService.getBuyerPointByBuyerCode(buyerCode);
                nowPoint = pointService.payDoneExpectPoint(infoDto, nowPoint, infoDto.getAmount(), buyerIdentificationCode);
                pointService.payDoneUsePoint(infoDto, nowPoint, usePoint, infoDto.getAmount(), buyerIdentificationCode);

                //????????????
                for (OrderItemDto dto : orderItemDtoList) {
                    Long productSeq = dto.getProductSeq();
                    productService.quantityManager(productSeq, -(dto.getOrderQuantity()));
                }

                // ?????? - ?????????????????? ????????????.
                orderItemRepository.saveAll(updateItemList); // ?????? ????????? ?????? ???????????? ??? ??????.
                orderInfoRepository.save(infoDto.updateRes(OrderStateEnum.PAY_DONE.getCode(), amount, usePoint, tid, payMethod, resultCode, resultMsg, buyerIdentificationCode)); // ?????? ?????? ???????????? ??? + PG ?????? ??????.

                // ???????????? ????????? ??????
                emailService.sendForItemPurchase(buyerIdentificationDto.getBuyer().getCorpEmail(), buyerIdentificationDto.getBuyer().getCorpName(), returnMap.get("orderno").toString(),
                        infoDto.getOrderName(), reserved01, String.valueOf(amount), "0");

                Map<String, String> resultData = new HashMap<>();
                resultData.put("corpName", infoDto.getCorpName());
                resultData.put("orderNo", infoDto.getOrderNo());
                resultData.put("orderState", OrderStateEnum.PAY_DONE.getEnCode());
                return resultData;

            } else {

                log.error("[successOrderPay-ERROR] >>>> ?????? ?????? : {} ", payResultMap);

                String orderNo = returnMap.get("orderno").toString(); // ?????? ??????

//                OrderInfoDto infoDto = orderInfoRepositorySupport.getOrderInfo(null, orderNo, null, Arrays.asList(OrderStateEnum.ORDER_STANDBY.getCode()));
                infoDto.setOrderItemList(this.getOrderItemList(infoDto.getSeq(), null));

                List<OrderItemDto> orderItemDtoList = infoDto.getOrderItemList();
                List<OrderItem> updateItemList = new ArrayList<>();
                for (OrderItemDto orderItemDto : orderItemDtoList) {
                    updateItemList.add(orderItemDto.updateOrderStateEntity(OrderStateEnum.ORDER_ERROR.getEnCode(), "ERROR"));
                }

                // ????????? ?????? ?????? ??????
                orderItemRepository.saveAll(updateItemList);
                orderInfoRepository.save(infoDto.updateError(OrderStateEnum.ORDER_ERROR.getCode(), "error", "??? ?????? ??????."));

                Map<String, String> resultData = new HashMap<>();
                resultData.put("orderState", OrderStateEnum.ORDER_ERROR.getEnCode());
                return resultData;
            }

        } catch (Exception e) {

            log.error("[successOrderPay-ERROR] >>>> ?????? ?????? ?????? : {} ", payResultMap.toString());

            String orderNo = returnMap.get("orderno").toString();

            OrderInfoDto infoDto = orderInfoRepositorySupport.getOrderInfo(null, orderNo, null, Arrays.asList(OrderStateEnum.ORDER_STANDBY.getCode()));
            infoDto.setOrderItemList(this.getOrderItemList(infoDto.getSeq(), null));

            String tid = payResultMap.get("tid").toString();
            String paymentMethod = payResultMap.get("pay_method").toString();
            int approveAmount = Integer.valueOf(payResultMap.get("approvamt").toString());
            int usePoint = Integer.valueOf(payResultMap.get("reserved01").toString());
            String tradeResult = payResultMap.get("result_code").toString(); // ????????? ?????? ??????
            String tradeResultMsg = payResultMap.get("result_msg").toString(); // ????????? ?????? ?????????


            List<OrderItemDto> orderItemDtoList = infoDto.getOrderItemList();
            for (OrderItemDto orderItemDto : orderItemDtoList) {
//              // ??????????????? ?????????.
                orderItemRepository.save(orderItemDto.updateOrderStateEntity(OrderStateEnum.PAYMENT_ERROR.getEnCode(), "ERROR"));
            }

            // ??????????????? ?????????.
            orderInfoRepository.save(infoDto.updateRes(OrderStateEnum.PAYMENT_ERROR.getCode(), approveAmount, usePoint, tid, paymentMethod, tradeResult, tradeResultMsg, "ERROR"));

            Map<String, String> resultData = new HashMap<>();
            resultData.put("orderState", OrderStateEnum.PAYMENT_ERROR.getEnCode());
            return resultData;
        }

    }

    // ????????????  -> ???????????????(??????), ?????????????????????(??????)
    public void orderConfirm(String orderStateEnCode, OrderItemDto orderItemDto) {
        // ????????? ????????????
        orderItemRepository.save(orderItemDto.updateOrderStateEntity(orderStateEnCode, SessionUtil.getUserCode()));
        PointDto nowPoint = pointService.getBuyerPointByBuyerCode(orderItemDto.getOrderInfo().getBuyerCode());
        // ????????? ??????
        pointService.orderConfirmPoint(orderItemDto, nowPoint, orderItemDto.getOrderInfo().getAmount());
    }

    //  ?????????
    public void modifyOrderDelivery(String orderStateEnCode, OrderItemDto oldItemDto, OrderItemDto newItemDto) {
        oldItemDto.setDeliveryNo(newItemDto.getDeliveryNo());
        oldItemDto.setDeliveryCompany(newItemDto.getDeliveryCompany());
        orderItemRepository.save(oldItemDto.updateOrderStateEntity(orderStateEnCode, SessionUtil.getUserCode()));
    }

    // ????????????, ????????????, ???????????? -> ????????? ?????? ??????.
    public void modifyOrderState(String orderStateEnCode, OrderItemDto orderItemDto) {
        orderItemRepository.save(orderItemDto.updateOrderStateEntity(orderStateEnCode, SessionUtil.getUserCode()));
    }

    // ????????????, ???????????? -> ???????????????(??????), ???????????????(??????) || ?????????????????????(??????) -- ???????????? ?????? ??????.(????????????, ??????????????????)
    public void orderCancelOrRefund(String orderStateEnCode, OrderItemDto orderItemDto, String newPaymentMethod) {
        log.info("==========[?????? ?????? ??????]=========================");

        log.info("========= ????????? ????????? ?????? ?????? ======================== {}", newPaymentMethod);
        int amount = orderItemDto.getOrderInfo().getAmount();
        log.info("========= ??? ?????? ?????? ======================== {}", amount);
        int approveAmount = orderItemDto.getOrderInfo().getApproveAmount();
        log.info("========= " + newPaymentMethod + " ?????? ?????? ====================== {}", approveAmount);
        int cancelRequestAmount = orderItemDto.getOrderItemAmount();
        log.info("========= ?????? ?????? ?????? ====================== {}", cancelRequestAmount);
        int cancelAmount = orderItemDto.getOrderInfo().getCancelAmount();
        log.info("========= " + newPaymentMethod + " ?????? ?????? ?????? ================== {}", cancelAmount);
        int availableAmount = approveAmount - cancelAmount;
        log.info("========= " + newPaymentMethod + " ?????? ?????? ?????? ================== {}", availableAmount);
        boolean cardCancel = Objects.equals(availableAmount, 0) ? false : (availableAmount >= cancelRequestAmount);
        log.info("========= " + newPaymentMethod + " ?????? ?????? ?????? ================== {}", cardCancel);

        String tid = orderItemDto.getOrderInfo().getOrgOrderNo();
        int upCancelAmount = cancelAmount;
        int upCancelPoint = orderItemDto.getOrderInfo().getCancelPoint();
        OrderInfoDto orderInfoDto = orderItemDto.getOrderInfo();
        PointDto nowPoint = pointService.getBuyerPointByBuyerCode(orderInfoDto.getBuyerCode());

        if (!Objects.equals(newPaymentMethod, "VA")) {
            if (cardCancel) {// ????????????????????? ??????
                Map<String, Object> payResultMap = XXService.paymentCancel(orderInfoDto.getPaymentMethod(), String.valueOf(cancelRequestAmount), tid);
                log.info("========= ???????????? ?????? ?????? : {}", payResultMap);
                upCancelAmount = cancelAmount + cancelRequestAmount;
                log.info("========= ?????? ?????? ?????? ?????? ================== {}", upCancelAmount);

            } else {// ???????????????????????? ?????????
                if (availableAmount > 0) { // ?????? ?????? ????????? ???????????? ??????
                    Map<String, Object> payResultMap = XXService.paymentCancel(orderInfoDto.getPaymentMethod(), String.valueOf(availableAmount), tid);
                    log.info("====================== ?????? ?????? ?????? =========================");
                    log.info("========= ???????????? ?????? ?????? : {}", payResultMap);
                    int realCancelAmt = Integer.valueOf(payResultMap.get("cancel_amt").toString());
                    log.info("========= ?????? ?????? ?????? ====================== {}", realCancelAmt);
                    upCancelAmount = cancelAmount + realCancelAmt;
                    log.info("========= ?????? ?????? ?????? ?????? ================== {}", upCancelAmount);
                }

                // ????????? ?????? ??????
                log.info("====================== ????????? ?????? ?????? =========================");
                int requestSavePoint = cancelRequestAmount - availableAmount;
                log.info("========= ?????? ?????? ?????? ?????? ================== {}", requestSavePoint);
                int usePoint = orderItemDto.getOrderInfo().getUsePoint();
                log.info("========= ?????? ????????? ========================= {}", usePoint);
                log.info("========= ?????? ?????? ?????????(???) ===================== {}", upCancelPoint);
                int availablePoint = usePoint - upCancelPoint;
                log.info("========= ?????? ?????? ????????? ===================== {}", availablePoint);
                boolean pointCancel = Objects.equals(availablePoint, 0) ? false : (availablePoint >= requestSavePoint);
                log.info("========= ????????? ?????? ?????? ?????? ================= {}", availablePoint);


                if (!pointCancel) {
                    throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "?????? ????????? ????????? ????????? ??????????????????.");
                }

                // ????????? ?????? ????????? ?????????.
                nowPoint = pointService.cancelDoneUsePoint(orderItemDto, nowPoint, requestSavePoint, amount);
                upCancelPoint = upCancelPoint + requestSavePoint;
                log.info("========= ?????? ?????? ?????????(???) ===================== {}", upCancelPoint);
            }
        } else {
            if (cardCancel) {
                String buyerCode = orderInfoDto.getBuyerCode();
                Optional<BuyerVaccount> buyerVaccountOptional = buyerVaccountRepository.findByBuyerCode(buyerCode);
                if (buyerVaccountOptional.isEmpty()) {
                    log.error("not exists buyer - buyerCode: {}", buyerCode);
                    throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "???????????? ???????????? ?????? ??????????????????.");
                }
                BuyerVaccount bv = buyerVaccountOptional.get();
                try {
                    upCancelAmount = cancelAmount + cancelRequestAmount;
                    log.info("========= ?????? ?????? ?????? ================== {}", upCancelAmount);
                } catch (Exception e) {
                    log.error("Transaction ERROR - {}", e);
                    throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUESTED_FAIL, "?????? ?????? ????????? ?????????????????????.");
                }
            } else {// ?????????????????? ?????????
                if (availableAmount > 0) { // ?????? ????????? ???????????? ??????
                    String buyerCode = orderInfoDto.getBuyerCode();
                    Optional<BuyerVaccount> buyerVaccountOptional = buyerVaccountRepository.findByBuyerCode(buyerCode);
                    if (buyerVaccountOptional.isEmpty()) {
                        log.error("not exists buyer - buyerCode: {}", buyerCode);
                        throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "???????????? ???????????? ?????? ??????????????????.");
                    }
                    BuyerVaccount bv = buyerVaccountOptional.get();
                    try {
                        upCancelAmount = cancelAmount + availableAmount;
                        log.info("========= ?????? ?????? ?????? ================== {}", upCancelAmount);
                    } catch (Exception e) {
                        log.error("Transaction ERROR - {}", e);
                        throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUESTED_FAIL, "?????? ?????? ????????? ?????????????????????.");
                    }
                }

                // ????????? ?????? ??????
                log.info("====================== ????????? ?????? ?????? =========================");
                int requestSavePoint = cancelRequestAmount - availableAmount;
                log.info("========= ?????? ?????? ?????? ?????? ================== {}", requestSavePoint);
                int usePoint = orderItemDto.getOrderInfo().getUsePoint();
                log.info("========= ?????? ????????? ========================= {}", usePoint);
                log.info("========= ?????? ?????? ?????????(???) ===================== {}", upCancelPoint);
                int availablePoint = usePoint - upCancelPoint;
                log.info("========= ?????? ?????? ????????? ===================== {}", availablePoint);
                boolean pointCancel = Objects.equals(availablePoint, 0) ? false : (availablePoint >= requestSavePoint);
                log.info("========= ????????? ?????? ?????? ?????? ================= {}", availablePoint);


                if (!pointCancel) {
                    throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "?????? ????????? ????????? ????????? ??????????????????.");
                }

                // ????????? ?????? ????????? ?????????.
                nowPoint = pointService.cancelDoneUsePoint(orderItemDto, nowPoint, requestSavePoint, amount);
                upCancelPoint = upCancelPoint + requestSavePoint;
                log.info("========= ?????? ?????? ?????????(???) ===================== {}", upCancelPoint);
            }
        }
        int upInfoSavePoint = orderItemDto.getOrderInfo().getSavePoint();
        int itemPointSave = orderItemDto.getPointSave();
        log.info("========= ????????????????????? ????????? ================= {}", upInfoSavePoint);
        log.info("========= ???????????? ????????? ????????? ================= {}", itemPointSave);
        upInfoSavePoint = upInfoSavePoint - itemPointSave;
        log.info("========= ???????????? ??? ?????? ????????? ================= {}", upInfoSavePoint);

        // ?????? ?????? ????????????
        orderInfoDto.setCancelAmount(upCancelAmount);
        orderInfoDto.setCancelPoint(upCancelPoint);
        orderInfoDto.setSavePoint(upInfoSavePoint);
        orderInfoRepository.save(orderInfoDto.updateOrderInfo());
        log.info("==============================================");
        log.info("========= ???????????? ??? ?????? ?????? ================= {}", upCancelAmount);
        log.info("========= ???????????? ??? ?????? ????????? =============== {}", upCancelPoint);
        log.info("========= ???????????? ?????? ????????? =============== {}", upInfoSavePoint);
        log.info("==============================================");


        // ?????? ????????? ????????????
        orderItemDto.setCancelYn("Y");
        orderItemRepository.save(orderItemDto.updateOrderStateEntity(orderStateEnCode, SessionUtil.getUserCode()));

        // ????????? ?????? ????????? ???????????? ??????.
        pointService.cancelDoneExpectPoint(orderItemDto, nowPoint, amount);

        // ?????? ????????????
        productService.quantityManager(orderItemDto.getProductSeq(), orderItemDto.getOrderQuantity());

        // ?????? or ????????? ????????? ??????
        if (orderStateEnCode.equals(OrderStateEnum.REFUND_DONE.getEnCode()))
            emailService.sendForItemPurchaseRefund(orderInfoDto.getCorpEmail(), orderInfoDto.getStaffName(), orderInfoDto.getOrderNo(), orderItemDto.getProductName(),
                    String.valueOf(upCancelPoint), String.valueOf(upCancelAmount), newPaymentMethod);
        if (orderStateEnCode.equals(OrderStateEnum.CANCEL_DONE.getEnCode()))
            emailService.sendForItemPurchaseCancel(orderInfoDto.getCorpEmail(), orderInfoDto.getStaffName(), orderInfoDto.getOrderNo(), orderItemDto.getProductName(),
                    String.valueOf(upCancelPoint), String.valueOf(upCancelAmount), newPaymentMethod);
    }

    /**
     * ????????? ??????.
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
                && !Objects.equals(OrderStateEnum.PAY_DONE.getEnCode(), infoDto.getOrderInfoState())) { // ????????? ?????? ????????? ???????????? ??????????????? ?????? ??????.
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "NOT_MATCH", "order state");
        }
        // buyerType??? ????????? ????????? ?????? ??????.
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
     * ???????????? ?????? ?????? ?????? ??????.
     */
    public void modifyOrderInfoByAdmin(ApiRequest apiRequest) {

        OrderInfoDto orderInfoDto = ObjectMapperUtil.requestConvertDto(apiRequest, OrderInfoDto.class);

        // OrderInfo??? ????????? ????????? ?????? - ??????????????? ??? ?????????.
        if (!ObjectUtils.isEmpty(orderInfoDto) && !ObjectUtils.isEmpty(orderInfoDto.getSeq()) && !ObjectUtils.isEmpty(orderInfoDto.getTaxbillYn())) {
            OrderInfoDto orderInfoDtoForUpdate = orderInfoRepositorySupport.getOrderInfo(orderInfoDto.getSeq(), null, null, Arrays.asList());
            orderInfoDtoForUpdate.setTaxbillYn(orderInfoDto.getTaxbillYn());
            orderInfoRepository.save(orderInfoDtoForUpdate.updateOrderInfo());
        } else {
            log.info("????????? ???????????? ??????????????? ????????? ???????????? ???????????????. - seq: {}, taxbillYn: {}", orderInfoDto.getSeq(), orderInfoDto.getTaxbillYn());
        }

        List<OrderItemDto> orderItemDtoList = orderInfoDto.getOrderItemList();

        for (OrderItemDto newItemDto : orderItemDtoList) {

            if (ObjectUtils.isEmpty(newItemDto.getSeq()) || newItemDto.getSeq() <= 0) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "seq");
            }

            // ?????? DB ?????? ??????
            OrderItemDto oldItemDto = orderItemRepositorySupport.getOrderItemWithOrderInfo(newItemDto.getSeq(), null);
            if (ObjectUtils.isEmpty(oldItemDto)) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "order item");
            }

            String oldEnCode = oldItemDto.getOrderItemState();
            String newEnCode = newItemDto.getOrderItemState();

            // [?????? ?????? : ???????????? / ?????? ?????? : ??????] (=??????????????? ??????)
            if ((Objects.equals(OrderStateEnum.PAY_DONE.getEnCode(), oldEnCode) || Objects.equals(OrderStateEnum.SHIPPING.getEnCode(), oldEnCode))
                    && ObjectUtils.isEmpty(newEnCode)
                    && (!ObjectUtils.isEmpty(newItemDto.getDeliveryCompany()) && !ObjectUtils.isEmpty(newItemDto.getDeliveryNo()))) {
                newEnCode = OrderStateEnum.SHIPPING.getEnCode();
            }

            // ?????? ??? ?????? ????????? Exception
            if (ObjectUtils.isEmpty(newEnCode)) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "orderItemState");
            }

            // ????????? ????????? ???????????? ??????. ???, shipping -> shipping ?????? ????????? ??????.
            if (!Objects.equals(OrderStateEnum.SHIPPING.getEnCode(), newEnCode)) {
                if (Objects.equals(newEnCode, oldEnCode)) {
                    continue;
                }
            }

            // admin ??? ????????? ??? ?????? ???????????? ??????.
            OrderStateEnum.checkApproveStateAdmin(oldEnCode, newEnCode);

            // ??????????????? ??????????????? ??????. ????????????, ????????? ??????.
            if (Objects.equals(newEnCode, OrderStateEnum.SHIPPING.getEnCode())) {

                // ???????????? ?????? ??????. ?????? ??? ??????.
                String deliveryCompany = !ObjectUtils.isEmpty(newItemDto.getDeliveryCompany()) ? newItemDto.getDeliveryCompany() : oldItemDto.getDeliveryCompany();
                deliveryCompany = deliveryCompany.replaceAll(" ", "");

                if (ObjectUtils.isEmpty(deliveryCompany)) { // ??????????????? ??????????????? ????????? ???????????? ???????????? ??? ??????.
                    throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "deliveryCompany");
                } else {
                    newItemDto.setDeliveryCompany(deliveryCompany);
                }

                // ??????????????? ?????? ??????(???????????? ??????). ?????? ??? ??????.
                String deliveryNo = "";
                if (!Objects.equals("????????????", deliveryCompany)) {
                    if (ObjectUtils.isEmpty(newItemDto.getDeliveryNo())) { // ?????? ????????? ????????? ??????????????? ?????? ?????? ?????? ?????????.
                        throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "deliveryNo");
                    }
                    deliveryNo = newItemDto.getDeliveryNo().replaceAll("[^0-9]", "");
                }
                newItemDto.setDeliveryNo(deliveryNo);
            }

            // ????????? ???????????? ??????.
            this.modifyOrderInfoByState(oldEnCode, newEnCode, oldItemDto, newItemDto);
        }

    }

    /**
     * ????????? ?????? ?????? ?????? ??????.
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
            String oldEnCode = orderItemDto.getOrderItemState(); // ????????????

            // ????????? ????????? ???????????? ??????. ?????? ????????? shipping ??? ?????? ?????? ??????.
            if (Objects.equals(newEnCode, oldEnCode)
                    && !(Objects.equals(OrderStateEnum.SHIPPING.getEnCode(), newEnCode))) {
                continue;
            }

            // buyer ??????????????? ????????? ?????? ?????? ??????.
            OrderStateEnum.checkApproveStateBuyer(oldEnCode, newEnCode);

            OrderItemDto newItemDto = new OrderItemDto();
            newItemDto.setOrderItemState(newEnCode);
            newItemDto.setSeq(seq);

            // ????????? ???????????? ??????.
            this.modifyOrderInfoByState(oldEnCode, newEnCode, orderItemDto, newItemDto);
        }

    }

    /**
     * ?????? ????????? ?????? ?????? + ????????? ?????? + ?????? ??????
     */
    private void modifyOrderInfoByState(String oldEnCode, String newEnCode, OrderItemDto oldItemDto, OrderItemDto newItemDto) {

        // ????????????, ????????????, ???????????? ?????? ??????????????? ??????????????? ????????? ??? ??????.
        if (Objects.equals(oldEnCode, OrderStateEnum.CANCEL_DONE.getEnCode())
                || Objects.equals(oldEnCode, OrderStateEnum.REFUND_DONE.getEnCode())
                || Objects.equals(oldEnCode, OrderStateEnum.ORDER_CONFIRM.getEnCode())
        ) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "??????????????? ??? ??? ?????? ????????? ?????????. [" + oldEnCode + "]");
        }


        if (Objects.equals(newEnCode, OrderStateEnum.ORDER_CONFIRM.getEnCode())) { // ????????????
            this.orderConfirm(newEnCode, oldItemDto);

        } else if (Objects.equals(newEnCode, OrderStateEnum.SHIPPING.getEnCode())) { // ?????????
            this.modifyOrderDelivery(newEnCode, oldItemDto, newItemDto);

        } else if (Objects.equals(newEnCode, OrderStateEnum.CANCEL_REQUEST.getEnCode())
                || Objects.equals(newEnCode, OrderStateEnum.REFUND_REQUEST.getEnCode())
                || Objects.equals(newEnCode, OrderStateEnum.PAY_DONE.getEnCode())
                || Objects.equals(newEnCode, OrderStateEnum.DELIVERY_COMPLETED.getEnCode())) { // ????????????, ????????????, ?????????, ???????????? : ?????? ????????????
            this.modifyOrderState(newEnCode, oldItemDto);

        } else if (Objects.equals(newEnCode, OrderStateEnum.CANCEL_DONE.getEnCode())
                || Objects.equals(newEnCode, OrderStateEnum.REFUND_DONE.getEnCode())) { // ????????????, ????????????
            this.orderCancelOrRefund(newEnCode, oldItemDto, newItemDto.getOrderInfo().getPaymentMethod());

        } else {// ??? ???
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "?????? ??? ??? ?????? ?????? ???.", newEnCode);
        }

    }


    /**
     * ?????? ??? ?????? ???????????? ????????? ????????? ??????.
     */
    private List<OrderItemDto> getOrderItemList(Long orderInfoSeq, String buyerCode) {
        return orderItemRepositorySupport.getOrderItemByInfoSeq(orderInfoSeq, buyerCode);
    }

    /**
     * ?????? ????????? ?????? ?????? ???????????? ????????? ????????? ??????.
     */
    public List<OrderItemDto> getOrderItemListForBuyerDelete(String buyerCode) {
        return orderItemRepositorySupport.getOrderItemByBuyerCodeAndStateList(buyerCode, OrderStateEnum.searchOrderInProgress());
    }

    /**
     * ?????? ????????? ??? ?????? ?????? ??????.
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
     * ?????? ?????? ????????? ?????? ????????? ??????.
     */
    public List<OrderInfoDto> searchSalesHeaderList(SalesSearchDto searchDto) {
        return orderInfoRepositorySupport.searchSalesHeaderList(searchDto);
    }

    /**
     * ?????? ????????? ??????.
     */
    public Page<OrderInfoDto> searchSalesList(SalesSearchDto searchDto, Pageable pageable) {
        return orderInfoRepositorySupport.searchSalesList(searchDto, pageable);
    }


    /**
     * ?????? ???, ?????? ??????
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public OrderInfoDto orderFor(int amount, String orderNo, int usePoint) throws Exception {

        if (amount <= 0 || ObjectUtils.isEmpty(orderNo)) {
            throw new Exception("??????????????? ???????????? ????????????.");
        }

        String buyerCode = buyerService.getBuyerCode();
        // todo ???????????? ???????????? ????????? ?????? ???????????? ???????????? bi ???
        BuyerIdentificationDto buyerIdentificationDto = buyerService.getBuyerIdentificationByBuyerCode(buyerCode);
        String buyerIdentificationCode = buyerIdentificationDto.getBuyerIdentificationCode();

        OrderInfoDto infoDto = orderInfoRepositorySupport.getOrderInfo(null, orderNo, buyerCode, Arrays.asList(OrderStateEnum.ORDER_STANDBY.getCode()));

        if (!Objects.equals(infoDto.getAmount() - usePoint, amount)) {
            log.error("invalid amout: requested: {}, actual: {} ", amount, infoDto.getAmount());
            throw new Exception("?????? ?????? ????????? ???????????? ????????????.");
        }

        infoDto.setOrderItemList(this.getOrderItemList(infoDto.getSeq(), null));

        List<OrderItemDto> orderItemDtoList = infoDto.getOrderItemList();
        List<Long> productSeqList = new ArrayList<>();
        List<OrderItem> updateItemList = new ArrayList<>();
        for (OrderItemDto orderItemDto : orderItemDtoList) {
            updateItemList.add(orderItemDto.updateOrderStateEntity(OrderStateEnum.PAY_DONE.getEnCode(), buyerIdentificationCode));
            productSeqList.add(orderItemDto.getProductSeq());
        }

        // ???????????? - ?????? ????????? ????????????.
        List<CartDto> cartDtoList = Optional.ofNullable(cartRepositorySupport.findCartByProductSeqList(productSeqList, buyerCode))
                .orElseThrow(() -> new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "cart list"));
        cartRepository.saveAll(cartDtoList.stream().map(list -> list.updateDelY(buyerIdentificationCode)).collect(Collectors.toList()));

        // ????????? ?????? ?????? ??????.
        PointDto nowPoint = pointService.getBuyerPointByBuyerCode(buyerCode);
        nowPoint = pointService.payDoneExpectPoint(infoDto, nowPoint, infoDto.getAmount(), buyerIdentificationCode);
        pointService.payDoneUsePoint(infoDto, nowPoint, usePoint, infoDto.getAmount(), buyerIdentificationCode);

        //????????????
        for (OrderItemDto dto : orderItemDtoList) {
            Long productSeq = dto.getProductSeq();
            productService.quantityManager(productSeq, -(dto.getOrderQuantity()));
        }

        final String payMethod = "VA"; // ???????????? ?????? (VA: )

        // ?????? - ?????????????????? ????????????.
        orderItemRepository.saveAll(updateItemList); // ?????? ????????? ?????? ???????????? ??? ??????.
        orderInfoRepository.save(infoDto.updateRes(OrderStateEnum.PAY_DONE.getCode(), amount, usePoint, null, payMethod, null, null, buyerIdentificationCode)); // ?????? ?????? ???????????? ??? + PG ?????? ??????.

        // ???????????? ????????? ??????
        emailService.sendForItemPurchase(buyerIdentificationDto.getBuyer().getCorpEmail(), buyerIdentificationDto.getBuyer().getCorpStaffName(), orderNo,
                infoDto.getOrderName(), String.valueOf(nowPoint.getUsePoint()), "0", String.valueOf(amount));

        return infoDto;
    }

    /**
     * ?????? ?????? ?????? ??????
     */
    public void resultOrderFor(String tid, String resultCode, String resultMessage, String orderNo, boolean isSuccess) {

        orderInfoRepository.setOrderInfoWithSuccess(tid, resultCode, resultMessage, orderNo, isSuccess ? OrderStateEnum.PAY_DONE.getCode() : OrderStateEnum.ORDER_STANDBY.getCode());

    }

}
