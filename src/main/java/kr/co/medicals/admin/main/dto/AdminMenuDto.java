package kr.co.medicals.admin.main.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class AdminMenuDto {
    private String key;
    private String labelLink;
    private String labelName;
    private List<AdminMenuDto> innerMenu;

    @Builder
    public AdminMenuDto(String key, String labelLink, String labelName, List<AdminMenuDto> innerMenu) {
        this.key = key;
        this.labelLink = labelLink;
        this.labelName = labelName;
        this.innerMenu = innerMenu;
    }
}
