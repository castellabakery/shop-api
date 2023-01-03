package kr.co.medicals.board.notice.service;

import kr.co.medicals.board.notice.domain.dto.NoticeDto;
import kr.co.medicals.board.notice.domain.repository.NoticeRepository;
import kr.co.medicals.board.notice.domain.repository.NoticeRepositorySupport;
import kr.co.medicals.common.ApiRequestFailExceptionMsg;
import kr.co.medicals.common.constants.ApiResponseCodeConstants;
import kr.co.medicals.common.util.ApiRequest;
import kr.co.medicals.common.util.ObjectMapperUtil;
import kr.co.medicals.common.util.SessionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final NoticeRepositorySupport noticeRepositorySupport;

    @Autowired
    public NoticeService(NoticeRepository noticeRepository, NoticeRepositorySupport noticeRepositorySupport) {
        this.noticeRepository = noticeRepository;
        this.noticeRepositorySupport = noticeRepositorySupport;
    }

    /**
     * 관리자 공지사항 목록 조회
     */
    public Page<NoticeDto> searchNoticeList(ApiRequest apiRequest) {
        NoticeDto searchParam = ObjectMapperUtil.requestConvertDto(apiRequest, NoticeDto.class);

        String displayYn = "Y"; // admin : 파라미터 값으로 조회, buyer : 공개설정된 내용만 조회.
        if (Objects.equals("admin", SessionUtil.getUserType())) {
            displayYn = searchParam.getDisplayYn();
        }

        return noticeRepositorySupport.searchNoticeList(searchParam, displayYn, PageRequest.of(apiRequest.getPage(), apiRequest.getPageSize()));
    }

    /**
     * 관리자 공지사항 단건 조회.
     */
    public NoticeDto getNoticeDetailBySeq(ApiRequest apiRequest) {

        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("seq")) { // 값이 없으면
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "seq");
        }

        if (Long.valueOf(map.get("seq").toString()) <= 0L) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_MATCH, "NOT_MATCH", "seq");
        }

        String displayYn = "Y"; // admin : 파라미터 값으로 조회, buyer : 공개설정된 내용만 조회.
        if (Objects.equals("admin", SessionUtil.getUserType())) {
            if (map.containsKey("displayYn")) {
                displayYn = map.get("displayYn").toString();
            } else {
                displayYn = null;
            }
        }

        return Optional.ofNullable(noticeRepositorySupport.getNoticeDetailBySeq(Long.valueOf(map.get("seq").toString()), displayYn))
                .orElseThrow(() -> new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "notice"));
    }


    /**
     * 관리자 공지사항 등록 및 수정.
     */
    public void addModifyAdminBoard(ApiRequest apiRequest) {

        NoticeDto paramDto = ObjectMapperUtil.requestConvertDto(apiRequest, NoticeDto.class);

        if (ObjectUtils.isEmpty(paramDto.getSeq())) {
            noticeRepository.save(paramDto.insertEntity());
        } else {
            NoticeDto oldNotice =
                    Optional.ofNullable(noticeRepositorySupport.getNoticeDetailBySeq(paramDto.getSeq(), null))
                            .orElseThrow(() -> new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.NOT_EXISTS, "NOT_EXISTS", "notice"));
            noticeRepository.save(oldNotice.updateEntity(paramDto));
        }

    }

    /**
     * 관리자 공지사항 삭제.
     */
    public void deleteAdminBoard(ApiRequest apiRequest) {
        Map<String, Object> map = ObjectMapperUtil.requestGetMap(apiRequest);

        if (ObjectUtils.isEmpty(map) || !map.containsKey("seq")) {
            throw new ApiRequestFailExceptionMsg(ApiResponseCodeConstants.REQUIRED_EMPTY, "REQUIRED_EMPTY", "seq");
        }

        NoticeDto noticeDto = noticeRepositorySupport.getNoticeDetailBySeq(Long.valueOf(map.get("seq").toString()), null);
        noticeRepository.save(noticeDto.updateDelY(SessionUtil.getUserCode()));
    }


}
