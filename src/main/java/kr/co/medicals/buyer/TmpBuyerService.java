package kr.co.medicals.buyer;

import kr.co.medicals.api.aws.email.service.EmailService;
import kr.co.medicals.buyer.domain.dto.BuyerIdentificationDto;
import kr.co.medicals.buyer.domain.dto.TmpBuyerDto;
import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import kr.co.medicals.common.constants.BuyerStateConstants;
import kr.co.medicals.common.constants.PathConstants;
import kr.co.medicals.common.enums.FileTypeEnum;
import kr.co.medicals.common.util.*;
import kr.co.medicals.file.AwsFileService;
import kr.co.medicals.file.domain.dto.FileManagerDto;
import kr.co.medicals.point.PointService;
import kr.co.medicals.point.domain.dto.PointDto;
import kr.co.medicals.terms.TermsService;
import kr.co.medicals.terms.TmpBuyerTermsService;
import kr.co.medicals.terms.domain.dto.TermsDto;
import kr.co.medicals.terms.domain.dto.TmpBuyerTermsDto;
import kr.co.medicals.common.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class TmpBuyerService {
    private final WebClientUtils webClientUtils;
    private final AwsFileService awsFileService;
    private final PointService pointService;
    private final TmpBuyerTermsService tmpBuyerTermsService;
    private final TermsService termsService;
    private final EmailService emailService;

    @Autowired
    public TmpBuyerService(WebClientUtils webClientUtils, AwsFileService awsFileService, PointService pointService, TmpBuyerTermsService tmpBuyerTermsService, TermsService termsService, EmailService emailService) {
        this.webClientUtils = webClientUtils;
        this.awsFileService = awsFileService;
        this.pointService = pointService;
        this.tmpBuyerTermsService = tmpBuyerTermsService;
        this.termsService = termsService;
        this.emailService = emailService;
    }

    public ApiResponse adminGetTmpBuyerList(ApiRequest apiRequest) {
        TmpBuyerDto tmpBuyerDto = ObjectMapperUtil.requestConvertDto(apiRequest, TmpBuyerDto.class);
        return this.getTmpBuyerList(tmpBuyerDto, apiRequest.getPage(), apiRequest.getPageSize());
    }

    public ApiResponse buyerGetTmpBuyerList(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("buyerSeq")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "buyerSeq");
        }

        Long buyerSeq = Long.valueOf(map.get("buyerSeq").toString());
        TmpBuyerDto tmpBuyerDto = new TmpBuyerDto();
        tmpBuyerDto.setBuyerSeq(buyerSeq);
        return this.getTmpBuyerList(tmpBuyerDto, apiRequest.getPage(), apiRequest.getPageSize());
    }

    // 임시 회원 리스트 조회
    public ApiResponse getTmpBuyerList(TmpBuyerDto tmpBuyerDto, int page, int pageSize) {

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.TMP_BUYER_SEARCH)
                .queryParam("page", page)
                .queryParam("pageSize", pageSize)
                .encode();

        if (!ObjectUtils.isEmpty(tmpBuyerDto)) {
            uriComponentsBuilder.queryParam("buyerType", tmpBuyerDto.getBuyerType())
                    .queryParam("buyerIdentificationId", tmpBuyerDto.getBuyerIdentificationId())
                    .queryParam("corpName", tmpBuyerDto.getCorpName())
                    .queryParam("buyerState", tmpBuyerDto.getState())
                    .queryParam("searchKeyword", tmpBuyerDto.getSearchKeyword())
                    .queryParam("buyerSeq", tmpBuyerDto.getBuyerSeq());
        }

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.GET, uriComponentsBuilder.build().toUri());

        return response.block().getBody();
    }

    // 임시 회원 상세 조회
    public ApiResponse getTmpBuyerInfo(ApiRequest apiRequest) {

        TmpBuyerDto tmpBuyerDto = ObjectMapperUtil.requestConvertDto(apiRequest, TmpBuyerDto.class);

        TmpBuyerDto getTmp = this.requestTmpBuyerInfo(tmpBuyerDto.getSeq());

        List<FileManagerDto> fileManagerDtoList = awsFileService.getFileListByRelationSeqAndFileTypes(tmpBuyerDto.getSeq(), FileTypeEnum.getTmpBuyerFileType());
        if (!ObjectUtils.isEmpty(fileManagerDtoList)) {
            getTmp.setFileList(fileManagerDtoList);
        }

        List<TmpBuyerTermsDto> tmpBuyerTermsDtoList = tmpBuyerTermsService.getTmpBuyerTermsList(tmpBuyerDto.getSeq());
        getTmp.setTmpBuyerTermsList(tmpBuyerTermsDtoList);

        return ApiResponse.success(getTmp);
    }

    /**
     * 임시 회원정보 요청
     * @param tmpBuyerSeq
     * @return
     */
    public TmpBuyerDto requestTmpBuyerInfo(long tmpBuyerSeq) {
        if (ObjectUtils.isEmpty(tmpBuyerSeq)) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "TMP_SEQ");
        }

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.TMP_BUYER)
                .queryParam("tmpSeq", tmpBuyerSeq)
                .encode();

        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.GET, uriComponentsBuilder.build().toUri());
        return ObjectMapperUtil.responseConvertDto(response.block().getBody(), TmpBuyerDto.class);
    }

    // 관리자
    // 임시회원가입 및 회원수정 승인/반려
    public void approveBuyer(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        // 필수 값이 오지 않으면 오류
        if (ObjectUtils.isEmpty(map) || !map.containsKey("tmpSeq") || !map.containsKey("changeState")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "tmpSeq, changeState");
        }

        String changeState = map.get("changeState").toString();

        // 승인이나 반려로 변경하는게 아닐 시 Exception
        if (!(Objects.equals(changeState, BuyerStateConstants.REJECTED) || Objects.equals(changeState, BuyerStateConstants.DONE))) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "NOT_MATCH_STATE", changeState);
        }

        // 반려 메시지에 대한 내용 없으면 공백 처리. 반려시 반려 메시지 입력 필수
        String rejectedMsg = "";
        if (Objects.equals(BuyerStateConstants.REJECTED, changeState)) {
            rejectedMsg = map.get("rejectedMsg").toString();
            ObjectCheck.isBlankStringException(rejectedMsg, "REJECTED_MSG");
        }

        // 승인/반려 Request
        Long tmpSeq = Long.valueOf(map.get("tmpSeq").toString());
        TmpBuyerDto tmpBuyerDto = this.requestTmpBuyerInfo(tmpSeq); // 현재의 상태를 담기 위해서 조회를 먼저 함.
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder
                .fromHttpUrl(PathConstants.TMP_BUYER_APPROVE)
                .queryParam("tmpSeq", tmpSeq)
                .queryParam("changeState", changeState)
                .queryParam("rejectedMsg", rejectedMsg)
                .queryParam("userCode", SessionUtil.getUserCode())
                .encode();

        // 승인/반려 Response
        ResponseEntity<ApiResponse> response = webClientUtils.requestWebClient(HttpMethod.PATCH, uriComponentsBuilder.build().toUri()).block();
        BuyerIdentificationDto dto = ObjectMapperUtil.responseConvertDto(response.getBody(), BuyerIdentificationDto.class);

        /* 신규가입자 대상 상태변경 성공시 이메일 발송 - yh.lee */
        if (response.getStatusCode().is2xxSuccessful()) {
            if (tmpBuyerDto.getState().equals(BuyerStateConstants.READY)) {
                if (changeState.equals(BuyerStateConstants.DONE))
                    emailService.sendForSignUpAccept(tmpBuyerDto.getCorpEmail(), tmpBuyerDto.getBuyerIdentificationId());
                if (changeState.equals(BuyerStateConstants.REJECTED))
                    emailService.sendForSignUpReject(tmpBuyerDto.getCorpEmail(), rejectedMsg);
            }
        }

        // 처리 완료된 BUYER Seq. 가입에서 반려된 상태면 value = 0.
        Long buyerSeq = Long.valueOf(String.valueOf(dto.getBuyer().getSeq()));

        // 승인/반려에 따라 파일 처리.
        awsFileService.copyTmpBuyerToBuyer(tmpSeq, buyerSeq, changeState);

        // 승인/반려에 따라 약관 처리.
        tmpBuyerTermsService.registerBuyerTerms(tmpSeq, dto.getBuyer().getBuyerCode(), changeState);

        // 가입 정상으로 처리 되었을때만 포인트 등록
        if (buyerSeq > 0L) {
            PointDto pointDto = pointService.getBuyerPointByBuyerCode(dto.getBuyer().getBuyerCode());
            if (ObjectUtils.isEmpty(pointDto)) {
                pointService.registerBuyerPoint(dto.getBuyer().getBuyerCode());
            }
        }

    }

    // 사용자
    // 임시회원가입 및 회원수정 요청
    public ApiResponse registerTmpBuyer(ApiRequest apiRequest, String state) {
        TmpBuyerDto tmpBuyerDto = ObjectMapperUtil.requestConvertDto(apiRequest, TmpBuyerDto.class);

        // 가입 요청인 경우 : 회원 약관에 대한 내용 필수
        if (Objects.equals(BuyerStateConstants.READY, state)) {
            // 아이디 체크 : 메인 아이디에 숫자가 포함되면 오류
            ObjectCheck.notNumeric(tmpBuyerDto.getBuyerIdentificationId());
            // 약관 등록 필수
            if (tmpBuyerDto.getTmpBuyerTermsList() != null && tmpBuyerDto.getTmpBuyerTermsList().size() <= 0) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "tmpBuyerTermsList");
            }
        } else {
            tmpBuyerDto.setBuyerIdentificationCode(SessionUtil.getUserCode());
        }

        // 가입 수정 동일 : 수정요청 한 약관 동의 내역이 있는데 해당 약관이 없는 약관이면 Exception
        if (tmpBuyerDto.getTmpBuyerTermsList() != null && tmpBuyerDto.getTmpBuyerTermsList().size() > 0) {
            List<Long> termsSeqList = tmpBuyerDto.getTmpBuyerTermsList().stream().map(item -> item.getTerms().getSeq()).collect(Collectors.toList());
            if (!(termsService.existTerms(termsSeqList))) {
                throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "term list");
            }
        }

        // 임시 회원 등록 request
        tmpBuyerDto.setState(state);
        Mono<ResponseEntity<ApiResponse>> response = webClientUtils.requestWebClient(HttpMethod.POST, PathConstants.TMP_BUYER, tmpBuyerDto.encryptPassword());
        ApiResponse apiResponse = response.block().getBody();

        // 임시 회원 등록 response
        TmpBuyerDto result = ObjectMapperUtil.responseConvertDto(apiResponse, TmpBuyerDto.class);

        // 회원 약관 등록
        List<TmpBuyerTermsDto> userTermsList = tmpBuyerDto.getTmpBuyerTermsList();

        // 가입일 경우만 : 회원이 체크 하지 않은 약관 리스트 조회. -> 내역이 있으면 해당 내역은 미동의 처리.
        if (Objects.equals(BuyerStateConstants.READY, state)) {
            List<Long> registerTermsList = new ArrayList<>();
            if (!ObjectUtils.isEmpty(userTermsList)) {
                registerTermsList = userTermsList.stream().map(item -> item.getTerms().getSeq()).collect(Collectors.toList());
            }
            List<TermsDto> termsDtoList = termsService.searchTermsList(null, null, null, registerTermsList);
            if (!ObjectUtils.isEmpty(termsDtoList)) {
                for (TermsDto dto : termsDtoList) {
                    userTermsList.add(dto.convertTmpBuyerTerms("N"));
                }
            }
        }

        tmpBuyerTermsService.addTmpBuyerTerms(userTermsList, result.getSeq(), result.getBuyerIdentificationCode());
        return apiResponse;
    }

}
