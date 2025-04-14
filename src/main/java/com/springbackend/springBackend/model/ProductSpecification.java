package com.springbackend.springBackend.model;

import org.springframework.data.jpa.domain.Specification;
import java.util.Optional;

public class ProductSpecification {

    public static Specification<Product> filterByCriteria(String name, Long categoryId, Double minPrice, Double maxPrice) {
        return Specification.where(hasName(name))
                .and(hasCategory(categoryId))
                .and(hasPriceBetween(minPrice, maxPrice));
    }

    private static Specification<Product> hasName(String name) {
        return (root, query, cb) ->
                Optional.ofNullable(name)
                        .map(n -> cb.like(cb.lower(root.get("name")), "%" + n.toLowerCase() + "%"))
                        .orElse(null);
    }

    private static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) ->
                Optional.ofNullable(categoryId)
                        .map(id -> cb.equal(root.get("categoryId"), id))
                        .orElse(null);
    }

    private static Specification<Product> hasPriceBetween(Double minPrice, Double maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) return null;
            if (minPrice == null) return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
            if (maxPrice == null) return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            return cb.between(root.get("price"), minPrice, maxPrice);
        };
    }
}
