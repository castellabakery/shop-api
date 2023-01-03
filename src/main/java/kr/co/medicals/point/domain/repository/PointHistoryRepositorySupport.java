package kr.co.medicals.point.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.medicals.order.domain.entity.QOrderInfo;
import kr.co.medicals.order.domain.entity.QOrderItem;
import kr.co.medicals.point.domain.dto.PointDto;
import kr.co.medicals.point.domain.dto.PointHistoryDto;
import kr.co.medicals.point.domain.entity.QPoint;
import kr.co.medicals.point.domain.entity.QPointHistory;
import kr.co.medicals.product.domain.entity.QProduct;
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
public class PointHistoryRepositorySupport {

    private final JPAQueryFactory jpaQueryFactory;

    public PointHistoryRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public Page<PointHistoryDto> getPointHistoryByBuyer(String buyerCode, String startDate, String endDate, List<String> pointState, Pageable pageable) {

        QPointHistory qPointHistory = QPointHistory.pointHistory;

        List<PointHistoryDto> pointHistoryDtoList = jpaQueryFactory.select(Projections.constructor(PointHistoryDto.class, qPointHistory))
                .from(qPointHistory)
                .where(
                          eqBuyerCode(buyerCode)
                        , eqPointState(pointState)
                        , searchDate(startDate, endDate)
                )
                .orderBy(qPointHistory.createdDatetime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = jpaQueryFactory.select(qPointHistory.count())
                .from(qPointHistory)
                .where(
                        eqBuyerCode(buyerCode)
                        , eqPointState(pointState)
                        , searchDate(startDate, endDate)
                )
                .fetchOne();

        return new PageImpl<>(pointHistoryDtoList, pageable, count);
    }

    public PointDto getPointHistoryByAdmin(String startDate, String endDate, List<String> pointState, String orderNo, String productDisplayName, String buyerIdentificationId, String searchKeyword, Pageable pageable) {

        QPointHistory qPointHistory = QPointHistory.pointHistory;
        QOrderInfo qOrderInfo = QOrderInfo.orderInfo;
        QOrderItem qOrderItem = QOrderItem.orderItem;
        QProduct qProduct = QProduct.product;

        List<PointHistoryDto> pointHistoryDtoList = jpaQueryFactory.select(Projections.constructor(PointHistoryDto.class, qPointHistory, qOrderInfo, qOrderItem, qProduct))
                .from(qPointHistory)
                .leftJoin(qOrderInfo).on(qPointHistory.orderNo.eq(qOrderInfo.orderNo))
                .leftJoin(qOrderItem).on(qOrderInfo.seq.eq(qOrderItem.orderInfo.seq))
                .leftJoin(qProduct).on(qOrderItem.productSeq.eq(qProduct.seq))
                .where(
                          searchDate(startDate, endDate)
                        , eqPointState(pointState)
                        , containsOrderNo(orderNo)
                        , containsSearchKeyword(searchKeyword)
                        , containsBuyerIdentificationId(buyerIdentificationId)
                        , containsProductDisplayName(productDisplayName)
                )
                .orderBy(qPointHistory.createdDatetime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = jpaQueryFactory.select(qPointHistory.count())
                .from(qPointHistory)
                .leftJoin(qOrderInfo).on(qPointHistory.orderNo.eq(qOrderInfo.orderNo))
                .leftJoin(qOrderItem).on(qOrderInfo.seq.eq(qOrderItem.orderInfo.seq))
                .leftJoin(qProduct).on(qOrderItem.productSeq.eq(qProduct.seq))
                .where(
                        searchDate(startDate, endDate)
                        , eqPointState(pointState)
                        , containsOrderNo(orderNo)
                        , containsSearchKeyword(searchKeyword)
                        , containsBuyerIdentificationId(buyerIdentificationId)
                        , containsProductDisplayName(productDisplayName)
                )
                .fetchOne();

        PointDto pointDto = new PointDto();
        pointDto.setPointHistoryDtoList(pointHistoryDtoList);
        pointDto.setListCnt(count);

        return pointDto;
    }

    public BooleanExpression containsSearchKeyword(String searchKeyword) {
        if (ObjectUtils.isEmpty(searchKeyword)) {
            return null;
        }
        return QOrderInfo.orderInfo.staffId.contains(searchKeyword)
                .or(QPointHistory.pointHistory.orderNo.contains(searchKeyword))
                .or(QProduct.product.productDisplayName.contains(searchKeyword));
    }

    public BooleanExpression containsBuyerIdentificationId(String buyerIdentificationId) {
        if (ObjectUtils.isEmpty(buyerIdentificationId)) {
            return null;
        }
        return QOrderInfo.orderInfo.staffId.contains(buyerIdentificationId);
    }
    public BooleanExpression containsProductDisplayName(String productDisplayName) {
        if (ObjectUtils.isEmpty(productDisplayName)) {
            return null;
        }
        return QOrderItem.orderItem.productDisplayName.contains(productDisplayName);
    }

    public BooleanExpression eqBuyerCode(String buyerCode) {
        if (ObjectUtils.isEmpty(buyerCode)) {
            return null;
        }
        return QPointHistory.pointHistory.buyerCode.eq(buyerCode);
    }


    public BooleanExpression eqPointState(List<String> pointState) {
        if ( ObjectUtils.isEmpty(pointState) || pointState.size() == 0 ) {
            return null;
        }
        return QPointHistory.pointHistory.pointState.in(pointState);
    }

    public BooleanExpression searchDate(String startDate, String endDate) {
        if (ObjectUtils.isEmpty(startDate) && ObjectUtils.isEmpty(endDate)) {
            return null;
        }
        return QPointHistory.pointHistory.createdDatetime.between(
                LocalDateTime.parse(startDate + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                LocalDateTime.parse(endDate + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    public BooleanExpression containsOrderNo(String orderNo) {
        if (ObjectUtils.isEmpty(orderNo)) {
            return null;
        }
        return QPointHistory.pointHistory.orderNo.contains(orderNo);
    }

}
