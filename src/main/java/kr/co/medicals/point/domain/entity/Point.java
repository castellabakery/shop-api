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
@Table(name = "POINT")
public class Point {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;
    private String buyerCode;
    private int savePoint;
    private int saveExpectPoint;
    private int usePoint;
    private String createdId;
    private LocalDateTime createdDatetime;
    private String modifiedId;
    private LocalDateTime modifiedDatetime;

    @Builder
    public Point(Long seq, String buyerCode, int savePoint, int saveExpectPoint, int usePoint, String createdId, LocalDateTime createdDatetime, String modifiedId, LocalDateTime modifiedDatetime) {
        this.seq = seq;
        this.buyerCode = buyerCode;
        this.savePoint = savePoint;
        this.saveExpectPoint = saveExpectPoint;
        this.usePoint = usePoint;
        this.createdId = createdId;
        this.createdDatetime = createdDatetime;
        this.modifiedId = modifiedId;
        this.modifiedDatetime = modifiedDatetime;
    }
}
