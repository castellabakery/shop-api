package kr.co.medicals.buyer.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.medicals.common.util.Encrypt;
import kr.co.medicals.file.domain.dto.FileManagerDto;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BuyerIdentificationDto {

    private Long seq;
    private BuyerDto buyer = new BuyerDto();
    private String buyerIdentificationCode;
    private String identificationType;
    private String buyerIdentificationState;
    private String buyerIdentificationId;
    private String password;
    private String staffName;
    private String staffTelNo;
    private String staffPhoneNo;
    private String staffEmail;
    private String createdId;
    private String modifiedId;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifiedDatetime;
    private List<FileManagerDto> fileList = new ArrayList<>();
    private String searchKeyword;

    public BuyerIdentificationDto() {
    }
    public BuyerIdentificationDto encryptPassword() {
        if (!ObjectUtils.isEmpty(password)) {
            this.password = Encrypt.sha256(password);
        }
        return this;
    }

}
