package kr.co.medicals.point.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.medicals.order.domain.entity.QOrderItem;
import kr.co.medicals.point.domain.dto.PointDto;
import kr.co.medicals.point.domain.entity.QPoint;
import kr.co.medicals.point.domain.entity.QPointHistory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
public class PointRepositorySupport {

    private final JPAQueryFactory jpaQueryFactory;

    public PointRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public PointDto getBuyerPoint(String buyerCode) {

        QPoint qPoint = QPoint.point;

        return jpaQueryFactory.select(Projections.constructor(PointDto.class, qPoint))
                .from(qPoint)
                .where(
                        qPoint.buyerCode.eq(buyerCode)
                )
                .fetchOne();
    }

}
