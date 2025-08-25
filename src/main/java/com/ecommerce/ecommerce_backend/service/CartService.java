package com.ecommerce.ecommerce_backend.service;


import com.ecommerce.ecommerce_backend.dto.CartItemDTO;
import com.ecommerce.ecommerce_backend.entity.CartItem;
import com.ecommerce.ecommerce_backend.entity.Product;
import com.ecommerce.ecommerce_backend.entity.User;
import com.ecommerce.ecommerce_backend.repository.CartItemRepository;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CartService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    public List<CartItem> getCartItems(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }

    public CartItemDTO addToCart(Long userId, Long productId, Integer quantity) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if product is available
        if (!product.isAvailable()) {
            throw new RuntimeException("Product is not available");
        }

        // Check stock availability
        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity());
        }

        // Check if item already exists in cart
        Optional<CartItem> existingCartItem = cartItemRepository.findByUserAndProductId(user, productId);

        if (existingCartItem.isPresent()) {
            // Update quantity
            CartItem cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + quantity;

            // Check total quantity against stock
            if (product.getStockQuantity() < newQuantity) {
                throw new RuntimeException("Insufficient stock. Available: " + product.getStockQuantity() +
                        ", Requested: " + newQuantity);
            }

            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
            return CartItemDTO.fromEntity(cartItem);
        } else {
            // Create new cart item
            CartItem cartItem = new CartItem(user, product, quantity);
            cartItemRepository.save(cartItem);
            return CartItemDTO.fromEntity(cartItem);
        }
    }

    public CartItemDTO updateCartItem(Long cartItemId, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (quantity <= 0) {
            cartItemRepository.delete(cartItem);
            return null;
        }

        // Check stock availability
        if (cartItem.getProduct().getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock. Available: " +
                    cartItem.getProduct().getStockQuantity());
        }

        cartItem.setQuantity(quantity);
        return CartItemDTO.fromEntity(cartItemRepository.save(cartItem));
    }

    public void removeFromCart(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found"));
        cartItemRepository.delete(cartItem);
    }

    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    public double getCartTotal(Long userId) {
        List<CartItem> cartItems = getCartItems(userId);
        return cartItems.stream()
                .mapToDouble(item -> item.getProduct().getPrice().doubleValue() * item.getQuantity())
                .sum();
    }

    public int getCartItemsCount(Long userId) {
        List<CartItem> cartItems = getCartItems(userId);
        return cartItems.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public boolean validateCartItems(Long userId) {
        List<CartItem> cartItems = getCartItems(userId);

        for (CartItem item : cartItems) {
            Product product = item.getProduct();

            // Check if product is still available
            if (!product.isAvailable()) {
                return false;
            }

            // Check if sufficient stock is available
            if (product.getStockQuantity() < item.getQuantity()) {
                return false;
            }
        }

        return true;
    }

    public void validateAndUpdateCartItems(Long userId) {
        List<CartItem> cartItems = getCartItems(userId);

        for (CartItem item : cartItems) {
            Product product = item.getProduct();

            // Remove unavailable products
            if (!product.isAvailable()) {
                cartItemRepository.delete(item);
                continue;
            }

            // Adjust quantity if insufficient stock
            if (product.getStockQuantity() < item.getQuantity()) {
                if (product.getStockQuantity() > 0) {
                    item.setQuantity(product.getStockQuantity());
                    cartItemRepository.save(item);
                } else {
                    cartItemRepository.delete(item);
                }
            }
        }
    }

    public CartItem findCartItem(Long userId, Long productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cartItemRepository.findByUserAndProductId(user, productId)
                .orElse(null);
    }
}
