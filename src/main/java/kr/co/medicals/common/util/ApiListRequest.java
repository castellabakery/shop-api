package kr.co.medicals.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiListRequest {

    private final int DEFAULT_PAGE_NO = 1;
    private final int DEFAULT_PAGE_LEN = 10;

    private Integer pageNo;
    private Integer len;

    public ApiListRequest(Integer pageNo, Integer len) {
        this.pageNo = pageNo == null ? DEFAULT_PAGE_NO : pageNo;
        this.len = len == null ? DEFAULT_PAGE_LEN : len;
    }

}
