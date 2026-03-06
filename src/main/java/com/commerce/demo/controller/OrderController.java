package com.commerce.demo.controller;

import com.commerce.demo.dto.response.ApiResponse;
import com.commerce.demo.dto.response.OrderResponse;
import com.commerce.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders/checkout")
    public ResponseEntity<ApiResponse<OrderResponse>> checkout(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Order placed successfully",
                orderService.checkout(principal.getName())));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrderHistory(
            Principal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Order history retrieved",
                orderService.getOrderHistory(principal.getName(), pageable)));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            Principal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Order retrieved",
                orderService.getOrderById(principal.getName(), id)));
    }

    @PutMapping("/orders/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            Principal principal,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Order cancelled",
                orderService.cancelOrder(principal.getName(), id)));
    }

    // ── ADMIN endpoints ──
    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("All orders retrieved",
                orderService.getAllOrders(pageable)));
    }

    @PutMapping("/admin/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(ApiResponse.success("Order status updated",
                orderService.updateOrderStatus(id, status)));
    }
}