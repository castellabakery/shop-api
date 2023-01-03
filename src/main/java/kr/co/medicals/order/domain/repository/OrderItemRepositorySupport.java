package kr.co.medicals.order.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.medicals.common.enums.OrderStateEnum;
import kr.co.medicals.common.util.SessionUtil;
import kr.co.medicals.order.domain.dto.OrderInfoDto;
import kr.co.medicals.order.domain.dto.OrderItemDto;
import kr.co.medicals.order.domain.entity.QOrderInfo;
import kr.co.medicals.order.domain.entity.QOrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
public class OrderItemRepositorySupport {
    private final JPAQueryFactory jpaQueryFactory;

    public OrderItemRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    /**
     * 주문 리스트 조회. orderItem & orderInfo Join 조회.
     */
    public OrderInfoDto searchOrderList(String buyerCode, String staffId, String corpName, String staffName, String startDate, String endDate, String searchKeyword, List<Integer> orderStateList, Pageable pageable) {

        QOrderItem qOrderItem = QOrderItem.orderItem;
        QOrderInfo qOrderInfo = QOrderInfo.orderInfo;

        List<OrderItemDto> orderItemDtoList =
                jpaQueryFactory.select(Projections.constructor(OrderItemDto.class, qOrderItem, qOrderInfo))
                        .from(qOrderItem)
                        .leftJoin(qOrderInfo)
                        .on(qOrderItem.orderInfo.seq.eq(qOrderInfo.seq))
                        .where(
                                eqBuyerCode(buyerCode)
                                , containsStaffId(staffId)
                                , containsCorpName(corpName)
                                , containsStaffName(staffName)
                                , containsSearchKeyword(searchKeyword)
                                , eqOrderItemState(orderStateList)
                                , searchDate(startDate, endDate)
                        )
                        .orderBy(qOrderItem.orderInfo.seq.desc(), qOrderItem.createdDatetime.desc())
                        .offset(pageable.getOffset())
                        .limit(pageable.getPageSize())
                        .fetch();

        Long count
                = jpaQueryFactory.select(qOrderItem.count())
                .from(qOrderItem)
                .leftJoin(qOrderInfo)
                .on(qOrderInfo.seq.eq(qOrderItem.seq))
                .where(
                        eqBuyerCode(buyerCode)
                        , containsStaffId(staffId)
                        , containsCorpName(corpName)
                        , containsStaffName(staffName)
                        , containsSearchKeyword(searchKeyword)
                        , eqOrderItemState(orderStateList)
                        , searchDate(startDate, endDate)
                )
                .fetchOne();

        OrderInfoDto orderInfoDto = new OrderInfoDto();
        orderInfoDto.setOrderItemList(orderItemDtoList);
        orderInfoDto.setListCnt(count);
        return orderInfoDto;
    }

    /**
     * 주문 아이템 단건 조회. orderItem & orderInfo Join 조회.
     */
    public OrderItemDto getOrderItemWithOrderInfo(Long orderItemSeq, String buyerCode) {
        QOrderItem qOrderItem = QOrderItem.orderItem;
        QOrderInfo qOrderInfo = QOrderInfo.orderInfo;

        return jpaQueryFactory.select(Projections.constructor(OrderItemDto.class, qOrderItem, qOrderInfo))
                .from(qOrderItem)
                .leftJoin(qOrderInfo)
                .on(qOrderInfo.seq.eq(qOrderItem.orderInfo.seq))
                .where(
                        qOrderItem.seq.eq(orderItemSeq)
                        , eqBuyerCode(buyerCode)
                )
                .fetchOne();
    }

