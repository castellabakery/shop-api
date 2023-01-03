package kr.co.medicals.cart.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.medicals.cart.domain.dto.CartDto;
import kr.co.medicals.cart.domain.entity.QCart;
import kr.co.medicals.product.domain.entity.QBuyerTypeProductAmount;
import kr.co.medicals.product.domain.entity.QProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Slf4j
@Repository
public class CartRepositorySupport {

    private final JPAQueryFactory jpaQueryFactory;

    @Autowired
    public CartRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<CartDto> myCartList(String buyerCode, String buyerType, List<Long> cartSeqList) {

        QCart qCart = QCart.cart;
        QProduct qProduct = QProduct.product;
        QBuyerTypeProductAmount qBuyerTypeProductAmount = QBuyerTypeProductAmount.buyerTypeProductAmount;

        return jpaQueryFactory.select(Projections.constructor(CartDto.class, qCart, qProduct, qBuyerTypeProductAmount))
                .from(qCart)
                .leftJoin(qProduct)
                .on(qProduct.seq.eq(qCart.product.seq))
                .leftJoin(qBuyerTypeProductAmount)
                .on(qProduct.seq.eq(qBuyerTypeProductAmount.product.seq))
                .where(
                        qCart.buyerCode.eq(buyerCode)
                        , qBuyerTypeProductAmount.buyerType.eq(buyerType)
                        , eqNotDelete()
                        , eqSeqIn(cartSeqList)
                )
                .orderBy(QCart.cart.createdDatetime.desc())
                .fetch();
    }

    public List<CartDto> findCartByProductSeqList(List<Long> productSeqList, String buyerCode) {

        QCart qCart = QCart.cart;

        return jpaQueryFactory.select(Projections.constructor(CartDto.class, qCart))
                .from(qCart)
                .where(
                        eqProductSeqIn(productSeqList)
                        , qCart.buyerCode.eq(buyerCode)
                        , eqNotDelete()
                )
                .orderBy(QCart.cart.createdDatetime.desc())
                .fetch();
    }

    public BooleanExpression eqNotDelete() {
        return QCart.cart.delYn.eq("N");
    }

    public BooleanExpression eqSeqIn(List<Long> cartSeq) {
        if (ObjectUtils.isEmpty(cartSeq)) {
            return null;
        }
        return QCart.cart.seq.in(cartSeq);
    }

    public BooleanExpression eqProductSeqIn(List<Long> productSeqList) {
        if (ObjectUtils.isEmpty(productSeqList)) {
            return null;
        }
        return QCart.cart.product.seq.in(productSeqList);
    }

}
