package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.entity.CartItem;
import com.ecommerce.ecommerce_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByUser(User user);

    List<CartItem> findByUserId(Long userId);

    Optional<CartItem> findByUserAndProductId(User user, Long productId);

    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);

    void deleteByUser(User user);

    void deleteByUserId(Long userId);
}
