package kr.co.medicals.cart;

import kr.co.medicals.buyer.BuyerService;
import kr.co.medicals.buyer.domain.dto.BuyerIdentificationDto;
import kr.co.medicals.cart.domain.dto.CartDto;
import kr.co.medicals.cart.domain.entity.Cart;
import kr.co.medicals.cart.domain.repository.CartRepository;
import kr.co.medicals.cart.domain.repository.CartRepositorySupport;
import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import kr.co.medicals.common.enums.FileTypeEnum;
import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ObjectMapperUtil;
import kr.co.medicals.common.util.SessionUtil;
import kr.co.medicals.file.AwsFileService;
import kr.co.medicals.file.domain.dto.FileManagerDto;
import kr.co.medicals.product.domain.dto.ProductDto;
import kr.co.medicals.product.domain.repository.ProductRepositorySupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartService {

    private final BuyerService buyerService;
    private final CartRepository cartRepository;
    private final AwsFileService awsFileService;
    private final CartRepositorySupport cartRepositorySupport;
    private final ProductRepositorySupport productRepositorySupport;

    @Autowired
    public CartService(BuyerService buyerService, CartRepository cartRepository, AwsFileService awsFileService, CartRepositorySupport cartRepositorySupport, ProductRepositorySupport productRepositorySupport) {
        this.buyerService = buyerService;
        this.cartRepository = cartRepository;
        this.awsFileService = awsFileService;
        this.cartRepositorySupport = cartRepositorySupport;
        this.productRepositorySupport = productRepositorySupport;
    }


    public Long addModifyCart(Map<String, Object> map) {

        if (ObjectUtils.isEmpty(map) || !map.containsKey("quantity") || !map.containsKey("productSeq")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "quantity, productSeq");
        }

        BuyerIdentificationDto buyerIdentificationDto = buyerService.getBuyerInfoByIdentificationCode(SessionUtil.getUserCode());
        if (ObjectUtils.isEmpty(buyerIdentificationDto)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "buyer identification");
        }

        Long productSeq = Long.valueOf(map.get("productSeq").toString());
        ProductDto getProductDto = productRepositorySupport.getProduct(productSeq, "Y");
        if (ObjectUtils.isEmpty(getProductDto)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "product");
        }

        String buyerCode = buyerIdentificationDto.getBuyer().getBuyerCode();
        String buyerType = buyerIdentificationDto.getBuyer().getBuyerType();

        List<CartDto> cartList = this.myCartListWithFile(buyerCode, buyerType, null);
        List<Long> productSeqList = cartList.stream().map(list -> list.getProduct().getSeq()).collect(Collectors.toList());
        int quantity = Integer.valueOf(map.get("quantity").toString());

        Long cartSeq = 0L;
        if (productSeqList.contains(productSeq)) { // 장바구니에 동일 상품이 있으면.
            for (CartDto cartDto : cartList) {
                if (Objects.equals(cartDto.getProduct().getSeq(), productSeq)) {
                    Cart cart = cartDto.updateQuantity(quantity);
                    cartSeq = cartRepository.save(cart).getSeq();
                    break;
                }
            }
        } else { // 장바구니에 동일 상품이 없으면.
            cartSeq = cartRepository.save(new CartDto().insertEntity(buyerCode, productSeq, quantity)).getSeq();
        }
        return cartSeq; // 결과 0이면 insert 실패
    }

    public Page<CartDto> myCartList() {

        BuyerIdentificationDto buyerIdentificationDto = buyerService.getBuyerInfoByIdentificationCode(SessionUtil.getUserCode());

        if (ObjectUtils.isEmpty(buyerIdentificationDto)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "buyer identification");
        }

        String buyerType = buyerIdentificationDto.getBuyer().getBuyerType();
        String buyerCode = buyerIdentificationDto.getBuyer().getBuyerCode();

        List<CartDto> cartDtoList = this.myCartListWithFile(buyerCode, buyerType, null);

        // front 에서 공통 컨포넌트 사용하기 때문에 이 내용이 있어야 한다고 함. 개선 되기 전에는 내용 삭제 금지.
        return new PageImpl<>(cartDtoList, Pageable.unpaged(), cartDtoList.size()); // 페이지 매김 없음 처리
    }

    public List<CartDto> myCartListWithFile(String buyerCode, String buyerType, List<Long> cartSeqList) {
        List<CartDto> cartDtoList = cartRepositorySupport.myCartList(buyerCode, buyerType, cartSeqList);

        List<CartDto> resultCart = new ArrayList<>();
        for (CartDto dto : cartDtoList) {
            Long productSeq = dto.getProduct().getSeq();
            List<FileManagerDto> fileManagerDtoList = awsFileService.getFileListByRelationSeqAndFileTypes(productSeq, FileTypeEnum.getProductMainFileType());
            if (!ObjectUtils.isEmpty(fileManagerDtoList)) {
                dto.setFileManagerDto(fileManagerDtoList);
            }
            resultCart.add(dto);
        }
        return resultCart;
    }

    public void deleteCart(ApiRequest apiRequest) {
        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("seq")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "seq");
        }

        Long cartSeq = Long.valueOf(map.get("seq").toString());

        BuyerIdentificationDto buyerIdentificationDto = buyerService.getBuyerInfoByIdentificationCode(SessionUtil.getUserCode());

        if (ObjectUtils.isEmpty(buyerIdentificationDto)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "buyer identification");
        }

        Cart cart = Optional.ofNullable(cartRepository.findBySeqAndDelYn(cartSeq, "N"))
                .orElseThrow(() -> new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "cart"));

        if (Objects.equals(cart.getBuyerCode(), buyerIdentificationDto.getBuyer().getBuyerCode())) {
            CartDto cartDto = CartDto.entityConvertDto().cart(cart).build();
            cartRepository.save(cartDto.updateDelY(SessionUtil.getUserCode()));
        } else {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "NOT_MATCH_BUYER_CODE", apiRequest);
        }

    }

}
