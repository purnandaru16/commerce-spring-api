package com.commerce.demo.controller;

import com.commerce.demo.dto.request.AddToCartRequest;
import com.commerce.demo.dto.request.UpdateCartItemRequest;
import com.commerce.demo.dto.response.ApiResponse;
import com.commerce.demo.dto.response.CartResponse;
import com.commerce.demo.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Principal principal) {
        return ResponseEntity.ok(ApiResponse.success("Cart retrieved",
                cartService.getCart(principal.getName())));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            Principal principal,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Item added to cart",
                cartService.addItem(principal.getName(), request)));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            Principal principal,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cart item updated",
                cartService.updateItem(principal.getName(), itemId, request)));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            Principal principal,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success("Item removed from cart",
                cartService.removeItem(principal.getName(), itemId)));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> clearCart(Principal principal) {
        cartService.clearCart(principal.getName());
        return ResponseEntity.ok(ApiResponse.success("Cart cleared", null));
    }
}
