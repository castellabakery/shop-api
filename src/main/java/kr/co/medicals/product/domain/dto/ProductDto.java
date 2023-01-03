package kr.co.medicals.product.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import kr.co.medicals.common.util.SessionUtil;
import kr.co.medicals.file.domain.dto.FileManagerDto;
import kr.co.medicals.file.domain.entity.FileManager;
import kr.co.medicals.product.domain.entity.BuyerTypeProductAmount;
import kr.co.medicals.product.domain.entity.Product;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDto {
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
    private List<FileManagerDto> fileList = new ArrayList<>();
    private List<BuyerTypeProductAmountDto> buyerTypeProductAmountList;

    private String searchKeyword;
    private String sortName;
    private String sortType;

    public ProductDto() {
    }

    @Builder(builderClassName = "byCreate", builderMethodName = "byCreate")
    public ProductDto(Long seq, int quantity, String productDetail, String standardCode, String insuranceCode, String specialDivision, String specialtyDivision, String medicalDivision, String insuranceDivision, String productName, String productDisplayName, String standard, String unit, String factory, String formulaDivision, String ingredientDivisionName, String ingredientName, String delYn, String useYn, String pointPayType, int point, String createdId, String modifiedId, LocalDateTime createdDatetime, LocalDateTime modifiedDatetime, List<FileManagerDto> fileList, List<BuyerTypeProductAmountDto> buyerTypeProductAmountList, String searchKeyword, String sortName, String sortType) {
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
        this.pointPayType = pointPayType;
        this.point = point;
        this.createdId = createdId;
        this.modifiedId = modifiedId;
        this.createdDatetime = createdDatetime;
        this.modifiedDatetime = modifiedDatetime;
        this.fileList = fileList;
        this.buyerTypeProductAmountList = buyerTypeProductAmountList;
        this.searchKeyword = searchKeyword;
        this.sortName = sortName;
        this.sortType = sortType;
    }

    @Builder(builderClassName = "selectByEntityWithFile", builderMethodName = "selectByEntityWithFile")
    public ProductDto(Product product, FileManager fileManager, BuyerTypeProductAmount buyerTypeProductAmount) {
        this.seq = product.getSeq();
        this.quantity = product.getQuantity();
        this.productDetail = product.getProductDetail();
        this.standardCode = product.getStandardCode();
        this.insuranceCode = product.getInsuranceCode();
        this.specialDivision = product.getSpecialDivision();
        this.specialtyDivision = product.getSpecialtyDivision();
        this.medicalDivision = product.getMedicalDivision();
        this.insuranceDivision = product.getInsuranceDivision();
        this.productName = product.getProductName();
        this.productDisplayName = product.getProductDisplayName();
        this.standard = product.getStandard();
        this.unit = product.getUnit();
        this.factory = product.getFactory();
        this.formulaDivision = product.getFormulaDivision();
        this.ingredientDivisionName = product.getIngredientDivisionName();
        this.ingredientName = product.getIngredientName();
        this.delYn = product.getDelYn();
        this.useYn = product.getUseYn();
        this.pointPayType = product.getPointPayType();
        this.point = product.getPoint();
        this.createdId = product.getCreatedId();
        this.modifiedId = product.getModifiedId();
        this.createdDatetime = product.getCreatedDatetime();
        this.modifiedDatetime = product.getModifiedDatetime();
        if (!ObjectUtils.isEmpty(fileManager)) {
            this.fileList = Arrays.asList(FileManagerDto.selectByEntity().fileManager(fileManager).build());
        } else {
            this.fileList = null;
        }
        if (!ObjectUtils.isEmpty(buyerTypeProductAmount)) {
            this.buyerTypeProductAmountList = Arrays.asList(BuyerTypeProductAmountDto.selectByEntity().buyerTypeProductAmount(buyerTypeProductAmount).build());
        } else {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "product amount");
        }

    }

    @Builder(builderClassName = "selectByEntity", builderMethodName = "selectByEntity")
    public ProductDto(Product product) {
        this.seq = product.getSeq();
        this.quantity = product.getQuantity();
        this.productDetail = product.getProductDetail();
        this.standardCode = product.getStandardCode();
        this.insuranceCode = product.getInsuranceCode();
        this.specialDivision = product.getSpecialDivision();
        this.specialtyDivision = product.getSpecialtyDivision();
        this.medicalDivision = product.getMedicalDivision();
        this.insuranceDivision = product.getInsuranceDivision();
        this.productName = product.getProductName();
        this.productDisplayName = product.getProductDisplayName();
        this.standard = product.getStandard();
        this.unit = product.getUnit();
        this.factory = product.getFactory();
        this.formulaDivision = product.getFormulaDivision();
        this.ingredientDivisionName = product.getIngredientDivisionName();
        this.ingredientName = product.getIngredientName();
        this.delYn = product.getDelYn();
        this.useYn = product.getUseYn();
        this.pointPayType = product.getPointPayType();
        this.point = product.getPoint();
        this.createdId = product.getCreatedId();
        this.modifiedId = product.getModifiedId();
        this.createdDatetime = product.getCreatedDatetime();
        this.modifiedDatetime = product.getModifiedDatetime();
    }

    public Product insertProduct() {
        return Product
                .builder()
                .quantity(this.quantity)
                .productDetail(this.productDetail)
                .standardCode(this.standardCode)
                .insuranceCode(this.insuranceCode)
                .specialDivision(this.specialDivision)
                .specialtyDivision(this.specialtyDivision)
                .medicalDivision(this.medicalDivision)
                .insuranceDivision(this.insuranceDivision)
                .productName(this.productName)
                .productDisplayName(this.productName + " " + this.standard)
                .standard(this.standard)
                .unit(this.unit)
                .factory(this.factory)
                .formulaDivision(this.formulaDivision)
                .ingredientDivisionName(this.ingredientDivisionName)
                .ingredientName(this.ingredientName)
                .delYn("N")
                .useYn(this.useYn)
                .pointPayType(this.pointPayType)
                .point(this.point)
                .createdId(SessionUtil.getUserCode())
                .createdDatetime(LocalDateTime.now())
                .build();
    }

    public Product updateProduct(ProductDto productDto) {
        return Product
                .builder()
                .seq(productDto.getSeq())
                .quantity(this.quantity)
                .productDetail(this.productDetail)
                .standardCode(this.standardCode)
                .insuranceCode(this.insuranceCode)
                .specialDivision(this.specialDivision)
                .specialtyDivision(this.specialtyDivision)
                .medicalDivision(this.medicalDivision)
                .insuranceDivision(this.insuranceDivision)
                .productName(this.productName)
                .productDisplayName(this.productName + " " + this.standard)
                .standard(this.standard)
                .unit(this.unit)
                .factory(this.factory)
                .formulaDivision(this.formulaDivision)
                .ingredientDivisionName(this.ingredientDivisionName)
                .ingredientName(this.ingredientName)
                .delYn(productDto.getDelYn())
                .useYn(this.useYn)
                .pointPayType(this.pointPayType)
                .point(this.point)
                .createdId(productDto.getCreatedId())
                .createdDatetime(productDto.getCreatedDatetime())
                .modifiedId(SessionUtil.getUserCode())
                .modifiedDatetime(LocalDateTime.now())
                .build();
    }

    public Product updateProductQuantity(int cnt) {
        return Product
                .builder()
                .seq(this.seq)
                .quantity(this.quantity + cnt)
                .productDetail(this.productDetail)
                .standardCode(this.standardCode)
                .insuranceCode(this.insuranceCode)
                .specialDivision(this.specialDivision)
                .specialtyDivision(this.specialtyDivision)
                .medicalDivision(this.medicalDivision)
                .insuranceDivision(this.insuranceDivision)
                .productName(this.productName)
                .productDisplayName(this.getProductDisplayName())
                .standard(this.standard)
                .unit(this.unit)
                .factory(this.factory)
                .formulaDivision(this.formulaDivision)
                .ingredientDivisionName(this.ingredientDivisionName)
                .ingredientName(this.ingredientName)
                .delYn(this.delYn)
                .useYn(this.useYn)
                .pointPayType(this.pointPayType)
                .point(this.point)
                .createdId(this.createdId)
                .createdDatetime(this.createdDatetime)
                .modifiedId(this.modifiedId)
                .modifiedDatetime(LocalDateTime.now())
                .build();
    }

}