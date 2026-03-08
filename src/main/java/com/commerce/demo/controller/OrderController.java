package com.commerce.demo.controller;

import com.commerce.demo.dto.response.CommonResponse;
import com.commerce.demo.dto.response.OrderResponse;
import com.commerce.demo.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "5. Orders", description = "Order management — USER endpoints for own orders, ADMIN endpoints for all orders")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders/checkout")
    @Operation(
            summary = "Checkout",
            description = "Create an order from current cart. Stock will be deducted automatically. Cart will be cleared after successful checkout."
    )
    @ApiResponse(responseCode = "200", description = "Order placed successfully")
    @ApiResponse(responseCode = "400", description = "Cart is empty or insufficient stock")
    @ApiResponse(responseCode = "401", description = "Unauthorized — token required")
    public ResponseEntity<CommonResponse<OrderResponse>> checkout(Principal principal) {
        return ResponseEntity.ok(CommonResponse.success("Order placed successfully",
                orderService.checkout(principal.getName())));
    }

    @GetMapping("/orders")
    @Operation(
            summary = "Get order history",
            description = "Retrieve paginated order history for the currently logged-in user."
    )
    @ApiResponse(responseCode = "200", description = "Order history retrieved")
    @ApiResponse(responseCode = "401", description = "Unauthorized — token required")
    public ResponseEntity<CommonResponse<Page<OrderResponse>>> getOrderHistory(
            Principal principal,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(CommonResponse.success("Order history retrieved",
                orderService.getOrderHistory(principal.getName(), pageable)));
    }

    @GetMapping("/orders/{id}")
    @Operation(
            summary = "Get order detail",
            description = "Retrieve detail of a specific order by ID. Only accessible by the order owner."
    )
    @ApiResponse(responseCode = "200", description = "Order retrieved")
    @ApiResponse(responseCode = "400", description = "Order does not belong to current user")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized — token required")
    public ResponseEntity<CommonResponse<OrderResponse>> getOrderById(
            Principal principal,
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(CommonResponse.success("Order retrieved",
                orderService.getOrderById(principal.getName(), id)));
    }

    @PutMapping("/orders/{id}/cancel")
    @Operation(
            summary = "Cancel order",
            description = "Cancel a PENDING order. Stock will be returned automatically. Only PENDING orders can be cancelled."
    )
    @ApiResponse(responseCode = "200", description = "Order cancelled successfully")
    @ApiResponse(responseCode = "400", description = "Order cannot be cancelled — not in PENDING status")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized — token required")
    public ResponseEntity<CommonResponse<OrderResponse>> cancelOrder(
            Principal principal,
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(CommonResponse.success("Order cancelled",
                orderService.cancelOrder(principal.getName(), id)));
    }

    // ── ADMIN endpoints ──
    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all orders — ADMIN",
            description = "Retrieve paginated list of all orders from all users. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "All orders retrieved")
    @ApiResponse(responseCode = "403", description = "Access denied — ADMIN only")
    public ResponseEntity<CommonResponse<Page<OrderResponse>>> getAllOrders(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(CommonResponse.success("All orders retrieved",
                orderService.getAllOrders(pageable)));
    }

    @PutMapping("/admin/orders/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Update order status — ADMIN",
            description = "Update the status of any order. Available statuses: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Order status updated")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "403", description = "Access denied — ADMIN only")
    public ResponseEntity<CommonResponse<OrderResponse>> updateOrderStatus(
            @Parameter(description = "Order ID", example = "1")
            @PathVariable Long id,
            @Parameter(description = "New status", example = "CONFIRMED")
            @RequestParam String status) {
        return ResponseEntity.ok(CommonResponse.success("Order status updated",
                orderService.updateOrderStatus(id, status)));
    }
}