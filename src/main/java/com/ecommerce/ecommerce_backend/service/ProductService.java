package com.ecommerce.ecommerce_backend.service;


import com.ecommerce.ecommerce_backend.entity.Product;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Page<Product> findAllActive(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    public Page<Product> findByCategory(String categoryName, Pageable pageable) {
        return productRepository.findByCategoryNameAndActiveTrue(categoryName, pageable);
    }

    public Page<Product> searchProducts(String searchTerm, Pageable pageable) {
        return productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseAndActiveTrue(
                searchTerm, searchTerm, pageable);
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findByIdAndActiveTrue(id);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            // Soft delete - set active to false instead of hard delete
            Product p = product.get();
            p.setActive(false);
            productRepository.save(p);
        }
    }

    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }

    public List<String> getAllCategories() {
        return productRepository.findDistinctCategories();
    }

    public List<Product> getFeaturedProducts() {
        // Return top 8 products (you can implement your own logic for featured products)
        return productRepository.findTop8ByActiveTrueOrderByCreatedAtDesc();
    }

    public Optional<Product> updateStock(Long productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStockQuantity(quantity);
            return Optional.of(productRepository.save(product));
        }
        return Optional.empty();
    }

    public boolean isProductAvailable(Long productId, Integer requestedQuantity) {
        Optional<Product> product = findById(productId);
        return product.map(p -> p.isAvailable() && p.getStockQuantity() >= requestedQuantity)
                .orElse(false);
    }

    public void decreaseStock(Long productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            int newQuantity = product.getStockQuantity() - quantity;
            if (newQuantity < 0) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            product.setStockQuantity(newQuantity);
            productRepository.save(product);
        } else {
            throw new RuntimeException("Product not found with id: " + productId);
        }
    }

    public void increaseStock(Long productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStockQuantity(product.getStockQuantity() + quantity);
            productRepository.save(product);
        }
    }

    public List<Product> findLowStockProducts(Integer threshold) {
        return productRepository.findByStockQuantityLessThanAndActiveTrue(threshold);
    }

    public long getTotalActiveProducts() {
        return productRepository.countByActiveTrue();
    }
}
