package com.ecommerce.ecommerce_backend.controller;


import com.ecommerce.ecommerce_backend.dto.ProductDTO;
import com.ecommerce.ecommerce_backend.entity.Product;
import com.ecommerce.ecommerce_backend.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Pageable pageable) {

        Page<Product> products;

        if (category != null && !category.isEmpty()) {
            products = productService.findByCategory(category, pageable);
        } else if (search != null && !search.isEmpty()) {
            products = productService.searchProducts(search, pageable);
        } else {
            products = productService.findAllActive(pageable);
        }

        Page<ProductDTO> productDTOs = products.map(ProductDTO::fromEntity);
        return ResponseEntity.ok(productDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Optional<Product> product = productService.findById(id);
        return product.map(p -> ResponseEntity.ok(ProductDTO.fromEntity(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product savedProduct = productService.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id,
                                                 @Valid @RequestBody Product productDetails) {
        Optional<Product> existingProduct = productService.findById(id);

        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setName(productDetails.getName() != null ? productDetails.getName() : product.getName());
            product.setDescription(productDetails.getDescription() != null ? productDetails.getDescription() : product.getDescription());
            product.setPrice(productDetails.getPrice() != null ? productDetails.getPrice() : product.getPrice());
            product.setStockQuantity(productDetails.getStockQuantity() != null ? productDetails.getStockQuantity() : product.getStockQuantity());
            product.setImageUrl(productDetails.getImageUrl() != null ? productDetails.getImageUrl() : product.getImageUrl());
            product.setCategory(productDetails.getCategory() != null ? productDetails.getCategory() : product.getCategory());
            product.setActive(productDetails.getActive() != null ? productDetails.getActive() : product.getActive());

            Product updatedProduct = productService.save(product);
            return ResponseEntity.ok(updatedProduct);
        }

        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (productService.existsById(id)) {
            productService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = productService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductDTO>> getFeaturedProducts() {
        List<Product> featuredProducts = productService.getFeaturedProducts();
        List<ProductDTO> featuredProductDTOs = featuredProducts.stream()
                .map(ProductDTO::fromEntity)
                .toList();
        return ResponseEntity.ok(featuredProductDTOs);
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductDTO> updateStock(@PathVariable Long id,
                                               @RequestParam Integer quantity) {
        Optional<Product> product = productService.updateStock(id, quantity);
        return product.map(p -> ResponseEntity.ok(ProductDTO.fromEntity(p)))
                .orElse(ResponseEntity.notFound().build());
    }
}