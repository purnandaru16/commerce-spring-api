package com.commerce.demo.service;

import com.commerce.demo.dto.response.OrderItemResponse;
import com.commerce.demo.dto.response.OrderResponse;
import com.commerce.demo.entity.*;
import com.commerce.demo.exception.ResourceNotFoundException;
import com.commerce.demo.repository.OrderRepository;
import com.commerce.demo.repository.ProductRepository;
import com.commerce.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    @Transactional
    public OrderResponse checkout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Ambil cart user
        var cartResponse = cartService.getCart(email);

        if (cartResponse.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        // Buat order baru
        Order order = Order.builder()
                .user(user)
                .status(Order.OrderStatus.PENDING)
                .totalAmount(BigDecimal.ZERO)
                .build();

        // Proses setiap item di cart
        List<OrderItem> orderItems = cartResponse.getItems().stream().map(cartItem -> {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

            // Validasi stok
            if (product.getStock() < cartItem.getQuantity()) {
                throw new IllegalArgumentException(
                        "Insufficient stock for: " + product.getName() +
                                ". Available: " + product.getStock()
                );
            }

            // Kurangi stok
            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);

            return OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(cartItem.getPrice())
                    .build();
        }).collect(Collectors.toList());

        // Set items dan hitung total
        order.setItems(orderItems);
        order.setTotalAmount(cartResponse.getTotalPrice());

        Order savedOrder = orderRepository.save(order);

        // Kosongkan cart setelah checkout
        cartService.clearCart(email);

        return mapToResponse(savedOrder);
    }

    public Page<OrderResponse> getOrderHistory(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(this::mapToResponse);
    }

    public OrderResponse getOrderById(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        // Pastikan order milik user ini
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Order does not belong to current user");
        }

        return mapToResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Order does not belong to current user");
        }

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING orders can be cancelled");
        }

        // Kembalikan stok
        order.getItems().forEach(item -> {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        });

        order.setStatus(Order.OrderStatus.CANCELLED);
        return mapToResponse(orderRepository.save(order));
    }

    // Khusus ADMIN — update status order
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(Order.OrderStatus.valueOf(status.toUpperCase()));
        return mapToResponse(orderRepository.save(order));
    }

    // Khusus ADMIN — get semua order
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::mapToResponse);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .priceAtPurchase(item.getPriceAtPurchase())
                        .subtotal(item.getPriceAtPurchase()
                                .multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .items(itemResponses)
                .createdAt(order.getCreatedAt())
                .build();
    }
}
