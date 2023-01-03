package kr.co.medicals.terms.domain.repository;

import kr.co.medicals.terms.domain.entity.TmpBuyerTerms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TmpBuyerTermsRepository extends JpaRepository<TmpBuyerTerms, Long> {
}
