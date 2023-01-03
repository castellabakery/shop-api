package kr.co.medicals.board.notice.domain.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@ToString
@Entity
@NoArgsConstructor
@DynamicUpdate
@EntityListeners(AuditingEntityListener.class)
@Table(name = "NOTICE")
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;
    private String boardType;
    private String title;
    private String content;
    private String displayYn;
    private String delYn;
    private String createdId;
    private String modifiedId;
    @CreatedDate
    private LocalDateTime createdDatetime;
    @LastModifiedDate
    private LocalDateTime modifiedDatetime;

    @Builder
    public Notice(Long seq, String boardType, String title, String content, String displayYn, String delYn, String createdId, String modifiedId, LocalDateTime createdDatetime, LocalDateTime modifiedDatetime) {
        this.seq = seq;
        this.boardType = boardType;
        this.title = title;
        this.content = content;
        this.displayYn = displayYn;
        this.delYn = delYn;
        this.createdId = createdId;
        this.modifiedId = modifiedId;
        this.createdDatetime = createdDatetime;
        this.modifiedDatetime = modifiedDatetime;
    }
}
