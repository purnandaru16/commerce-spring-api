package com.commerce.demo.controller;

import com.commerce.demo.dto.request.CategoryRequest;
import com.commerce.demo.dto.response.CommonResponse;
import com.commerce.demo.dto.response.CategoryResponse;
import com.commerce.demo.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "2. Categories", description = "Category management — GET is public, POST/PUT/DELETE requires ADMIN token")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(
            summary = "Get all categories",
            description = "Retrieve list of all available categories. No authentication required."
    )
    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully")
    public ResponseEntity<CommonResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(CommonResponse.success("Categories retrieved", categoryService.getAllCategories()));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get category by ID",
            description = "Retrieve a single category by its ID. No authentication required."
    )
    @ApiResponse(responseCode = "200", description = "Category found")
    @ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<CommonResponse<CategoryResponse>> getCategoryById(
            @Parameter(description = "Category ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(CommonResponse.success("Category retrieved", categoryService.getCategoryById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Create category",
            description = "Create a new product category. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "201", description = "Category created successfully")
    @ApiResponse(responseCode = "400", description = "Category name already exists or validation failed")
    @ApiResponse(responseCode = "403", description = "Access denied — ADMIN only")
    public ResponseEntity<CommonResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("Category created", categoryService.createCategory(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Update category",
            description = "Update an existing category by ID. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Category updated successfully")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @ApiResponse(responseCode = "403", description = "Access denied — ADMIN only")
    public ResponseEntity<CommonResponse<CategoryResponse>> updateCategory(
            @Parameter(description = "Category ID", example = "1")
            @PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(CommonResponse.success("Category updated", categoryService.updateCategory(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(
            summary = "Delete category",
            description = "Delete a category by ID. Cannot delete if category still has products. Requires ADMIN role."
    )
    @ApiResponse(responseCode = "200", description = "Category deleted successfully")
    @ApiResponse(responseCode = "400", description = "Category still has products")
    @ApiResponse(responseCode = "404", description = "Category not found")
    @ApiResponse(responseCode = "403", description = "Access denied — ADMIN only")
    public ResponseEntity<CommonResponse<Void>> deleteCategory(
            @Parameter(description = "Category ID", example = "1")
            @PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(CommonResponse.success("Category deleted", null));
    }
}