package kr.co.medicals.buyer.domain.repository;

import kr.co.medicals.buyer.domain.entity.BuyerVaccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface BuyerVaccountRepository extends JpaRepository<BuyerVaccount, Long> {

    Optional<BuyerVaccount> findByBuyerCode(String buyerCode);

    @Transactional
    @Modifying
    @Query("update BuyerVaccount set vaccountWithdrawTid = ?1, modifiedDatetime = current_timestamp where buyerCode = ?2")
    void setVaccountWithdrawTid(String vaccountWithdrawTid, String buyerCode);

}