    /**
     * 주문 아이템 리스트 조회. orderItem 조회. - seq
     */
    public List<OrderItemDto> getOrderItemByInfoSeq(Long orderInfoSeq, String buyerCode) {
        QOrderItem qOrderItem = QOrderItem.orderItem;
        QOrderInfo qOrderInfo = QOrderInfo.orderInfo;

        return jpaQueryFactory.select(Projections.constructor(OrderItemDto.class, qOrderItem))
                .from(qOrderItem)
                .leftJoin(qOrderInfo)
                .on(qOrderInfo.seq.eq(qOrderItem.orderInfo.seq))
                .where(
                          eqOrderInfoSeq(orderInfoSeq)
                        , eqBuyerCode(buyerCode)
                )
                .fetch();
    }

    /**
     * 주문 아이템 리스트 조회. orderItem 조회. - buyerCode, orderStateList
     */
    public List<OrderItemDto> getOrderItemByBuyerCodeAndStateList(String buyerCode, List<Integer> orderItemStateList) {
        QOrderItem qOrderItem = QOrderItem.orderItem;
        QOrderInfo qOrderInfo = QOrderInfo.orderInfo;

        return jpaQueryFactory.select(Projections.constructor(OrderItemDto.class, qOrderItem))
                .from(qOrderItem)
                .leftJoin(qOrderInfo)
                .on(qOrderInfo.seq.eq(qOrderItem.orderInfo.seq))
                .where(
                          eqBuyerCode(buyerCode)
                        , eqOrderItemState(orderItemStateList)
                )
                .fetch();
    }

    public BooleanExpression eqOrderInfoSeq(Long seq) {
        if (ObjectUtils.isEmpty(seq)) {
            return null;
        }
        return QOrderInfo.orderInfo.seq.eq(seq);
    }

    public BooleanExpression containsSearchKeyword(String searchKeyword) {
        if (ObjectUtils.isEmpty(searchKeyword)) {
            return null;
        }
        return QOrderInfo.orderInfo.staffId.contains(searchKeyword)
                .or(QOrderInfo.orderInfo.corpName.contains(searchKeyword))
                .or(QOrderInfo.orderInfo.staffName.contains(searchKeyword));
    }

    public BooleanExpression eqOrderItemState(List<Integer> orderState) {
        if (orderState.size() == 0) {
            if (Objects.equals(SessionUtil.getUserType(), "admin")) {
                return QOrderItem.orderItem.orderItemState.ne(OrderStateEnum.ORDER_STANDBY.getCode()); // 임시 주문서 제외하고 조회.
            } else {
                return QOrderItem.orderItem.orderItemState.notIn(OrderStateEnum.ORDER_STANDBY.getCode(), OrderStateEnum.ORDER_ERROR.getCode()); // 임시 주문서, 주문오류 상태는 제외하고 조회.
            }
        }
        return QOrderItem.orderItem.orderItemState.in(orderState);
    }

    public BooleanExpression containsCorpName(String corpName) {
        if (ObjectUtils.isEmpty(corpName)) {
            return null;
        }
        return QOrderInfo.orderInfo.corpName.contains(corpName);
    }

    public BooleanExpression containsStaffId(String staffId) {
        if (ObjectUtils.isEmpty(staffId)) {
            return null;
        }
        return QOrderInfo.orderInfo.staffId.contains(staffId);
    }

    public BooleanExpression containsStaffName(String staffName) {
        if (ObjectUtils.isEmpty(staffName)) {
            return null;
        }
        return QOrderInfo.orderInfo.staffName.contains(staffName);
    }

    public BooleanExpression eqBuyerCode(String buyerCode) {
        if (ObjectUtils.isEmpty(buyerCode)) {
            return null;
        }
        return QOrderItem.orderItem.orderInfo.buyerCode.eq(buyerCode);
    }

    public BooleanExpression searchDate(String startDate, String endDate) {
        if (ObjectUtils.isEmpty(startDate) && ObjectUtils.isEmpty(endDate)) {
            return null;
        }
        return QOrderItem.orderItem.createdDatetime.between(
                LocalDateTime.parse(startDate + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                LocalDateTime.parse(endDate + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }
}
