package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByActiveTrue(Pageable pageable);

    Optional<Product> findByIdAndActiveTrue(Long id);

    Page<Product> findByCategoryNameAndActiveTrue(String categoryName, Pageable pageable);

    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndActiveTrue(
            String name, String description, Pageable pageable);

    List<Product> findTop8ByActiveTrueOrderByCreatedAtDesc();

    List<Product> findByStockQuantityLessThanAndActiveTrue(Integer threshold);

    long countByActiveTrue();

    @Query("SELECT DISTINCT p.category.name FROM Product p WHERE p.active = true")
    List<String> findDistinctCategories();
}

