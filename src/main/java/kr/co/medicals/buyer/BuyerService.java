package kr.co.medicals.buyer;

import kr.co.medicals.buyer.domain.dto.BuyerIdentificationDto;
import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import kr.co.medicals.common.constants.IdentificationTypeConstants;
import kr.co.medicals.common.constants.PathConstants;
import kr.co.medicals.common.enums.FileTypeEnum;
import kr.co.medicals.common.excel.EnumExcelBuyerColumn;
import kr.co.medicals.common.excel.ExcelConstants;
import kr.co.medicals.common.excel.ExcelMakeUtil;
import kr.co.medicals.common.util.*;
import kr.co.medicals.file.AwsFileService;
import kr.co.medicals.file.domain.dto.FileManagerDto;
import kr.co.medicals.terms.BuyerTermsService;
import kr.co.medicals.terms.domain.dto.BuyerTermsDto;
import kr.co.medicals.common.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.util.*;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class BuyerService {

    private final WebClientUtils webClientUtils;
    private final AwsFileService awsFileService;
    private final BuyerTermsService buyerTermsService;

    @Autowired
    public BuyerService(WebClientUtils webClientUtils, AwsFileService awsFileService, BuyerTermsService buyerTermsService) {
        this.webClientUtils = webClientUtils;
        this.awsFileService = awsFileService;
        this.buyerTermsService = buyerTermsService;
    }

    // 회원 메인 리스트 조회.
    // 회원 서브 리스트 조회.
    public ApiResponse getBuyerList(ApiRequest apiRequest, String identificationType) {

        BuyerIdentificationDto paramDto = ObjectMapperUtil.requestConvertDto(apiRequest, BuyerIdentificationDto.class);

        if (ObjectUtils.isEmpty(apiRequest.getPage()) || ObjectUtils.isEmpty(apiRequest.getPageSize())) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "paging");
        }

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.BUYER_SEARCH)
                .queryParam("identificationType", identificationType)
                .queryParam("page", apiRequest.getPage())
                .queryParam("pageSize", apiRequest.getPageSize())
                .encode();

        if (Objects.equals("buyer", SessionUtil.getUserType())) {
            // 회원 정보 조회.
            BuyerIdentificationDto getLoginInfo = this.getBuyerInfoByIdentificationCode(SessionUtil.getUserCode());
            // 서브계정은 리스트 조회 할 수 없음.
            if (Objects.equals(IdentificationTypeConstants.SUB, getLoginInfo.getIdentificationType())) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.AUTHENTICATION_FAILED, "AUTHENTICATION_FAILED", "부계정은 리스트 조회 권한 없음.");
            }
            uriComponentsBuilder.queryParam("buyerSeq", getLoginInfo.getBuyer().getSeq());
        } else {
            if (Objects.equals(IdentificationTypeConstants.MAIN, identificationType)) {
                uriComponentsBuilder
                        .queryParam("buyerType", paramDto.getBuyer().getBuyerType())
                        .queryParam("buyerIdentificationId", paramDto.getBuyerIdentificationId())
                        .queryParam("corpName", paramDto.getBuyer().getCorpName())
                        .queryParam("staffName", paramDto.getStaffName())
                        .queryParam("staffEmail", paramDto.getStaffEmail())
                        .queryParam("approveDatetime", paramDto.getStaffEmail())
                        .queryParam("buyerState", paramDto.getBuyer().getBuyerState());

                if (!ObjectUtils.isEmpty(paramDto.getSearchKeyword())) {
                    uriComponentsBuilder.queryParam("searchKeyword", paramDto.getSearchKeyword());
                }

            } else {
                if (ObjectUtils.isEmpty(paramDto) || ObjectUtils.isEmpty(paramDto.getBuyer().getSeq())) {
                    throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "buyer-Seq");
                }
                uriComponentsBuilder.queryParam("buyerSeq", paramDto.getBuyer().getSeq());
            }
        }

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.GET, uriComponentsBuilder.build().toUri());
        return response.block().getBody();
    }

    // 회원 메인 상세 조회.
    // 회원 서브 상세 조회.
    public ApiResponse getBuyerIdentificationInfo(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        BuyerIdentificationDto resultDto;

        if (Objects.equals("admin", SessionUtil.getUserType())) {
            if (map.containsKey("buyerIdentificationSeq")) {
                Long buyerIdentificationSeq = ObjectCheck.isBlankLongException(map.get("buyerIdentificationSeq"), "BUYER_IDENTIFICATION_SEQ");
                resultDto = this.getBuyerInfoByBuyerIdentificationSeq(buyerIdentificationSeq);
            } else if (map.containsKey("buyerIdentificationCode")) {
                String buyerIdentificationCode = map.get("buyerIdentificationCode").toString();
                resultDto = this.getBuyerInfoByIdentificationCode(buyerIdentificationCode);
            } else {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "buyerIdentificationSeq, buyerIdentificationCode");
            }
        } else {
            resultDto = this.getBuyerInfoByIdentificationCode(SessionUtil.getUserCode());
        }

        // Main 정보 리턴시 회원파일 + 약관 내용 같이 리턴.
        if (Objects.equals(IdentificationTypeConstants.MAIN, resultDto.getIdentificationType())) {
            return ApiResponse.success(this.addOptionInfo(resultDto));
        }

        return ApiResponse.success(resultDto);

    }

    private BuyerIdentificationDto addOptionInfo(BuyerIdentificationDto dto) {
        // 등록 된 회원 파일 조회
        Long buyerSeq = dto.getBuyer().getSeq();
        List<FileManagerDto> fileManagerDtoList = awsFileService.getFileListByRelationSeqAndFileTypes(buyerSeq, FileTypeEnum.getBuyerFileType());
        if (!ObjectUtils.isEmpty(fileManagerDtoList)) {
            dto.setFileList(fileManagerDtoList);
        }

        // 등록 된 약관 조회
        List<BuyerTermsDto> buyerTermsDtoList = buyerTermsService.getBuyerTermsList(dto.getBuyer().getBuyerCode(), null);
        dto.getBuyer().setBuyerTermsList(buyerTermsDtoList);
        return dto;
    }

    public BuyerIdentificationDto getBuyerInfoByBuyerIdentificationSeq(Long identificationSeq) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.BUYER)
                .queryParam("buyerIdentificationSeq", identificationSeq)
                .encode();

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.GET, uriComponentsBuilder.build().toUri());
        return ObjectMapperUtil.responseConvertDto(response.block().getBody(), BuyerIdentificationDto.class);
    }

    public BuyerIdentificationDto getBuyerInfoByIdentificationCode(String buyerIdentificationCode) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.BUYER)
                .queryParam("buyerIdentificationCode", buyerIdentificationCode)
                .encode();
        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.GET, uriComponentsBuilder.build().toUri());
        return ObjectMapperUtil.responseConvertDto(response.block().getBody(), BuyerIdentificationDto.class);
    }

    public BuyerIdentificationDto getBuyerInfoByIdentificationId(String buyerIdentificationId) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.BUYER)
                .queryParam("buyerIdentificationId", buyerIdentificationId)
                .encode();

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.GET, uriComponentsBuilder.build().toUri());
        return ObjectMapperUtil.responseConvertDto(response.block().getBody(), BuyerIdentificationDto.class);
    }

    /**
     * SessionUtil.getPreUserCode() 로 대체 될 예정이니 변경 부탁드립니다.
     */
    @Deprecated
    public String getBuyerCode() {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.BUYER)
                .queryParam("buyerIdentificationCode", SessionUtil.getUserCode())
                .encode();

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.GET, uriComponentsBuilder.build().toUri());
        BuyerIdentificationDto buyerIdentificationDto = ObjectMapperUtil.responseConvertDto(response.block().getBody(), BuyerIdentificationDto.class);
        return buyerIdentificationDto.getBuyer().getBuyerCode();
    }

    public String getBuyerCodeByBuyerSeq(Long buyerSeq) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.BUYER)
                .queryParam("buyerSeq", buyerSeq)
                .encode();

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.GET, uriComponentsBuilder.build().toUri());
        BuyerIdentificationDto buyerIdentificationDto = ObjectMapperUtil.responseConvertDto(response.block().getBody(), BuyerIdentificationDto.class);
        return buyerIdentificationDto.getBuyer().getBuyerCode();
    }

    public String getBuyerCodeById(String id) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.BUYER)
                .queryParam("buyerIdentificationId", id)
                .encode();

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.GET, uriComponentsBuilder.build().toUri());
        BuyerIdentificationDto buyerIdentificationDto = ObjectMapperUtil.responseConvertDto(response.block().getBody(), BuyerIdentificationDto.class);
        return buyerIdentificationDto.getBuyer().getBuyerCode();
    }

    // 회원 상태 변경.
    public ApiResponse modifyStateBuyer(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("changeState") || !map.containsKey("buyerSeq")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "changeState, buyerSeq");
        }

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.BUYER_CHANGE_STATE)
                .queryParam("changeState", map.get("changeState"))
                .queryParam("buyerSeq", map.get("buyerSeq"))
                .encode();

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.PATCH, uriComponentsBuilder.build().toUri());
        return response.block().getBody();
    }

    // 부계정 등록
    public ApiResponse addSubBuyer(ApiRequest apiRequest) {
        BuyerIdentificationDto paramDto = ObjectMapperUtil.requestConvertDto(apiRequest, BuyerIdentificationDto.class);

        BuyerIdentificationDto getBuyerInfo = this.getBuyerInfoByIdentificationCode(SessionUtil.getUserCode());
        if (Objects.equals(IdentificationTypeConstants.SUB, getBuyerInfo.getIdentificationType())) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.AUTHENTICATION_FAILED, "AUTHENTICATION_FAILED", "주계정 권한 없음.");
        }
        paramDto.getBuyer().setBuyerCode(getBuyerInfo.getBuyer().getBuyerCode());

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.POST, PathConstants.BUYER_SUB, paramDto.encryptPassword());
        return response.block().getBody();
    }

    // 부계정 수정
    public ApiResponse modifySubBuyer(ApiRequest apiRequest) {

        BuyerIdentificationDto paramDto = ObjectMapperUtil.requestConvertDto(apiRequest, BuyerIdentificationDto.class);
        BuyerIdentificationDto getDto = this.getBuyerInfoByIdentificationCode(SessionUtil.getUserCode());

        if (Objects.equals(IdentificationTypeConstants.SUB, getDto.getIdentificationType())) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.AUTHENTICATION_FAILED, "AUTHENTICATION_FAILED", "부계정 수정은 주계정만 가능.");
        }

        paramDto.getBuyer().setSeq(getDto.getBuyer().getSeq());

        if (ObjectUtils.isEmpty(paramDto.getSeq())) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "seq");
        }

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.PATCH, PathConstants.BUYER_SUB, paramDto);
        return response.block().getBody();
    }

    // 회원 코드 존재 여부.
    public ApiResponse existsBuyerCode(ApiRequest apiRequest) {
        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("buyerCode")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "buyerCode");
        }

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.EXISTS_BUYER_CODE)
                .queryParam("buyerCode", map.get("buyerCode"))
                .encode();

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.GET, uriComponentsBuilder.build().toUri());
        return response.block().getBody();
    }

    // 회원 아이디 존재 여부.
    public ApiResponse existsBuyerIdentificationId(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("buyerIdentificationId")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "buyerIdentificationId");
        }

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.EXISTS_BUYER_IDENTIFICATION_ID)
                .queryParam("buyerIdentificationId", map.get("buyerIdentificationId"))
                .encode();

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.GET, uriComponentsBuilder.build().toUri());
        return response.block().getBody();
    }

    // 회원 이메일 존재 여부.
    public ApiResponse existsBuyerEmail(ApiRequest apiRequest) {
        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("email")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "email");
        }

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.EXISTS_EMAIL)
                .queryParam("email", map.get("email"))
                .encode();

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.GET, uriComponentsBuilder.build().toUri());
        return response.block().getBody();
    }


    public ApiResponse modifyPassword(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("oldPassword") || !map.containsKey("newPassword")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY");
        }

        String oldPassword = map.get("oldPassword").toString();
        String newPassword = map.get("newPassword").toString();

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.BUYER_CHANGE_PASSWORD)
                .queryParam("buyerIdentificationCode", SessionUtil.getUserCode())
                .queryParam("oldPassword", Encrypt.sha256(oldPassword))
                .queryParam("newPassword", Encrypt.sha256(newPassword))
                .encode();

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.PATCH, uriComponentsBuilder.build().toUri());
        return response.block().getBody();

    }

    public ApiResponse initPassword(ApiRequest apiRequest) {
        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("buyerIdentificationId")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "buyerIdentificationId");
        }

        String buyerIdentificationId = map.get("buyerIdentificationId").toString();

        BuyerIdentificationDto editDto = this.getBuyerInfoByIdentificationId(buyerIdentificationId);

        if (Objects.equals("buyer", SessionUtil.getUserType())) {
            BuyerIdentificationDto requestDto = this.getBuyerInfoByIdentificationCode(SessionUtil.getUserCode());
            // 회원일때는 바이어 정보가 같은지 확인
            if (!Objects.equals(requestDto.getBuyer().getBuyerCode(), editDto.getBuyer().getBuyerCode())) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.AUTHENTICATION_FAILED, "AUTHENTICATION_FAILED", "로그인 된 계정과 수정하려는 계정 정보가 같은 BUYER_CODE 를 가지고 있지 않아 수정할 수 없습니다.");
            }
            // sub가 sub를 수정하려는 내용인지 확인. - 본인계정만 수정가능.
            if (Objects.equals(IdentificationTypeConstants.SUB, requestDto.getIdentificationType())) {
                if (!Objects.equals(requestDto.getBuyerIdentificationId(), editDto.getBuyerIdentificationId())) {
                    throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.AUTHENTICATION_FAILED, "AUTHENTICATION_FAILED", "서브계정은 본인 계정만 수정이 가능합니다.");
                }
            }
        }

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.BUYER_INIT_PASSWORD)
                .queryParam("buyerIdentificationId", buyerIdentificationId)
                .queryParam("password", Encrypt.sha256(buyerIdentificationId))
                .queryParam("userCode", SessionUtil.getUserCode())
                .encode();

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.PATCH, uriComponentsBuilder.build().toUri());
        return response.block().getBody();
    }

    // =====================================================================================================

    public Object getBuyerInfoForExcelDownload(Pageable pageable, BuyerListRequest buyerListRequest) throws Exception {
//        List<BuyerIdentification> list = getBuyerList(buyerIdentificationDto);
        List<BuyerIdentificationDto> list = new ArrayList();
        if (ObjectUtils.isEmpty(list))
            throw new Exception("Excel Download Exception !");
        try {
            List<Map<String, Object>> prevContent = new ArrayList<>();
            List<?> tmp = list;
            tmp.forEach(item -> {
                Map<String, Object> temp = ConverObjectToMap(item);
                prevContent.add(temp);
            });
            List<Map<String, Object>> nextContent = new ArrayList<>();
            Map<String, String[]> heads;
            nextContent = this.setContents(prevContent, nextContent);
            heads = this.setHeads(nextContent);
            return ExcelMakeUtil.makeSalesExcel(nextContent, heads.get("keys"), heads.get("width"));
        } catch (Exception t) {
            t.printStackTrace();
            throw new Exception("Excel Download Exception !");
        }
    }

    public BuyerIdentificationDto getBuyerIdentificationByBuyerCode(String buyerCode) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.BUYER)
                .queryParam("buyerCode", buyerCode)
                .queryParam("identificationType", "M")
                .encode();

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.GET, uriComponentsBuilder.build().toUri());
        return ObjectMapperUtil.responseConvertDto(response.block().getBody(), BuyerIdentificationDto.class);
    }


    private Map ConverObjectToMap(Object obj) {
        try {
            Field[] fields = obj.getClass().getDeclaredFields();
            Map resultMap = new HashMap();
            for (int i = 0; i <= fields.length - 1; i++) {
                fields[i].setAccessible(true);
                resultMap.put(fields[i].getName(), fields[i].get(obj));
            }
            return resultMap;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Map<String, Object>> setContents(List<Map<String, Object>> prevContent, List<Map<String, Object>> nextContent) {
        int indexNo = 1;
        for (Map<String, Object> content : prevContent) {
            Map<String, Object> nextContentMap = new HashMap<>();
            List<String> keyList = new ArrayList<>();
            for (int i = 0; i < content.keySet().size(); i++) {
                String code = EnumExcelBuyerColumn.getCodeBySeq(i); // 0
                keyList.add(code);
            }
            Iterator<String> listIterator = keyList.iterator();
            while (listIterator.hasNext()) {
                String code = listIterator.next();
                String colName = EnumExcelBuyerColumn.getNameByCode(code); // 1
                if (!Objects.equals(ExcelConstants.notFound, colName)) {
                    Object realValue = this.selectColumnName(content, code); // 2_2
                    nextContentMap.put(colName, realValue);
                }
            }
            nextContent.add(nextContentMap);
            indexNo++;
        }
        return nextContent;
    }

    private Map<String, String[]> setHeads(List<Map<String, Object>> nextContent) {
        Map<?, ?> headCellKeys = nextContent.get(0);
        String[] keys = new String[headCellKeys.keySet().size()];
        String[] width = new String[headCellKeys.keySet().size()];
        Map<?, ?> cellKeys = nextContent.get(0);
        Iterator<?> iterator = cellKeys.keySet().iterator();
        while (iterator.hasNext()) {
            String key = (String) iterator.next();
            int sequence = EnumExcelBuyerColumn.getSequenceByName(key);
            keys[sequence] = key;
            width[sequence] = EnumExcelBuyerColumn.getWidthBySequence(sequence);
        }
        Map<String, String[]> result = new HashMap<>();
        result.put("keys", keys);
        result.put("width", width);
        return result;
    }

    private Object selectColumnName(Map<String, Object> content, String code) {
        Object value = content.get(code);
        return value;
    }

}
