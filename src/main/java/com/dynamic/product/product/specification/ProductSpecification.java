package com.dynamic.product.product.specification;

import com.dynamic.product.product.entity.Product;
import com.dynamic.product.category.entity.Category;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class ProductSpecification {

    private ProductSpecification() {
    }

    public static Specification<Product> filterProducts(
            List<Long> categoryIds,
            Double minPrice,
            Double maxPrice,
            String keyword) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.isTrue(root.get("visible")));

            if (categoryIds != null && !categoryIds.isEmpty()) {

                Join<Product, Category> category = root.join("category", JoinType.LEFT);

                Join<Category, Category> parent = category.join("parent", JoinType.LEFT);

                Predicate directCategory = category.get("id").in(categoryIds);

                Predicate parentCategory = parent.get("id").in(categoryIds);

                predicates.add(cb.or(directCategory, parentCategory));
            }

            if (minPrice != null) {
                predicates.add(
                        cb.greaterThanOrEqualTo(
                                root.get("price"),
                                minPrice
                        )
                );
            }

            if (maxPrice != null) {
                predicates.add(
                        cb.lessThanOrEqualTo(
                                root.get("price"),
                                maxPrice
                        )
                );
            }

            if (keyword != null && !keyword.isBlank()) {

                String pattern = "%" + keyword.trim().toLowerCase() + "%";

                Join<Product, Category> category = root.join("category", JoinType.LEFT);

                Predicate nameMatch = cb.like(
                        cb.lower(root.get("name")),
                        pattern
                );

                Predicate descriptionMatch = cb.like(
                        cb.lower(root.get("description")),
                        pattern
                );

                Predicate categoryMatch = cb.like(
                        cb.lower(category.get("name")),
                        pattern
                );

                predicates.add(
                        cb.or(
                                nameMatch,
                                descriptionMatch,
                                categoryMatch
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

