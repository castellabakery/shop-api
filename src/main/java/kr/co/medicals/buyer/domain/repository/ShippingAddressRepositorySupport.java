package kr.co.medicals.buyer.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.medicals.buyer.domain.dto.ShippingAddressDto;
import kr.co.medicals.buyer.domain.entity.QShippingAddress;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Slf4j
@Repository
public class ShippingAddressRepositorySupport {

    private final JPAQueryFactory jpaQueryFactory;

    @Autowired
    public ShippingAddressRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<ShippingAddressDto> findShippingAddress(String buyerCode) {

        QShippingAddress qShippingAddress = QShippingAddress.shippingAddress;

        return jpaQueryFactory.select(Projections.constructor(ShippingAddressDto.class, qShippingAddress))
                .from(qShippingAddress)
                .where(
                        qShippingAddress.buyerCode.eq(buyerCode)
                        , eqDelYn()
                )
                .orderBy(qShippingAddress.createdDatetime.desc())
                .fetch();
    }

    public Long countShippingAddress(String buyerCode) {

        QShippingAddress qShippingAddress = QShippingAddress.shippingAddress;

        return jpaQueryFactory.select(qShippingAddress.count())
                .from(qShippingAddress)
                .where(
                        qShippingAddress.buyerCode.eq(buyerCode)
                        , eqDelYn()
                )
                .fetchOne();
    }

    public ShippingAddressDto lastSeqShippingAddress(String buyerCode) {

        QShippingAddress qShippingAddress = QShippingAddress.shippingAddress;

        return jpaQueryFactory.select(Projections.constructor(ShippingAddressDto.class, qShippingAddress))
                .from(qShippingAddress)
                .where(
                        qShippingAddress.buyerCode.eq(buyerCode)
                        , eqDelYn()
                )
                .orderBy(qShippingAddress.addressSeq.desc())
                .limit(1)
                .fetchOne();
    }

    public ShippingAddressDto findShippingAddressOne(Long seq, String buyerCode) {

        QShippingAddress qShippingAddress = QShippingAddress.shippingAddress;

        return jpaQueryFactory.select(Projections.constructor(ShippingAddressDto.class, qShippingAddress))
                .from(qShippingAddress)
                .where(
                        qShippingAddress.buyerCode.eq(buyerCode)
                        , eqSeq(seq)
                        , eqDelYn()
                )
                .fetchOne();
    }

    public BooleanExpression eqSeq(Long seq) {
        if (ObjectUtils.isEmpty(seq) || seq.intValue() <= 0) {
            return null;
        }
        return QShippingAddress.shippingAddress.seq.eq(seq);
    }

    public BooleanExpression eqDelYn() {
        return QShippingAddress.shippingAddress.delYn.eq("N");
    }
}
