package kr.co.medicals.buyer;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.medicals.common.util.ApiListRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BuyerListRequest extends ApiListRequest {
    private String searchConditionSelect;
    private String searchConditionText;

    public BuyerListRequest(Integer pageNo, Integer len, String searchConditionSelect, String searchConditionText) {
        super(pageNo, len);
        this.searchConditionSelect = searchConditionSelect;
        this.searchConditionText = searchConditionText;
    }
}
