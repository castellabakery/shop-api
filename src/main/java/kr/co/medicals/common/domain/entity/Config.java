package kr.co.medicals.common.domain.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@ToString
@Entity
@NoArgsConstructor
public class Config {

    @EmbeddedId
    private ConfigIds configIds;

    @Column(name="CONFIG_VAL", length = 500, nullable = false)
    private String configVal;
    @Column(name="DETAIL", length = 50, nullable = false)
    private String detail;
    @Column(name="CREATED_DATETIME", nullable = false)
    private LocalDateTime createdDatetime;
    @Column(name="MODIFIED_DATETIME")
    private LocalDateTime modifiedDatetime;

    @Builder
    public Config(ConfigIds configIds, String configVal, String detail, LocalDateTime createdDatetime, LocalDateTime modifiedDatetime) {
        this.configIds = configIds;
        this.configVal = configVal;
        this.detail = detail;
        this.createdDatetime = createdDatetime;
        this.modifiedDatetime = modifiedDatetime;
    }
}
