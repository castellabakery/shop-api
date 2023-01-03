package kr.co.medicals.order.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import kr.co.medicals.common.enums.OrderStateEnum;
import kr.co.medicals.order.domain.dto.OrderInfoDto;
import kr.co.medicals.order.domain.entity.QOrderInfo;
import kr.co.medicals.sales.domain.SalesSearchDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Repository
public class OrderInfoRepositorySupport {

    private final JPAQueryFactory jpaQueryFactory;

    public OrderInfoRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    /**
     * 매출 헤더 조회용. 개별 호출 필요.
     * 주문 오류의 경우 바로 취소될 수 있는데 이 경우 취소완료 건으로 카운트 되는 내용 문제 없는지 확인해야함.
     * <p>
     * 매출 조회의 주문 상태는 구매확정 or 취소완료/환불완료 세가지의 경우만 조회.
     * <p>
     * **** mybatis 로도 속도 테스트 해야함. ****
     */
    public List<OrderInfoDto> searchSalesHeaderList(SalesSearchDto searchDto) {
        QOrderInfo qOrderInfo = QOrderInfo.orderInfo;

        return jpaQueryFactory.select(Projections.constructor(OrderInfoDto.class, qOrderInfo))
                .from(qOrderInfo)
                .where(
                        eqPaymentMethod(searchDto.getPaymentMethod())
                        , searchDate(searchDto.getStartDate(), searchDto.getEndDate())
                        , eqOrderState(searchDto.getOrderStateList())
                )
                .fetch();
    }

    /**
     * 매출 리스트 조회용.
     */
    public Page<OrderInfoDto> searchSalesList(SalesSearchDto searchDto, Pageable pageable) {
        QOrderInfo qOrderInfo = QOrderInfo.orderInfo;

        List<OrderInfoDto> orderInfoDtoList = jpaQueryFactory.select(Projections.constructor(OrderInfoDto.class, qOrderInfo))
                .from(qOrderInfo)
                .where(
                        eqPaymentMethod(searchDto.getPaymentMethod())
                        , searchDate(searchDto.getStartDate(), searchDto.getEndDate())
                        , eqOrderState(searchDto.getOrderStateList()) // 매출 조회의 주문 상태는 구매확정 or 취소완료/환불완료 세가지의 경우만 조회.
                )
                .orderBy(qOrderInfo.orderInfo.seq.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long orderCnt = jpaQueryFactory.select(qOrderInfo.count())
                .from(qOrderInfo)
                .where(
                        eqPaymentMethod(searchDto.getPaymentMethod())
                        , searchDate(searchDto.getStartDate(), searchDto.getEndDate())
                        , eqOrderState(searchDto.getOrderStateList())
                )
                .fetchOne();

        return new PageImpl<>(orderInfoDtoList, pageable, orderCnt);
    }

    /**
     * 주문 정보 조회. orderInfoSeq or orderNo 파라미터 필수.
     */
    public OrderInfoDto getOrderInfo(Long orderInfoSeq, String orderNo, String buyerCode, List<Integer> orderStateList) {
        QOrderInfo qOrderInfo = QOrderInfo.orderInfo;

        if (ObjectUtils.isEmpty(orderInfoSeq) && ObjectUtils.isEmpty(orderNo)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "orderInfoSeq and orderNo");
        }

        return jpaQueryFactory.select(Projections.constructor(OrderInfoDto.class, qOrderInfo))
                .from(qOrderInfo)
                .where(
                        eqOrderSeq(orderInfoSeq)
                        , eqOrderNo(orderNo)
                        , eqBuyerCode(buyerCode)
                        , eqOrderState(orderStateList)
                )
                .orderBy(qOrderInfo.createdDatetime.desc())
                .fetchOne();
    }

    private BooleanExpression eqOrderSeq(Long orderInfoSeq) {
        if (ObjectUtils.isEmpty(orderInfoSeq)) {
            return null;
        }
        return QOrderInfo.orderInfo.seq.eq(orderInfoSeq);
    }

    private BooleanExpression eqBuyerCode(String buyerCode) {
        if (ObjectUtils.isEmpty(buyerCode)) {
            return null;
        }
        return QOrderInfo.orderInfo.buyerCode.eq(buyerCode);
    }

    private BooleanExpression eqOrderNo(String orderNo) {
        if (ObjectUtils.isEmpty(orderNo)) {
            return null;
        }
        return QOrderInfo.orderInfo.orderNo.eq(orderNo);
    }

    private BooleanExpression eqOrderState(List<Integer> orderStateList) {
        if (orderStateList.size() == 0) {
            return QOrderInfo.orderInfo.orderInfoState.ne(OrderStateEnum.ORDER_STANDBY.getCode());
        }
        return QOrderInfo.orderInfo.orderInfoState.in(orderStateList);
    }


    private BooleanExpression eqPaymentMethod(String paymentMethod) {

        if (ObjectUtils.isEmpty(paymentMethod)) { // 조회를 요청하는 데이터가 들어오지 않으면 null 제외하고 조회.
            return QOrderInfo.orderInfo.paymentMethod.isNotNull();
        }

        return QOrderInfo.orderInfo.paymentMethod.eq(paymentMethod);
    }

    public BooleanExpression searchDate(String startDate, String endDate) {
        return QOrderInfo.orderInfo.createdDatetime.between(
                LocalDateTime.parse(startDate + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                LocalDateTime.parse(endDate + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

}
