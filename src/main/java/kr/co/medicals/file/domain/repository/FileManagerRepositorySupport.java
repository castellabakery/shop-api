package kr.co.medicals.file.domain.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.medicals.file.domain.dto.FileManagerDto;
import kr.co.medicals.file.domain.entity.QFileManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.List;


@Slf4j
@Repository
public class FileManagerRepositorySupport {

    private final JPAQueryFactory jpaQueryFactory;

    public FileManagerRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<FileManagerDto> findFileList(Long relationSeq, List<Integer> fileTypes, List<String> fileUploadNames) {

        QFileManager qFileManager = QFileManager.fileManager;

        return jpaQueryFactory.select(Projections.constructor(FileManagerDto.class, qFileManager))
                .from(qFileManager)
                .where(
                          eqRelationSeqAndFileTypes(relationSeq, fileTypes)
                        , inUploadFileName(fileUploadNames)
                        , eqNotDelete()
                )
                .orderBy(qFileManager.createdDatetime.asc())
                .fetch();
    }

    public List<FileManagerDto> findFileForUploadName(List<String> uploadFileName){
        QFileManager qFileManager = QFileManager.fileManager;

        return jpaQueryFactory.select(Projections.constructor(FileManagerDto.class, qFileManager))
                .from(qFileManager)
                .where(
                          inUploadFileName(uploadFileName)
                        , eqNotDelete()
                )
                .fetch();
    }

    private BooleanExpression inUploadFileName(List<String> uploadFileName) {
        if(ObjectUtils.isEmpty(uploadFileName) || uploadFileName.size() >= 1){
            return null;
        }
        return QFileManager.fileManager.uploadFileName.in(uploadFileName);
    }

    private BooleanExpression eqRelationSeqAndFileTypes(Long relationSeq, List<Integer> fileTypeList) {
        if(ObjectUtils.isEmpty(relationSeq) || ObjectUtils.isEmpty(fileTypeList) || fileTypeList.size() == 0){
            return null;
        }
        return QFileManager.fileManager.relationSeq.eq(relationSeq).and(QFileManager.fileManager.fileType.in(fileTypeList));
    }

    private BooleanExpression eqNotDelete() {
        return QFileManager.fileManager.delYn.eq("N");
    }



}
