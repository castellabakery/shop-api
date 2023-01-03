package kr.co.medicals.terms;

import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ObjectCheck;
import kr.co.medicals.common.util.ObjectMapperUtil;
import kr.co.medicals.terms.domain.dto.TermsDto;
import kr.co.medicals.terms.domain.repository.TermsRepository;
import kr.co.medicals.terms.domain.repository.TermsRepositorySupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class TermsService {

    private final TermsRepositorySupport termsRepositorySupport;
    private final TermsRepository termsRepository;

    @Autowired
    public TermsService(TermsRepositorySupport termsRepositorySupport, TermsRepository termsRepository) {
        this.termsRepositorySupport = termsRepositorySupport;
        this.termsRepository = termsRepository;
    }

    // 약관 등록
    public void addTerms(ApiRequest apiRequest) {
        TermsDto termsDto = ObjectMapperUtil.requestConvertDto(apiRequest, TermsDto.class);
        termsRepository.save(termsDto.insertEntity());
    }

    // 약관 수정
    public void modifyTerms(ApiRequest apiRequest) {
        TermsDto newTermsDto = ObjectMapperUtil.requestConvertDto(apiRequest, TermsDto.class);
        newTermsDto.checkUpdateRequire();

        TermsDto oldTermsDto = Optional.ofNullable(termsRepositorySupport.findTermsDetail(newTermsDto.getSeq()))
                .orElseThrow(() -> new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "term"));
        termsRepository.save(oldTermsDto.updateEntity(newTermsDto));
    }

    // 약관 리스트 조회
    public Page<TermsDto> getTermsListPage(ApiRequest apiRequest) {
        TermsDto searchDto = ObjectMapperUtil.requestConvertDto(apiRequest, TermsDto.class);
        return termsRepositorySupport.findTermsList(searchDto.getTitle(), searchDto.getContent(), PageRequest.of(apiRequest.getPage(), apiRequest.getPageSize()));
    }

    public List<TermsDto> searchTermsList(String optionYn, String title, String content, List<Long> notEqSeqList) {
        return termsRepositorySupport.findTermsListAll(optionYn, title, content, notEqSeqList);
    }

    // 약관 상세 조회
    public TermsDto getTermsDetail(ApiRequest apiRequest) {
        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("seq")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "seq");
        }

        Long seq = ObjectCheck.isBlankLongException(map.get("seq"), "seq");
        return Optional.ofNullable(termsRepositorySupport.findTermsDetail(seq))
                .orElseThrow(() -> new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "term"));
    }

    // 약관 존재여부 확인
    public boolean existTerms(List<Long> termsSeqList) {
        List<TermsDto> termsDtoList = termsRepositorySupport.getExistsTermsList(termsSeqList);
        if (termsSeqList.size() == termsDtoList.size()) {
            return true;
        }
        return false;
    }

}
