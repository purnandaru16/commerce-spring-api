package com.commerce.demo.service;

import com.commerce.demo.dto.request.AddToCartRequest;
import com.commerce.demo.dto.request.UpdateCartItemRequest;
import com.commerce.demo.dto.response.CartItemResponse;
import com.commerce.demo.dto.response.CartResponse;
import com.commerce.demo.entity.Cart;
import com.commerce.demo.entity.CartItem;
import com.commerce.demo.entity.Product;
import com.commerce.demo.entity.User;
import com.commerce.demo.exception.ResourceNotFoundException;
import com.commerce.demo.repository.CartItemRepository;
import com.commerce.demo.repository.CartRepository;
import com.commerce.demo.repository.ProductRepository;
import com.commerce.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartResponse getCart(String email) {
        Cart cart = getOrCreateCart(email);
        return mapToResponse(cart);
    }

    @Transactional
    public CartResponse addItem(String email, AddToCartRequest request) {
        Cart cart = getOrCreateCart(email);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getStock() < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStock());
        }

        // Kalau product sudah ada di cart, update quantity
        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), product.getId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            if (product.getStock() < newQuantity) {
                throw new IllegalArgumentException("Insufficient stock. Available: " + product.getStock());
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }

        return mapToResponse(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse updateItem(String email, Long itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(email);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        // Pastikan item milik cart user ini
        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to current user");
        }

        if (item.getProduct().getStock() < request.getQuantity()) {
            throw new IllegalArgumentException("Insufficient stock. Available: " + item.getProduct().getStock());
        }

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
        return mapToResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public CartResponse removeItem(String email, Long itemId) {
        Cart cart = getOrCreateCart(email);

        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new IllegalArgumentException("Cart item does not belong to current user");
        }

        cart.getItems().remove(item);
        cartItemRepository.delete(item);
        return mapToResponse(cartRepository.findById(cart.getId()).orElseThrow());
    }

    @Transactional
    public void clearCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        cartRepository.findByUserId(user.getId())
                .ifPresent(cartRepository::delete); // ← hapus cart sekalian
    }

    // Helper: get cart atau buat baru kalau belum ada
    private Cart getOrCreateCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(
                        Cart.builder().user(user).build()
                ));
    }

    private CartResponse mapToResponse(Cart cart) {
        List<CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> CartItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .price(item.getProduct().getPrice())
                        .quantity(item.getQuantity())
                        .subtotal(item.getProduct().getPrice()
                                .multiply(BigDecimal.valueOf(item.getQuantity())))
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalPrice = itemResponses.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .id(cart.getId())
                .items(itemResponses)
                .totalPrice(totalPrice)
                .totalItems(itemResponses.size())
                .build();
    }
}
