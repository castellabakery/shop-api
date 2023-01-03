package kr.co.medicals.terms.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.medicals.terms.domain.dto.BuyerTermsDto;
import kr.co.medicals.terms.domain.entity.QBuyerTerms;
import kr.co.medicals.terms.domain.entity.QTerms;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Slf4j
@Repository
public class BuyerTermsRepositorySupport {
    private final JPAQueryFactory jpaQueryFactory;

    public BuyerTermsRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<BuyerTermsDto> getBuyerTermsList(String buyerCode, List<Long> termsSeqList){

        QBuyerTerms qbuyerTerms = QBuyerTerms.buyerTerms;
        QTerms qTerms = QTerms.terms;

        return jpaQueryFactory.select(Projections.constructor(BuyerTermsDto.class, qbuyerTerms, qTerms))
                .from(qbuyerTerms)
                .leftJoin(qTerms)
                    .on(qbuyerTerms.terms.seq.eq(qTerms.seq))
                .where(
                          eqBuyerCode(buyerCode)
                        , eqTermsSeq(termsSeqList)
                        , qbuyerTerms.delYn.ne("N")
                )
                .orderBy(qTerms.createdDatetime.asc())
                .fetch();
    }

    private BooleanExpression eqBuyerCode(String buyerCode) {
        if (ObjectUtils.isEmpty(buyerCode)) {
            return null;
        }
        return QBuyerTerms.buyerTerms.buyerCode.eq(buyerCode);
    }

    private BooleanExpression eqTermsSeq(List<Long> termsSeqList) {
        if (ObjectUtils.isEmpty(termsSeqList) || termsSeqList.size() == 0) {
            return null;
        }
        return QBuyerTerms.buyerTerms.terms.seq.in(termsSeqList);
    }


}
