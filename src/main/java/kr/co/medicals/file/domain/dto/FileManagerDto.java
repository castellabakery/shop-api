package kr.co.medicals.file.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.medicals.common.constants.PropertiesConstants;
import kr.co.medicals.common.enums.FileTypeEnum;
import kr.co.medicals.file.domain.entity.FileManager;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileManagerDto {

    private Long seq;
    private Long relationSeq;
    private int fileType;
    private String fileExtension;
    private int fileSize;
    private String orgFileName;
    private String uploadFileName;
    private String filePath;
    private String fullFilePath;
    private String delYn;
    private String createdId;
    private String modifiedId;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifiedDatetime;

    public FileManagerDto() {
    }

    @Builder(builderClassName = "byCreate", builderMethodName = "byCreate")
    public FileManagerDto(Long seq, Long relationSeq, int fileType, String fileExtension, int fileSize, String orgFileName, String uploadFileName, String filePath, String fullFilePath, String delYn, String createdId, String modifiedId, LocalDateTime createdDatetime, LocalDateTime modifiedDatetime) {
        this.seq = seq;
        this.relationSeq = relationSeq;
        this.fileType = fileType;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.orgFileName = orgFileName;
        this.uploadFileName = uploadFileName;
        this.filePath = filePath;
        this.delYn = delYn;
        this.createdId = createdId;
        this.modifiedId = modifiedId;
        this.createdDatetime = createdDatetime;
        this.modifiedDatetime = modifiedDatetime;
    }

    @Builder(builderClassName = "selectByEntity", builderMethodName = "selectByEntity")
    public FileManagerDto(FileManager fileManager) {
        this.seq = fileManager.getSeq();
        this.relationSeq = fileManager.getRelationSeq();
        this.fileType = fileManager.getFileType();
        this.fileExtension = fileManager.getFileExtension();
        this.fileSize = fileManager.getFileSize();
        this.orgFileName = fileManager.getOrgFileName();
        this.uploadFileName = fileManager.getUploadFileName();
        this.filePath = fileManager.getFilePath();
        this.fullFilePath = PropertiesConstants.FILE_ADDR+"/"+fileManager.getFilePath()+"/"+fileManager.getUploadFileName();
        this.delYn = fileManager.getDelYn();
        this.createdId = fileManager.getCreatedId();
        this.modifiedId = fileManager.getModifiedId();
        this.createdDatetime = fileManager.getCreatedDatetime();
        this.modifiedDatetime = fileManager.getModifiedDatetime();
    }

    public FileManager insertEntity(String userCode) {
        return FileManager
                .builder()
                .relationSeq(this.relationSeq)
                .fileType(this.fileType)
                .fileExtension(this.fileExtension)
                .fileSize(this.fileSize)
                .orgFileName(this.orgFileName)
                .uploadFileName(this.uploadFileName)
                .filePath(this.filePath)
                .delYn("N")
                .createdId(userCode)
                .createdDatetime(LocalDateTime.now())
                .build();
    }

    public FileManager updateDelYEntity(String userCode) {
        return FileManager
                .builder()
                .seq(this.seq)
                .relationSeq(this.relationSeq)
                .fileType(this.fileType)
                .fileExtension(this.fileExtension)
                .fileSize(this.fileSize)
                .orgFileName(this.orgFileName)
                .uploadFileName(this.uploadFileName)
                .filePath(this.filePath)
                .delYn("Y")
                .createdId(this.createdId)
                .createdDatetime(this.createdDatetime)
                .modifiedId(userCode)
                .modifiedDatetime(LocalDateTime.now())
                .build();
    }

    public FileManager copyDataInsertEntity(FileManagerDto oldDto, FileTypeEnum fileTypeEnum){
        return FileManager
                .builder()
                .relationSeq(oldDto.getRelationSeq())
                .fileType(fileTypeEnum.getCode())
                .fileExtension(oldDto.getFileExtension())
                .fileSize(oldDto.getFileSize())
                .orgFileName(oldDto.getOrgFileName())
                .uploadFileName(oldDto.getUploadFileName())
                .filePath(fileTypeEnum.getPath())
                .delYn("N")
                .createdId(oldDto.getCreatedId())
                .createdDatetime(LocalDateTime.now())
                .build();
    }

}
