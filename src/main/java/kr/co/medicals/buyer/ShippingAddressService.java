package kr.co.medicals.buyer;

import kr.co.medicals.buyer.domain.dto.ShippingAddressDto;
import kr.co.medicals.buyer.domain.repository.ShippingAddressRepository;
import kr.co.medicals.buyer.domain.repository.ShippingAddressRepositorySupport;
import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ObjectCheck;
import kr.co.medicals.common.util.ObjectMapperUtil;
import kr.co.medicals.common.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class ShippingAddressService {

    private final ShippingAddressRepositorySupport shippingAddressRepositorySupport;
    private final ShippingAddressRepository shippingAddressRepository;
    private final BuyerService buyerService;

    @Autowired
    public ShippingAddressService(ShippingAddressRepositorySupport shippingAddressRepositorySupport, ShippingAddressRepository shippingAddressRepository, BuyerService buyerService) {
        this.shippingAddressRepositorySupport = shippingAddressRepositorySupport;
        this.shippingAddressRepository = shippingAddressRepository;
        this.buyerService = buyerService;
    }

    public List<ShippingAddressDto> getShippingAddress() {
        return shippingAddressRepositorySupport.findShippingAddress(SessionUtil.getPreUserCode());
    }

    public void addShippingAddress(ApiRequest apiRequest) {
        String buyerCode = SessionUtil.getPreUserCode();
        ShippingAddressDto paramDto = ObjectMapperUtil.requestConvertDto(apiRequest, ShippingAddressDto.class);
        int cnt = shippingAddressRepositorySupport.countShippingAddress(buyerCode).intValue();
        if (cnt >= 3) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "배송지는 3개까지 등록 가능합니다.", cnt);
        }
        ShippingAddressDto dto = shippingAddressRepositorySupport.lastSeqShippingAddress(buyerCode);
        int seq = 0;
        if (!ObjectUtils.isEmpty(dto)) {
            seq = dto.getAddressSeq();
        }
        shippingAddressRepository.save(paramDto.insertEntity(buyerCode, seq));
    }

    public void modifyShippingAddress(ApiRequest apiRequest) {
        ShippingAddressDto paramDto = ObjectMapperUtil.requestConvertDto(apiRequest, ShippingAddressDto.class);
        ShippingAddressDto oldDto = shippingAddressRepositorySupport.findShippingAddressOne(paramDto.getSeq(), SessionUtil.getPreUserCode());
        if (ObjectUtils.isEmpty(oldDto)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "shipping address");
        }
        shippingAddressRepository.save(paramDto.updateEntity(oldDto));
    }

    public void deleteShippingAddress(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("shippingAddressSeq")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "shippingAddressSeq");
        }

        Long shippingAddressSeq = ObjectCheck.isBlankLongException(map.get("shippingAddressSeq"), "shippingAddressSeq");
        ShippingAddressDto getDto = shippingAddressRepositorySupport.findShippingAddressOne(shippingAddressSeq, SessionUtil.getPreUserCode());

        if (ObjectUtils.isEmpty(getDto)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "shipping address");
        }

        shippingAddressRepository.save(getDto.updateDelYEntity());

    }

}
