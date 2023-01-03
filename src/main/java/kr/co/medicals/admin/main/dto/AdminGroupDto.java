package kr.co.medicals.admin.main.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class AdminGroupDto {

    private Long seq;
    private String defaultGroupYn;
    private String groupName;
    private String createdId;
    private LocalDateTime createdDatetime;
    private String modifiedId;
    private LocalDateTime modifiedDatetime;

    public AdminGroupDto(){
    }

    @Builder(builderClassName = "byCreate", builderMethodName = "byCreate")
    public AdminGroupDto(Long seq, String defaultGroupYn, String groupName, String createdId, LocalDateTime createdDatetime, String modifiedId, LocalDateTime modifiedDatetime) {
        this.seq = seq;
        this.defaultGroupYn = defaultGroupYn;
        this.groupName = groupName;
        this.createdId = createdId;
        this.createdDatetime = createdDatetime;
        this.modifiedId = modifiedId;
        this.modifiedDatetime = modifiedDatetime;
    }

}
