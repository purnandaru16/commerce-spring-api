package com.commerce.demo.service;

import com.commerce.demo.dto.request.ProductRequest;
import com.commerce.demo.dto.response.ProductResponse;
import com.commerce.demo.entity.Category;
import com.commerce.demo.entity.Product;
import com.commerce.demo.exception.ResourceNotFoundException;
import com.commerce.demo.repository.CategoryRepository;
import com.commerce.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public Page<ProductResponse> getAllProducts(Long categoryId, String keyword, Pageable pageable) {
        if (categoryId != null && keyword != null) {
            return productRepository
                    .findByCategoryIdAndNameContainingIgnoreCase(categoryId, keyword, pageable)
                    .map(this::mapToResponse);
        } else if (categoryId != null) {
            return productRepository
                    .findByCategoryId(categoryId, pageable)
                    .map(this::mapToResponse);
        } else if (keyword != null) {
            return productRepository
                    .searchByKeyword(keyword, pageable)
                    .map(this::mapToResponse);
        } else {
            return productRepository
                    .findAll(pageable)
                    .map(this::mapToResponse);
        }
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponse(product);
    }

    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(category)
                .build();

        return mapToResponse(productRepository.save(product));
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(category);

        return mapToResponse(productRepository.save(product));
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStock())
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .createdAt(product.getCreatedAt())
                .build();
    }
}