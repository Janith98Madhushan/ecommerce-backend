package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.entity.*;
import com.ecommerce.ecommerce_backend.repository.OrderRepository;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public Page<Order> getOrdersByUser(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUser(user, pageable);
    }

    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    public Order createOrder(Long userId, String shippingAddress, String paymentMethod) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Get cart items
        List<CartItem> cartItems = cartService.getCartItems(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        // Validate cart items and stock
        cartService.validateAndUpdateCartItems(userId);
        cartItems = cartService.getCartItems(userId); // Refresh after validation

        if (cartItems.isEmpty()) {
            throw new RuntimeException("No valid items in cart");
        }

        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            BigDecimal itemTotal = item.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        // Create order
        Order order = new Order(user, totalAmount, shippingAddress);
        order.setPaymentMethod(paymentMethod);
        order.setTrackingNumber(generateTrackingNumber());
        order = orderRepository.save(order);

        // Create order items and update stock
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            // Check stock availability one more time
            if (!productService.isProductAvailable(cartItem.getProduct().getId(), cartItem.getQuantity())) {
                throw new RuntimeException("Product " + cartItem.getProduct().getName() + " is no longer available");
            }

            // Create order item
            OrderItem orderItem = new OrderItem(order, cartItem.getProduct(),
                    cartItem.getQuantity(), cartItem.getProduct().getPrice());
            orderItems.add(orderItem);

            // Decrease stock
            productService.decreaseStock(cartItem.getProduct().getId(), cartItem.getQuantity());
        }

        order.setOrderItems(orderItems);
        order = orderRepository.save(order);

        // Clear cart
        cartService.clearCart(userId);

        return order;
    }

    public Order updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderStatus currentStatus = order.getStatus();

        // Validate status transition
        if (!isValidStatusTransition(currentStatus, status)) {
            throw new RuntimeException("Invalid status transition from " + currentStatus + " to " + status);
        }

        order.setStatus(status);
        return orderRepository.save(order);
    }

    public Order cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Check if user owns the order
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to cancel this order");
        }

        // Check if order can be cancelled
        if (order.getStatus() == OrderStatus.SHIPPED ||
                order.getStatus() == OrderStatus.DELIVERED ||
                order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order cannot be cancelled in current status: " + order.getStatus());
        }

        // Restore stock for cancelled order
        for (OrderItem item : order.getOrderItems()) {
            productService.increaseStock(item.getProduct().getId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status.name(), Pageable.unpaged()).getContent();
    }

    public List<Order> getOrdersBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findOrdersBetweenDates(startDate, endDate);
    }

    public long getOrdersCountByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status.name());
    }

    public Double getTotalRevenue() {
        Double revenue = orderRepository.getTotalRevenue();
        return revenue != null ? revenue : 0.0;
    }

    public Order processPayment(Long orderId, String paymentDetails) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Order is not in pending status");
        }

        // Here you would integrate with payment gateway
        // For now, we'll simulate successful payment
        boolean paymentSuccessful = simulatePayment(paymentDetails);

        if (paymentSuccessful) {
            order.setStatus(OrderStatus.CONFIRMED);
        } else {
            // Restore stock if payment fails
            for (OrderItem item : order.getOrderItems()) {
                productService.increaseStock(item.getProduct().getId(), item.getQuantity());
            }
            order.setStatus(OrderStatus.CANCELLED);
        }

        return orderRepository.save(order);
    }

    private boolean simulatePayment(String paymentDetails) {
        // Simulate payment processing
        // In real implementation, integrate with Stripe, PayPal, etc.
        return true; // Assume payment is always successful for demo
    }

    private String generateTrackingNumber() {
        return "TRK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private boolean isValidStatusTransition(OrderStatus from, OrderStatus to) {
        switch (from) {
            case PENDING:
                return to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED:
                return to == OrderStatus.PROCESSING || to == OrderStatus.CANCELLED;
            case PROCESSING:
                return to == OrderStatus.SHIPPED || to == OrderStatus.CANCELLED;
            case SHIPPED:
                return to == OrderStatus.DELIVERED;
            case DELIVERED:
                return to == OrderStatus.REFUNDED;
            case CANCELLED:
            case REFUNDED:
                return false; // No transitions allowed from these states
            default:
                return false;
        }
    }

    public boolean userOwnsOrder(Long userId, Long orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        return order.isPresent() && order.get().getUser().getId().equals(userId);
    }
}
