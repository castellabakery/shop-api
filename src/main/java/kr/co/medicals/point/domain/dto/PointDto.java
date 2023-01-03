package kr.co.medicals.point.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.medicals.common.util.SessionUtil;
import kr.co.medicals.point.domain.entity.Point;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PointDto {

    private Long seq;
    private String buyerCode;
    private int savePoint;
    private int saveExpectPoint;
    private int usePoint;
    private String createdId;
    private LocalDateTime createdDatetime;
    private String modifiedId;
    private LocalDateTime modifiedDatetime;
    private PointHistoryDto pointHistoryDto;

    private List<PointHistoryDto> pointHistoryDtoList;
    private Long listCnt;

    public PointDto() {
    }

    @Builder(builderClassName = "byCreate", builderMethodName = "byCreate")
    public PointDto(Long seq, String buyerCode, int savePoint, int saveExpectPoint, int usePoint, PointHistoryDto pointHistoryDto, String createdId, LocalDateTime createdDatetime, String modifiedId, LocalDateTime modifiedDatetime) {
        this.seq = seq;
        this.buyerCode = buyerCode;
        this.savePoint = savePoint;
        this.saveExpectPoint = saveExpectPoint;
        this.usePoint = usePoint;
        this.pointHistoryDto = pointHistoryDto;
        this.createdId = createdId;
        this.createdDatetime = createdDatetime;
        this.modifiedId = modifiedId;
        this.modifiedDatetime = modifiedDatetime;
    }

    @Builder(builderClassName = "selectByEntity", builderMethodName = "selectByEntity")
    public PointDto(Point point) {
        this.seq = point.getSeq();
        this.buyerCode = point.getBuyerCode();
        this.savePoint = point.getSavePoint();
        this.saveExpectPoint = point.getSaveExpectPoint();
        this.usePoint = point.getUsePoint();
        this.createdId = point.getCreatedId();
        this.createdDatetime = point.getCreatedDatetime();
        this.modifiedId = point.getModifiedId();
        this.modifiedDatetime = point.getModifiedDatetime();
    }

    public Point setDefaultPoint(String buyerCode) {
        return Point
                .builder()
                .buyerCode(buyerCode)
                .savePoint(0)
                .saveExpectPoint(0)
                .usePoint(0)
                .createdId(SessionUtil.getUserCode())
                .createdDatetime(LocalDateTime.now())
                .build();
    }

    // 연산
    public Point updateEntity(String buyerIdentificationCode) {
        return Point
                .builder()
                .seq(this.seq)
                .buyerCode(this.buyerCode)
                .savePoint(this.savePoint)
                .saveExpectPoint(this.saveExpectPoint)
                .usePoint(this.usePoint)
                .createdId(this.createdId)
                .createdDatetime(this.createdDatetime)
                .modifiedId(buyerIdentificationCode)
                .modifiedDatetime(LocalDateTime.now())
                .build();
    }


}
