package kr.co.medicals.terms.domain.entity;

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
@Table(name = "TERMS")
public class Terms {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;
    private String title;
    private String content;
    private String optionYn;
    private String createdId;
    private LocalDateTime createdDatetime;
    private String modifiedId;
    private LocalDateTime modifiedDatetime;

    @Builder
    public Terms(Long seq, String title, String content, String optionYn, String createdId, String modifiedId, LocalDateTime createdDatetime, LocalDateTime modifiedDatetime) {
        this.seq = seq;
        this.title = title;
        this.content = content;
        this.optionYn = optionYn;
        this.createdId = createdId;
        this.createdDatetime = createdDatetime;
        this.modifiedId = modifiedId;
        this.modifiedDatetime = modifiedDatetime;
    }
}
