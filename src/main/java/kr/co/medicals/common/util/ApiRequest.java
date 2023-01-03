package kr.co.medicals.common.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiRequest {

    private final int page;
    private final int pageSize;
    private Map<String, Object> map;

    public ApiRequest(int page, int pageSize, Map<String, Object> map) {
        this.page = page;
        this.pageSize = pageSize;
        this.map = map;
    }

}
