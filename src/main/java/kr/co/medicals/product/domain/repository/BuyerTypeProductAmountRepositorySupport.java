package kr.co.medicals.product.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.medicals.product.domain.dto.BuyerTypeProductAmountDto;
import kr.co.medicals.product.domain.entity.QBuyerTypeProductAmount;
import kr.co.medicals.product.domain.entity.QProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Slf4j
@Repository
public class BuyerTypeProductAmountRepositorySupport {

    private final JPAQueryFactory jpaQueryFactory;

    @Autowired
    public BuyerTypeProductAmountRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<BuyerTypeProductAmountDto> getAmountList(Long productSeq, String buyerType) {

        QBuyerTypeProductAmount qBuyerTypeProductAmount = QBuyerTypeProductAmount.buyerTypeProductAmount;

        return jpaQueryFactory.select(Projections.constructor(BuyerTypeProductAmountDto.class, qBuyerTypeProductAmount))
                .from(qBuyerTypeProductAmount)
                .where(
                          qBuyerTypeProductAmount.product.seq.eq(productSeq) // 필수 조건.
                        , notDeleteAmount()
                        , eqBuyerType(buyerType)
                )
                .fetch();
    }

    public BooleanExpression eqBuyerType(String buyerType){
        if(ObjectUtils.isEmpty(buyerType)){
            return null;
        }
        return QBuyerTypeProductAmount.buyerTypeProductAmount.buyerType.eq(buyerType);
    }

    public BooleanExpression notDeleteAmount(){
        return QBuyerTypeProductAmount.buyerTypeProductAmount.delYn.eq("N");
    }

}
