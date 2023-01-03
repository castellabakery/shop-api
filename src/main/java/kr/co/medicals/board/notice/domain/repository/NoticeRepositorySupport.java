package kr.co.medicals.board.notice.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.medicals.board.notice.domain.dto.NoticeDto;
import kr.co.medicals.board.notice.domain.entity.QNotice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Slf4j
@Repository
public class NoticeRepositorySupport {

    private final JPAQueryFactory jpaQueryFactory;

    @Autowired
    public NoticeRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    /**
     * 공지사항 리스트 조회.
     */
    public Page<NoticeDto> searchNoticeList(NoticeDto searchParam, String displayYn, Pageable pageable) {

        QNotice qNotice = QNotice.notice;
//
        List<NoticeDto> noticeDtoList = jpaQueryFactory.select(Projections.constructor(NoticeDto.class, qNotice))
                .from(qNotice)
                .where(
                        eqDelN()
                        , eqDisplayYn(displayYn)
                        , containsTitle(searchParam.getTitle())
                        , containsContent(searchParam.getContent())
                )
                .orderBy(qNotice.createdDatetime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = jpaQueryFactory.select(qNotice.count())
                .from(qNotice)
                .where(
                        eqDelN()
                        , eqDisplayYn(displayYn)
                        , containsTitle(searchParam.getTitle())
                        , containsContent(searchParam.getContent())
                )
                .fetchOne();

        return new PageImpl<>(noticeDtoList, pageable, count);
    }

    /**
     * 공지사항 seq 기준으로 조회.
     */
    public NoticeDto getNoticeDetailBySeq(Long seq, String displayYn) {

        QNotice qNotice = QNotice.notice;

        return jpaQueryFactory.select(Projections.constructor(NoticeDto.class, qNotice))
                .from(qNotice)
                .where(
                          eqSeq(seq)
                        , eqDelN()
                        , eqDisplayYn(displayYn)
                )
                .fetchOne();
    }

    public BooleanExpression eqSeq(Long seq) {
        if (ObjectUtils.isEmpty(seq)) {
            return null;
        }
        return QNotice.notice.seq.eq(seq);
    }

    public BooleanExpression eqDelN() {
        return QNotice.notice.delYn.eq("N");
    }

    public BooleanExpression eqDisplayYn(String displayYn) {
        if (ObjectUtils.isEmpty(displayYn)) {
            return null;
        }
        return QNotice.notice.displayYn.eq(displayYn);
    }

    public BooleanExpression containsTitle(String title) {
        if (ObjectUtils.isEmpty(title)) {
            return null;
        }
        return QNotice.notice.title.contains(title);
    }

    public BooleanExpression containsContent(String content) {
        if (ObjectUtils.isEmpty(content)) {
            return null;
        }
        return QNotice.notice.content.contains(content);
    }
}
