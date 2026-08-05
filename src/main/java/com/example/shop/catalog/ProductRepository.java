package com.example.shop.catalog;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);

    boolean existsBySerialNumber(String serialNumber);

    List<Product> findByActiveTrue();

    Page<Product> findByActiveTrue(Pageable pageable);
    Page<Product> findByActiveTrueAndCategorySlug(String categorySlug, Pageable pageable);
    List<Product> findByActiveTrueAndCategorySlug(String categorySlug);
}
