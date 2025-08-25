package com.ecommerce.ecommerce_backend.controller;


import com.ecommerce.ecommerce_backend.dto.ApiResponse;
import com.ecommerce.ecommerce_backend.dto.CartItemDTO;
import com.ecommerce.ecommerce_backend.dto.CartUpdateRequest;
import com.ecommerce.ecommerce_backend.entity.CartItem;
import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.service.CartService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:3000")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CartItemDTO>>> getCart(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            List<CartItem> cartItems = cartService.getCartItems(user.getId());
            return ResponseEntity.ok(ApiResponse.success("Cart retrieved successfully", cartItems.stream().map(CartItemDTO::fromEntity).toList()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving cart: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CartItemDTO>> addToCart(@Valid @RequestBody CartUpdateRequest request,
                                                           Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            CartItemDTO cartItem = cartService.addToCart(user.getId(), request.getProductId(), request.getQuantity());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("Item added to cart successfully", cartItem));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error adding item to cart: " + e.getMessage()));
        }
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<CartItemDTO>> updateCartItem(@PathVariable Long cartItemId,
                                                                @RequestParam Integer quantity,
                                                                Authentication authentication) {
        try {
            CartItemDTO updatedItem = cartService.updateCartItem(cartItemId, quantity);
            if (updatedItem == null) {
                return ResponseEntity.ok(ApiResponse.success("Item removed from cart",null));
            }
            return ResponseEntity.ok(ApiResponse.success("Cart item updated successfully", updatedItem));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error updating cart item: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> removeFromCart(@PathVariable Long cartItemId,
                                                            Authentication authentication) {
        try {
            cartService.removeFromCart(cartItemId);
            return ResponseEntity.ok(ApiResponse.success("Item removed from cart successfully",null));

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error removing item from cart: " + e.getMessage()));
        }
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            cartService.clearCart(user.getId());
            return ResponseEntity.ok(ApiResponse.success("Cart cleared successfully",null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error clearing cart: " + e.getMessage()));
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCartSummary(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            double total = cartService.getCartTotal(user.getId());
            int itemsCount = cartService.getCartItemsCount(user.getId());

            Map<String, Object> summary = new HashMap<>();
            summary.put("total", total);
            summary.put("itemsCount", itemsCount);

            return ResponseEntity.ok(ApiResponse.success("Cart summary retrieved successfully", summary));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error retrieving cart summary: " + e.getMessage()));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateCart(Authentication authentication) {
        try {
            User user = (User) authentication.getPrincipal();
            boolean isValid = cartService.validateCartItems(user.getId());
            return ResponseEntity.ok(ApiResponse.success("Cart validation completed", isValid));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Error validating cart: " + e.getMessage()));
        }
    }
}
