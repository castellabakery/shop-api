package kr.co.medicals.product.domain.entity;

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
@Table(name = "PRODUCT")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seq;
    private int quantity;
    private String productDetail;
    private String standardCode;
    private String insuranceCode;
    private String specialDivision;
    private String specialtyDivision;
    private String medicalDivision;
    private String insuranceDivision;
    private String productName;
    private String productDisplayName;
    private String standard;
    private String unit;
    private String factory;
    private String formulaDivision;
    private String ingredientDivisionName;
    private String ingredientName;
    private String delYn;
    private String useYn;
    private String pointPayType;
    private int point;
    private String createdId;
    private String modifiedId;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifiedDatetime;

    @Builder
    public Product(Long seq, int quantity, String productDetail, String standardCode, String insuranceCode, String specialDivision, String specialtyDivision, String medicalDivision, String insuranceDivision, String productName, String productDisplayName, String standard, String unit, String factory, String formulaDivision, String ingredientDivisionName, String ingredientName, String delYn, String useYn, String pointPayType, int point, String createdId, String modifiedId, LocalDateTime createdDatetime, LocalDateTime modifiedDatetime) {
        this.seq = seq;
        this.quantity = quantity;
        this.productDetail = productDetail;
        this.standardCode = standardCode;
        this.insuranceCode = insuranceCode;
        this.specialDivision = specialDivision;
        this.specialtyDivision = specialtyDivision;
        this.medicalDivision = medicalDivision;
        this.insuranceDivision = insuranceDivision;
        this.productName = productName;
        this.productDisplayName = productDisplayName;
        this.standard = standard;
        this.unit = unit;
        this.factory = factory;
        this.formulaDivision = formulaDivision;
        this.ingredientDivisionName = ingredientDivisionName;
        this.ingredientName = ingredientName;
        this.delYn = delYn;
        this.useYn = useYn;
        this.point = point;
        this.pointPayType = pointPayType;
        this.createdId = createdId;
        this.modifiedId = modifiedId;
        this.createdDatetime = createdDatetime;
        this.modifiedDatetime = modifiedDatetime;
    }
}