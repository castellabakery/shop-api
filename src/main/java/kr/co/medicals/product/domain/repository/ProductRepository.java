package kr.co.medicals.product.domain.repository;

import kr.co.medicals.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByStandardCode(String standardCode);
}
