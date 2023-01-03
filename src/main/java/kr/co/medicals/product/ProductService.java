package kr.co.medicals.product;

import kr.co.medicals.buyer.BuyerService;
import kr.co.medicals.buyer.domain.dto.BuyerIdentificationDto;
import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import kr.co.medicals.common.constants.BuyerTypeConstants;
import kr.co.medicals.common.enums.FileTypeEnum;
import kr.co.medicals.common.enums.PointTypeEnum;
import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ObjectMapperUtil;
import kr.co.medicals.common.util.SessionUtil;
import kr.co.medicals.file.AwsFileService;
import kr.co.medicals.file.domain.dto.FileManagerDto;
import kr.co.medicals.product.domain.dto.BuyerTypeProductAmountDto;
import kr.co.medicals.product.domain.dto.ProductDto;
import kr.co.medicals.product.domain.entity.BuyerTypeProductAmount;
import kr.co.medicals.product.domain.repository.BuyerTypeProductAmountRepository;
import kr.co.medicals.product.domain.repository.BuyerTypeProductAmountRepositorySupport;
import kr.co.medicals.product.domain.repository.ProductRepository;
import kr.co.medicals.product.domain.repository.ProductRepositorySupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductRepositorySupport productRepositorySupport;
    private final BuyerTypeProductAmountRepository buyerTypeProductAmountRepository;
    private final BuyerTypeProductAmountRepositorySupport buyerTypeProductAmountRepositorySupport;
    private final AwsFileService awsFileService;
    private final BuyerService buyerService;

    @Autowired
    public ProductService(ProductRepository productRepository, ProductRepositorySupport productRepositorySupport, BuyerTypeProductAmountRepository buyerTypeProductAmountRepository, BuyerTypeProductAmountRepositorySupport buyerTypeProductAmountRepositorySupport, AwsFileService awsFileService, BuyerService buyerService) {
        this.productRepository = productRepository;
        this.productRepositorySupport = productRepositorySupport;
        this.buyerTypeProductAmountRepository = buyerTypeProductAmountRepository;
        this.buyerTypeProductAmountRepositorySupport = buyerTypeProductAmountRepositorySupport;
        this.awsFileService = awsFileService;
        this.buyerService = buyerService;
    }

    public Long addModifyProduct(ApiRequest apiRequest) {

        ProductDto productDto = ObjectMapperUtil.requestConvertDto(apiRequest, ProductDto.class);
        String pointPayType = productDto.getPointPayType();

        //null 인지 체크, null 이 아니면 리스트 3개인지 체크
        if (ObjectUtils.isEmpty(productDto.getBuyerTypeProductAmountList()) || productDto.getBuyerTypeProductAmountList().size() != 3) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "NOT_MATCH", "buyerTypeProductAmountList");
        }

        PointTypeEnum.isExist(pointPayType);

        List<BuyerTypeProductAmountDto> amountDtoList = productDto.getBuyerTypeProductAmountList();
        List<BuyerTypeProductAmountDto> amountDtoListReady;
        List<BuyerTypeProductAmount> amountList;

        int pointD = productDto.getPoint();

        // 기본 로직
        if (ObjectUtils.isEmpty(productDto.getSeq())) { // 신규 상품
            Long productSeq = productRepository.save(productDto.insertProduct()).getSeq();
            productDto.setSeq(productSeq);
            amountDtoListReady = this.createAmountList(amountDtoList, pointD, pointPayType, productSeq);
            amountList = amountDtoListReady.stream().map(list -> list.insertAmount()).collect(Collectors.toList());
        } else { // 기존 상품
            Long productSeq = productDto.getSeq();
            ProductDto getProductDto = productRepositorySupport.getProduct(productSeq, null);
            productRepository.save(productDto.updateProduct(getProductDto));
            List<BuyerTypeProductAmountDto> getAmountList = buyerTypeProductAmountRepositorySupport.getAmountList(productSeq, BuyerTypeConstants.NONE);

            List<BuyerTypeProductAmountDto> updateList = new ArrayList<>();
            for (BuyerTypeProductAmountDto target : getAmountList) {
                for (BuyerTypeProductAmountDto userInput : amountDtoList) {
                    if (Objects.equals(userInput.getBuyerType(), target.getBuyerType())) {
                        target.setAmount(userInput.getAmount());
                        updateList.add(target);
                    }
                }
            }

            amountDtoListReady = this.createAmountList(updateList, pointD, pointPayType, productSeq);
            amountList = amountDtoListReady.stream().map(list -> list.updateEntity()).collect(Collectors.toList());
        }

        buyerTypeProductAmountRepository.saveAll(amountList);
        return productDto.getSeq();
    }

    private List<BuyerTypeProductAmountDto> createAmountList(List<BuyerTypeProductAmountDto> targetList, int pointOrigin, String pointPayType, Long productSeq) {

        List<BuyerTypeProductAmountDto> amountDtoList = new ArrayList<>();
        for (BuyerTypeProductAmountDto targetDto : targetList) {
            int amountD = targetDto.getAmount();

            int point = 0;
            if (Objects.equals(pointPayType, PointTypeEnum.FLAT_RATE.getCode())) {

                BigDecimal bigDecimal = new BigDecimal(amountD * (pointOrigin / 100.0));
                point = bigDecimal.setScale(1, RoundingMode.FLOOR).intValue();

            } else if (Objects.equals(pointPayType, PointTypeEnum.FIXED_RATE.getCode())) {
                BigDecimal bigDecimal = new BigDecimal(pointOrigin);
                point = bigDecimal.setScale(1, RoundingMode.FLOOR).intValue();
            }

            targetDto.setAmount(targetDto.getAmount());
            targetDto.setOriginPoint(pointOrigin);
            targetDto.setSavePoint(point); // setScale(처리 할 소수점 위치, 처리방법)
            targetDto.setSavePointType(pointPayType); // 적립 타입
            targetDto.setProduct(ProductDto.byCreate().seq(productSeq).build());
            targetDto.setDelYn("N");
            amountDtoList.add(targetDto);
        }
        return amountDtoList;
    }

    public Page<ProductDto> getProductListAddRole(ApiRequest apiRequest) {
        ProductDto productDto = ObjectMapperUtil.requestConvertDto(apiRequest, ProductDto.class);
        if (ObjectUtils.isEmpty(productDto.getSortName()) || ObjectUtils.isEmpty(productDto.getSortType())) {
            productDto.setSortName("createdDatetime");
            productDto.setSortType("desc");
        }
        String buyerType = BuyerTypeConstants.MEDICAL;
        if (Objects.equals("buyer", SessionUtil.getUserType())) {
            BuyerIdentificationDto dto = buyerService.getBuyerInfoByIdentificationCode(SessionUtil.getUserCode());
            buyerType = dto.getBuyer().getBuyerType();
            productDto.setUseYn("Y");
        }
        return productRepositorySupport.getProductList(productDto, buyerType, productDto.getSortName(), productDto.getSortType(), PageRequest.of(apiRequest.getPage(), apiRequest.getPageSize()));
    }

    public ProductDto getProductByRole(ApiRequest apiRequest, String useYn) {

        String buyerType = BuyerTypeConstants.NONE;
        if (Objects.equals("buyer", SessionUtil.getUserType())) {
            BuyerIdentificationDto dto = buyerService.getBuyerInfoByIdentificationCode(SessionUtil.getUserCode());
            buyerType = dto.getBuyer().getBuyerType();
        }

        ProductDto productDto = ObjectMapperUtil.requestConvertDto(apiRequest, ProductDto.class);
        ProductDto getProductDto = productRepositorySupport.getProduct(productDto.getSeq(), useYn);

        if (ObjectUtils.isEmpty(getProductDto)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "product");
        }

        List<FileManagerDto> fileManagerDtoList = awsFileService.getFileListByRelationSeqAndFileTypes(getProductDto.getSeq(), FileTypeEnum.getProductAllFileType());
        if (!ObjectUtils.isEmpty(fileManagerDtoList)) {
            getProductDto.setFileList(fileManagerDtoList);
        }

        List<BuyerTypeProductAmountDto> amountDtoList = buyerTypeProductAmountRepositorySupport.getAmountList(getProductDto.getSeq(), buyerType);
        getProductDto.setBuyerTypeProductAmountList(amountDtoList);

        return getProductDto;
    }

    public void quantityManager(Long productSeq, int cnt) {
        ProductDto productDto = productRepositorySupport.getProduct(productSeq, null);
        productRepository.save(productDto.updateProductQuantity(cnt));
    }

}
