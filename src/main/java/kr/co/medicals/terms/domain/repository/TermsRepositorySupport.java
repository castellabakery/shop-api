package kr.co.medicals.terms.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.medicals.terms.domain.dto.TermsDto;
import kr.co.medicals.terms.domain.entity.QTerms;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Slf4j
@Repository
public class TermsRepositorySupport {
    private final JPAQueryFactory jpaQueryFactory;

    public TermsRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<TermsDto> findTermsListAll(String optionYn, String title, String content, List<Long> notEqSeqList){
        QTerms qTerms = QTerms.terms;
        return jpaQueryFactory.select(Projections.constructor(TermsDto.class, qTerms))
                .from(qTerms)
                .where(
                          containsTitle(title)
                        , containsContent(content)
                        , eqOptionYn(optionYn)
                        , notInSeqList(notEqSeqList)
                )
                .fetch();
    }

    public Page<TermsDto> findTermsList(String title, String content, Pageable pageable) {
        QTerms qTerms = QTerms.terms;
        List<TermsDto> termsDtoList
                = jpaQueryFactory.select(Projections.constructor(TermsDto.class, qTerms))
                .from(qTerms)
                .where(
                        containsTitle(title)
                        , containsContent(content)
                )
                .orderBy(qTerms.createdDatetime.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count
                = jpaQueryFactory.select(qTerms.count())
                .from(qTerms)
                .where(
                        containsTitle(title)
                        , containsContent(content)
                )
                .fetchOne();
        return new PageImpl<>(termsDtoList, pageable, count);
    }

    public TermsDto findTermsDetail(Long seq) {
        QTerms qTerms = QTerms.terms;
        return jpaQueryFactory.select(Projections.constructor(TermsDto.class, qTerms))
                .from(qTerms)
                .where(
                        eqTermsSeq(seq)
                )
                .orderBy(qTerms.createdDatetime.asc())
                .fetchOne();
    }

    public List<TermsDto> getExistsTermsList(List<Long> seqList) {
        QTerms qTerms = QTerms.terms;
        return jpaQueryFactory.select(Projections.constructor(TermsDto.class, qTerms))
                .from(qTerms)
                .where(
                        eqSeqList(seqList)
                )
                .fetch();
    }

    private BooleanExpression eqSeqList(List<Long> seqList) {
        if (ObjectUtils.isEmpty(seqList) || seqList.size() <= 0) {
            return null;
        }
        return QTerms.terms.seq.in(seqList);
    }

    private BooleanExpression notInSeqList(List<Long> notEqSeqList) {
        if (ObjectUtils.isEmpty(notEqSeqList) || notEqSeqList.size() <= 0) {
            return null;
        }
        return QTerms.terms.seq.notIn(notEqSeqList);
    }

    private BooleanExpression eqTermsSeq(Long seq) {
        if (ObjectUtils.isEmpty(seq)) {
            return null;
        }
        return QTerms.terms.seq.eq(seq);
    }
    private BooleanExpression eqOptionYn(String optionYn) {
        if (ObjectUtils.isEmpty(optionYn)) {
            return null;
        }
        return QTerms.terms.optionYn.eq(optionYn);
    }

    private BooleanExpression containsTitle(String title) {
        if (ObjectUtils.isEmpty(title)) {
            return null;
        }
        return QTerms.terms.title.contains(title);
    }

    private BooleanExpression containsContent(String content) {
        if (ObjectUtils.isEmpty(content)) {
            return null;
        }
        return QTerms.terms.content.contains(content);
    }

}
