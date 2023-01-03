package kr.co.medicals.terms.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.medicals.terms.domain.dto.TmpBuyerTermsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Slf4j
@Repository
public class TmpBuyerTermsRepositorySupport {

    private final JPAQueryFactory jpaQueryFactory;

    public TmpBuyerTermsRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<TmpBuyerTermsDto> getTmpBuyerTermsList(Long tmpSeq) {
        QTmpBuyerTerms qTmpBuyerTerms = QTmpBuyerTerms.tmpBuyerTerms;
        QTerms qTerms = QTerms.terms;

        return jpaQueryFactory.select(Projections.constructor(TmpBuyerTermsDto.class, qTmpBuyerTerms, qTerms))
                .from(qTmpBuyerTerms)
                .leftJoin(qTerms)
                .on(qTmpBuyerTerms.terms.seq.eq(qTerms.seq))
                .where(
                        eqTmpSeq(tmpSeq)
                        , qTmpBuyerTerms.delYn.eq("N")
                )
                .orderBy(qTerms.createdDatetime.asc())
                .fetch();
    }

    private BooleanExpression eqTmpSeq(Long tmpSeq) {
        if (ObjectUtils.isEmpty(tmpSeq)) {
            return null;
        }
        return QTmpBuyerTerms.tmpBuyerTerms.tmpSeq.eq(tmpSeq);
    }

}
