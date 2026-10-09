package com.dynamic.product.repository;

import com.dynamic.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
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
    SELECT p
    FROM Product p
    JOIN p.category c
    WHERE p.visible = true

    AND (
        :filterCategories = false
        OR c.id IN :categoryIds
        OR c.parent.id IN :categoryIds
    )

    AND (:minPrice IS NULL OR p.price >= :minPrice)
    AND (:maxPrice IS NULL OR p.price <= :maxPrice)

    AND (
        :filterKeyword = false
        OR LOWER(p.name) LIKE CONCAT('%', LOWER(:keyword), '%')
        OR LOWER(p.description) LIKE CONCAT('%', LOWER(:keyword), '%')
        OR LOWER(c.name) LIKE CONCAT('%', LOWER(:keyword), '%')
    )
    """)
    Page<Product> filterProducts(
            @Param("filterCategories") boolean filterCategories,
            @Param("categoryIds") List<Long> categoryIds,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("filterKeyword") boolean filterKeyword,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
