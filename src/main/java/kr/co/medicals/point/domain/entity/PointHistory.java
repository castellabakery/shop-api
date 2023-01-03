package kr.co.medicals.point.domain.entity;

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
@Table(name = "POINT_HISTORY")
public class PointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;
    private String buyerCode;
    private String orderNo;
    private String pointState;
    private int applyPoint;
    private int payAmount;
    private String content;
    private int beforeSavePoint;
    private int afterSavePoint;
    private int beforeSaveExpectPoint;
    private int afterSaveExpectPoint;
    private int beforeUsePoint;
    private int afterUsePoint;
    private String createdId;
    private LocalDateTime createdDatetime;

    @Builder
    public PointHistory(Long seq, String buyerCode, String orderNo, String pointState, int applyPoint, int payAmount, String content, int beforeSavePoint, int afterSavePoint, int beforeSaveExpectPoint, int afterSaveExpectPoint, int beforeUsePoint, int afterUsePoint, String createdId, LocalDateTime createdDatetime) {
        this.seq = seq;
        this.buyerCode = buyerCode;
        this.orderNo = orderNo;
        this.pointState = pointState;
        this.applyPoint = applyPoint;
        this.payAmount = payAmount;
        this.content = content;
        this.beforeSavePoint = beforeSavePoint;
        this.afterSavePoint = afterSavePoint;
        this.beforeSaveExpectPoint = beforeSaveExpectPoint;
        this.afterSaveExpectPoint = afterSaveExpectPoint;
        this.beforeUsePoint = beforeUsePoint;
        this.afterUsePoint = afterUsePoint;
        this.createdId = createdId;
        this.createdDatetime = createdDatetime;
    }
}
