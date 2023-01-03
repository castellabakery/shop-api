package kr.co.medicals.cart.domain.repository;

import kr.co.medicals.cart.domain.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Cart findBySeqAndDelYn(Long seq, String delYn);
}
