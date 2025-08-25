package com.ecommerce.ecommerce_backend.dto;


import com.ecommerce.ecommerce_backend.entity.CartItem;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;



@Getter
@Setter
public class CartItemDTO{
    Long id;
    UserDTO user;
    ProductDTO product;
    Integer quantity;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    public static CartItemDTO fromEntity(CartItem cartItem) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(cartItem.getId());
        dto.setQuantity(cartItem.getQuantity());
        dto.setCreatedAt(cartItem.getCreatedAt());
        dto.setUpdatedAt(cartItem.getUpdatedAt());

        if (cartItem.getUser() != null) {
            dto.setUser(UserDTO.fromEntity(cartItem.getUser()));
        }

        if (cartItem.getProduct() != null) {
            dto.setProduct(ProductDTO.fromEntity(cartItem.getProduct()));
        }

        return dto;
    }

}
