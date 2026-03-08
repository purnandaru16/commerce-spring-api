package com.commerce.demo.controller;

import com.commerce.demo.dto.request.AddToCartRequest;
import com.commerce.demo.dto.request.UpdateCartItemRequest;
import com.commerce.demo.dto.response.CommonResponse;
import com.commerce.demo.dto.response.CartResponse;
import com.commerce.demo.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "4. Cart", description = "Shopping cart management — all endpoints require USER token")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(
            summary = "Get cart",
            description = "Retrieve current user's cart with all items and total price."
    )
    @ApiResponse(responseCode = "200", description = "Cart retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized — token required")
    public ResponseEntity<CommonResponse<CartResponse>> getCart(Principal principal) {
        return ResponseEntity.ok(CommonResponse.success("Cart retrieved",
                cartService.getCart(principal.getName())));
    }

    @PostMapping("/items")
    @Operation(
            summary = "Add item to cart",
            description = "Add a product to the cart. If product already exists in cart, quantity will be incremented."
    )
    @ApiResponse(responseCode = "200", description = "Item added to cart")
    @ApiResponse(responseCode = "400", description = "Insufficient stock or product not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized — token required")
    public ResponseEntity<CommonResponse<CartResponse>> addItem(
            Principal principal,
            @Valid @RequestBody AddToCartRequest request) {
        return ResponseEntity.ok(CommonResponse.success("Item added to cart",
                cartService.addItem(principal.getName(), request)));
    }

    @PutMapping("/items/{itemId}")
    @Operation(
            summary = "Update item quantity",
            description = "Update the quantity of a specific item in the cart."
    )
    @ApiResponse(responseCode = "200", description = "Cart item updated")
    @ApiResponse(responseCode = "400", description = "Insufficient stock")
    @ApiResponse(responseCode = "404", description = "Cart item not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized — token required")
    public ResponseEntity<CommonResponse<CartResponse>> updateItem(
            Principal principal,
            @Parameter(description = "Cart item ID", example = "1")
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(CommonResponse.success("Cart item updated",
                cartService.updateItem(principal.getName(), itemId, request)));
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(
            summary = "Remove item from cart",
            description = "Remove a specific item from the cart by cart item ID."
    )
    @ApiResponse(responseCode = "200", description = "Item removed from cart")
    @ApiResponse(responseCode = "404", description = "Cart item not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized — token required")
    public ResponseEntity<CommonResponse<CartResponse>> removeItem(
            Principal principal,
            @Parameter(description = "Cart item ID", example = "1")
            @PathVariable Long itemId) {
        return ResponseEntity.ok(CommonResponse.success("Item removed from cart",
                cartService.removeItem(principal.getName(), itemId)));
    }

    @DeleteMapping
    @Operation(
            summary = "Clear cart",
            description = "Remove all items and delete the cart entirely."
    )
    @ApiResponse(responseCode = "200", description = "Cart cleared successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized — token required")
    public ResponseEntity<CommonResponse<Void>> clearCart(Principal principal) {
        cartService.clearCart(principal.getName());
        return ResponseEntity.ok(CommonResponse.success("Cart cleared", null));
    }
}
