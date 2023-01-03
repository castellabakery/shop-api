package kr.co.medicals.file.domain.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@ToString
@Entity
@NoArgsConstructor
@DynamicUpdate
@Table(name = "FILE_MANAGER")
public class FileManager {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;
    private Long relationSeq;
    private int fileType;
    private String fileExtension;
    private int fileSize;
    private String orgFileName;
    private String uploadFileName;
    private String filePath;
    private String delYn;
    private String createdId;
    private String modifiedId;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifiedDatetime;

    @Builder
    public FileManager(Long seq, Long relationSeq, int fileType, String fileExtension, int fileSize, String orgFileName, String uploadFileName, String filePath, String delYn, String createdId, String modifiedId, LocalDateTime createdDatetime, LocalDateTime modifiedDatetime) {
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
}
