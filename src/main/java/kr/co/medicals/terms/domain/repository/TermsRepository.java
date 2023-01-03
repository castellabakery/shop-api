package kr.co.medicals.terms.domain.repository;

import kr.co.medicals.terms.domain.entity.Terms;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TermsRepository extends JpaRepository<Terms, Long> {
}
