package kr.co.medicals.product.domain.repository;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.co.medicals.common.enums.FileTypeEnum;
import kr.co.medicals.file.domain.entity.QFileManager;
import kr.co.medicals.product.domain.dto.ProductDto;
import kr.co.medicals.product.domain.entity.QBuyerTypeProductAmount;
import kr.co.medicals.product.domain.entity.QProduct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Repository
public class ProductRepositorySupport {

    private final JPAQueryFactory jpaQueryFactory;

    @Autowired
    public ProductRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        this.jpaQueryFactory = jpaQueryFactory;
    }

    // 검색조건 = 설정 안되어 있음. 파일 리스트 같이 불러옴.
    // ,
    public Page<ProductDto> getProductList(ProductDto productDto, String buyerType, String sortName, String sortType, Pageable pageable) {

        QProduct qProduct = QProduct.product;
        QFileManager qFileManager = QFileManager.fileManager;
        QBuyerTypeProductAmount qBuyerTypeProductAmount = QBuyerTypeProductAmount.buyerTypeProductAmount;

        List<OrderSpecifier> ORDERS = createdSort(sortName,sortType);

        List<ProductDto> productDtoList = jpaQueryFactory.select(Projections.constructor(ProductDto.class, qProduct, qFileManager, qBuyerTypeProductAmount))
                .from(qProduct)
                .leftJoin(qFileManager)
                    .on(qFileManager.delYn.eq("N")
                        .and(qFileManager.relationSeq.eq(qProduct.seq)
                            .and(qFileManager.fileType.eq(FileTypeEnum.PRODUCT_IMG_MAIN.getCode()))))
                .leftJoin(qBuyerTypeProductAmount)
                    .on(qProduct.seq.eq(qBuyerTypeProductAmount.product.seq)
                            .and(qBuyerTypeProductAmount.buyerType.eq(buyerType)))
                .where(
                        containsStandardCode(productDto.getStandardCode()),
                        containsProductDisplayName(productDto.getProductName()),
                        containsFactory(productDto.getFactory()),
                        containsSearchKeyword(productDto.getSearchKeyword()),
                        eqFormulaDivision(productDto.getFormulaDivision()),
                        eqSpecialtyDivision(productDto.getSpecialtyDivision()),
                        eqUseYn(productDto.getUseYn())
                )
                .orderBy(createdSort(sortName, sortType).stream().toArray(OrderSpecifier[]::new))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = jpaQueryFactory
                .select(qProduct.count())
                .from(qProduct)
                .where(
                        containsStandardCode(productDto.getStandardCode()),
                        containsProductDisplayName(productDto.getProductName()),
                        containsFactory(productDto.getFactory()),
                        containsSearchKeyword(productDto.getSearchKeyword()),
                        eqFormulaDivision(productDto.getFormulaDivision()),
                        eqSpecialtyDivision(productDto.getSpecialtyDivision()),
                        eqUseYn(productDto.getUseYn())
                )
                .fetchOne();

        return new PageImpl<>(productDtoList, pageable, count);
    }

    public List<OrderSpecifier> createdSort(String sortName, String sortType){

        List<OrderSpecifier> sortBy = new ArrayList<>();

        Order direction = sortType.equals("desc") ? Order.DESC : Order.ASC;
        switch (sortName){
            case "amount" :
                sortBy.add(new OrderSpecifier(direction, QBuyerTypeProductAmount.buyerTypeProductAmount.amount));
            case "createdDatetime" :
                sortBy.add(new OrderSpecifier(direction, QProduct.product.createdDatetime));
            default:
                sortBy.add(new OrderSpecifier(direction, QProduct.product.createdDatetime));
                sortBy.add(new OrderSpecifier(direction, QProduct.product.modifiedDatetime));
        }
        return sortBy;
    }

    public ProductDto getProduct(Long productSeq, String useYn) {
        QProduct qProduct = QProduct.product;

        return jpaQueryFactory.select(Projections.constructor(ProductDto.class, qProduct))
                .from(qProduct)
                .where(
                        qProduct.seq.eq(productSeq),
                        eqUseYn(useYn)
                )
                .fetchOne();
    }

    public BooleanExpression containsStandardCode(String StandardCode){
        if(ObjectUtils.isEmpty(StandardCode)){
            return null;
        }
        return QProduct.product.standardCode.contains(StandardCode);
    }
    public BooleanExpression containsProductDisplayName(String productDisplayName){
        if(ObjectUtils.isEmpty(productDisplayName)){
            return null;
        }
        return QProduct.product.productDisplayName.contains(productDisplayName);
    }
    public BooleanExpression containsFactory(String factory){
        if(ObjectUtils.isEmpty(factory)){
            return null;
        }
        return QProduct.product.factory.contains(factory);
    }
    public BooleanExpression eqFormulaDivision(String formulaDivision){
        if(ObjectUtils.isEmpty(formulaDivision)){
            return null;
        }
        return QProduct.product.formulaDivision.eq(formulaDivision);
    }
    public BooleanExpression eqSpecialtyDivision(String specialtyDivision){
        if(ObjectUtils.isEmpty(specialtyDivision)){
            return null;
        }
        return QProduct.product.specialtyDivision.eq(specialtyDivision);
    }
    public BooleanExpression containsSearchKeyword(String searchKeyword){
        if(ObjectUtils.isEmpty(searchKeyword)){
            return null;
        }
        return QProduct.product.standardCode.contains(searchKeyword)
                        .or(QProduct.product.productName.contains(searchKeyword)
                                .or(QProduct.product.factory.contains(searchKeyword)));
    }
    public BooleanExpression eqUseYn(String useYn){
        // 파일 삭제하는 기능은 없지만 UseYn 작업하면서 추가해둠
        if(ObjectUtils.isEmpty(useYn)){
            return null;
        }
        return QProduct.product.useYn.eq(useYn)
                .and(QProduct.product.delYn.eq("N"));
    }


}
