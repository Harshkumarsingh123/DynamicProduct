package com.dynamic.product.repository;

import com.dynamic.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long> {

    List<Product> findByVisibleTrue();

    List<Product> findByCategoryIdInAndVisibleTrue(List<Long> categoryIds);

    Optional<Product> findByIdAndVisibleTrue(Long id);

    @Query("""
       SELECT p FROM Product p
       WHERE p.visible = true
       AND (
           LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR
           LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
       )
       """)
    List<Product> searchVisibleProducts(@Param("keyword") String keyword);
}
