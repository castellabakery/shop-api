package kr.co.medicals.product.domain.repository;

import kr.co.medicals.product.domain.entity.BuyerTypeProductAmount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuyerTypeProductAmountRepository extends JpaRepository<BuyerTypeProductAmount, Long> {
}
