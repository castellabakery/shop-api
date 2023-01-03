package kr.co.medicals.terms;

import kr.co.medicals.terms.domain.dto.BuyerTermsDto;
import kr.co.medicals.terms.domain.repository.BuyerTermsRepository;
import kr.co.medicals.terms.domain.repository.BuyerTermsRepositorySupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class BuyerTermsService {

    private final BuyerTermsRepositorySupport buyerTermsRepositorySupport;
    private final BuyerTermsRepository buyerTermsRepository;

    @Autowired
    public BuyerTermsService(BuyerTermsRepositorySupport buyerTermsRepositorySupport, BuyerTermsRepository buyerTermsRepository) {
        this.buyerTermsRepositorySupport = buyerTermsRepositorySupport;
        this.buyerTermsRepository = buyerTermsRepository;
    }

    // 회원 약관 리스트 조회
    public List<BuyerTermsDto> getBuyerTermsList(String buyerCode, List<Long> termsSeqList) {
        return buyerTermsRepositorySupport.getBuyerTermsList(buyerCode, termsSeqList);
    }

    // 회원 약관 등록
    public void addBuyerTerms(List<BuyerTermsDto> buyerTermsDto) {
        buyerTermsRepository.saveAll(buyerTermsDto.stream().map(list -> list.insertEntity()).collect(Collectors.toList()));
    }

}
