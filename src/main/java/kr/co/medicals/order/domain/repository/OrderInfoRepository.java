package kr.co.medicals.order.domain.repository;

import kr.co.medicals.order.domain.entity.OrderInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OrderInfoRepository extends JpaRepository<OrderInfo, Long> {

    @Transactional
    @Modifying
    @Query("update OrderInfo set orgOrderNo = ?1, tradeResult = ?2, tradeResultMsg = ?3, modifiedDatetime = current_timestamp where orderNo = ?4 and orderInfoState = ?5")
    void setOrderInfoWithSuccess(String tid, String resultCode, String resultMessage, String orderNo, int orderState);

}
