package kr.co.medicals.terms.domain.repository;

import kr.co.medicals.terms.domain.entity.BuyerTerms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuyerTermsRepository extends JpaRepository<BuyerTerms, Long> {

}
