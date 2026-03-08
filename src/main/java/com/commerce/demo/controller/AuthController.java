package com.commerce.demo.controller;

import com.commerce.demo.dto.request.LoginRequest;
import com.commerce.demo.dto.request.RegisterRequest;
import com.commerce.demo.dto.response.CommonResponse;
import com.commerce.demo.dto.response.AuthResponse;
import com.commerce.demo.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "1. Authentication", description = "Register and login to get JWT token")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Create a new user account with USER role. Returns JWT token upon success."
    )
    @ApiResponse(responseCode = "201", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Email already registered or validation failed")
    public ResponseEntity<CommonResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CommonResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    @Operation(
            summary = "Login",
            description = "Authenticate with email and password. Returns JWT token to be used in Authorization header."
    )
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "400", description = "Invalid email or password")
    public ResponseEntity<CommonResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(CommonResponse.success("Login successful", response));
    }
}
