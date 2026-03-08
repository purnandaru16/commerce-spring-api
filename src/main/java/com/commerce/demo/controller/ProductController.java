package com.commerce.demo.controller;

import com.commerce.demo.dto.request.ProductRequest;
import com.commerce.demo.dto.response.CommonResponse;
import com.commerce.demo.dto.response.ProductResponse;
import com.commerce.demo.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "3. Products", description = "Product management — GET is public, POST/PUT/DELETE requires ADMIN token")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(
            summary = "Get all products",
            description = "Retrieve paginated list of products. Supports filtering by category and keyword search. No authentication required."
    )
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully")
    public ResponseEntity<CommonResponse<Page<ProductResponse>>> getAllProducts(
            @Parameter(description = "Filter by category ID", example = "1")
            @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Search by product name or description", example = "headphone")
            @RequestParam(required = false) String keyword,
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Items per page", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction: asc or desc", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductResponse> products = productService.getAllProducts(categoryId, keyword, pageable);
        return ResponseEntity.ok(CommonResponse.success("Products retrieved", products));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get product by ID",
            description = "Retrieve a single product by its ID. No authentication required."
    )
    @ApiResponse(responseCode = "200", description = "Product found")
    @ApiResponse(responseCode = "404", description = "Product not found")
    public ResponseEntity<CommonResponse<ProductResponse>> getProductById(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(CommonResponse.success("Product retrieved", productService.getProductById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create product",
            description = "Create a new product under a specific category. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "201", description = "Product created successfully")
    @ApiResponse(responseCode = "400", description = "Validation failed or category not found")
    @ApiResponse(responseCode = "403", description = "Access denied — ADMIN only")
    public ResponseEntity<CommonResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("Product created", productService.createProduct(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update product",
            description = "Update an existing product by ID. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Product updated successfully")
    @ApiResponse(responseCode = "404", description = "Product or category not found")
    @ApiResponse(responseCode = "403", description = "Access denied — ADMIN only")
    public ResponseEntity<CommonResponse<ProductResponse>> updateProduct(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(CommonResponse.success("Product updated", productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete product",
            description = "Delete a product by ID. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Product deleted successfully")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @ApiResponse(responseCode = "403", description = "Access denied — ADMIN only")
    public ResponseEntity<CommonResponse<Void>> deleteProduct(
            @Parameter(description = "Product ID", example = "1")
            @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(CommonResponse.success("Product deleted", null));
    }
}
