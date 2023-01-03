package kr.co.medicals.terms;

import kr.co.medicals.common.constants.BuyerStateConstants;
import kr.co.medicals.terms.domain.dto.BuyerTermsDto;
import kr.co.medicals.terms.domain.dto.TmpBuyerTermsDto;
import kr.co.medicals.terms.domain.entity.BuyerTerms;
import kr.co.medicals.terms.domain.entity.TmpBuyerTerms;
import kr.co.medicals.terms.domain.repository.BuyerTermsRepository;
import kr.co.medicals.terms.domain.repository.TmpBuyerTermsRepository;
import kr.co.medicals.terms.domain.repository.TmpBuyerTermsRepositorySupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class TmpBuyerTermsService {

    private final TmpBuyerTermsRepositorySupport tmpBuyerTermsRepositorySupport;
    private final BuyerTermsRepository buyerTermsRepository;
    private final TmpBuyerTermsRepository tmpBuyerTermsRepository;
    private final BuyerTermsService buyerTermsService;

    @Autowired
    public TmpBuyerTermsService(TmpBuyerTermsRepositorySupport tmpBuyerTermsRepositorySupport, BuyerTermsRepository buyerTermsRepository, TmpBuyerTermsRepository tmpBuyerTermsRepository, BuyerTermsService buyerTermsService) {
        this.tmpBuyerTermsRepositorySupport = tmpBuyerTermsRepositorySupport;
        this.buyerTermsRepository = buyerTermsRepository;
        this.tmpBuyerTermsRepository = tmpBuyerTermsRepository;
        this.buyerTermsService = buyerTermsService;
    }

    // 임시회원 - 임시 약관 등록
    public void addTmpBuyerTerms(List<TmpBuyerTermsDto> tmpBuyerTermsDtoList, Long tmpSeq, String buyerIdentificationCode) {
        if (tmpBuyerTermsDtoList != null) {
            List<TmpBuyerTerms> tmpBuyerTermsList = tmpBuyerTermsDtoList.stream().map(list -> list.insertEntity(tmpSeq, buyerIdentificationCode)).collect(Collectors.toList());
            tmpBuyerTermsRepository.saveAll(tmpBuyerTermsList);
        }

    }

    // 회원 임시 약관 조회.
    public List<TmpBuyerTermsDto> getTmpBuyerTermsList(Long tmpSeq) {
        return tmpBuyerTermsRepositorySupport.getTmpBuyerTermsList(tmpSeq);
    }

    // 승인/반려에 따라 약관 처리.
    public void registerBuyerTerms(Long tmpSeq, String buyerCode, String state) {
        // 임시로 등록되어있던 약관 내용 조회.
        List<TmpBuyerTermsDto> tmpBuyerTermsDtoList = this.getTmpBuyerTermsList(tmpSeq);

        if (Objects.equals(BuyerStateConstants.DONE, state)) {

            // 임시로 등록되어있던 약관이 없으면 종료.
            if (tmpBuyerTermsDtoList.size() <= 0) {
                return;
            }

            // 기존에 존재하던 회원 약관 조회.
            List<Long> tmpBuyerTermsSeqList = tmpBuyerTermsDtoList.stream().map(item -> item.getTerms().getSeq()).collect(Collectors.toList());
            List<BuyerTermsDto> buyerTermsOld = buyerTermsService.getBuyerTermsList(buyerCode, tmpBuyerTermsSeqList);

            // update/insert dto
            List<BuyerTerms> newBuyerTerms = new ArrayList<>();

            // 기존에 존재하던 회원 약관이 있을때.
            if (!ObjectUtils.isEmpty(buyerTermsOld)) {
                // 기존에 존재하던 내용 사용 안함으로 dto 내용 변경.
                for (BuyerTermsDto dto : buyerTermsOld) {
                    boolean isExists = tmpBuyerTermsDtoList.stream().anyMatch(list -> list.getTerms().getSeq().equals(dto.getTerms().getSeq()));
                    if (isExists) {
                        dto.setDelYn("Y");
                        newBuyerTerms.add(dto.updateEntity());
                    }
                }
            }

            // 신규 내용 새로 등록으로 변경
            for (TmpBuyerTermsDto dto : tmpBuyerTermsDtoList) {
                newBuyerTerms.add(dto.convertBuyerDto(buyerCode).insertEntity());
            }

            // 약관 신규-등록, 기존-사용안함으로 업데이트
            buyerTermsRepository.saveAll(newBuyerTerms);
        }

        // 승인/반려 동일 : 임시 회원 약관 내용 사용 안함으로 변경.
        tmpBuyerTermsRepository.saveAll(tmpBuyerTermsDtoList.stream().map(list -> list.updateUseYnEntity()).collect(Collectors.toList()));

    }


}
