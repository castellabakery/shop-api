package kr.co.medicals.admin.main.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class AdminDto {
    private Long seq;
    private AdminGroupDto adminGroup;
    private String adminCode;
    private String adminId;
    private String password;
    private String name;
    private String telNo;
    private String phoneNo;
    private String email;
    private String state;
    private String createdId;
    private LocalDateTime createdDatetime;
    private String modifiedId;
    private LocalDateTime modifiedDatetime;
    private String role;
    private String refreshIdx;

    public AdminDto() {
    }

    @Builder(builderClassName = "byCreate", builderMethodName = "byCreate")
    public AdminDto(Long seq, AdminGroupDto adminGroup, String adminCode, String adminId, String password, String name, String telNo, String phoneNo, String email, String state, String createdId, LocalDateTime createdDatetime, String modifiedId, LocalDateTime modifiedDatetime, String role, String refreshIdx) {
        this.seq = seq;
        this.adminGroup = adminGroup;
        this.adminCode = adminCode;
        this.adminId = adminId;
        this.password = password;
        this.name = name;
        this.telNo = telNo;
        this.phoneNo = phoneNo;
        this.email = email;
        this.state = state;
        this.createdId = createdId;
        this.createdDatetime = createdDatetime;
        this.modifiedId = modifiedId;
        this.modifiedDatetime = modifiedDatetime;
        this.role = role;
        this.refreshIdx = refreshIdx;
    }

}
