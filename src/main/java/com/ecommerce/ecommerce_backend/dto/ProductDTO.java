package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.entity.Product;
import lombok.Data;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private CategoryDTO category; // Use CategoryDTO instead of Category
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // You can add a static method to convert from Entity to DTO
    public static ProductDTO fromEntity(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStockQuantity(product.getStockQuantity());
        dto.setImageUrl(product.getImageUrl());
        dto.setActive(product.getActive());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());

        if (product.getCategory() != null) {
            ModelMapper modelMapper = new ModelMapper();
            dto.setCategory(modelMapper.map(product.getCategory(), CategoryDTO.class));
        }

        return dto;
    }
}
