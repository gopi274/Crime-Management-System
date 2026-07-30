package com.crimemanagement.controller;

import com.crimemanagement.dto.ApiResponse;
import com.crimemanagement.dto.AuthResponse;
import com.crimemanagement.dto.LoginRequest;
import com.crimemanagement.entity.UserAccount;
import com.crimemanagement.service.AuthService;
import com.crimemanagement.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest loginRequest) {
        try {
            UserAccount user = authService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
            if (user != null) {
                String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
                AuthResponse authResponse = new AuthResponse(token, user.getUsername(), user.getRole());
                return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
            } else {
                return ResponseEntity.status(401).body(ApiResponse.error("Invalid username or password"));
            }
        } catch (SQLException e) {
            return ResponseEntity.internalServerError().body(ApiResponse.error("Login error: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserAccount>> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            // In a full implementation, we would check the token against security context.
            // For now, we'll just extract username from token as a proof of concept.
            UserAccount mockUser = new UserAccount();
            mockUser.setUsername(username);
            return ResponseEntity.ok(ApiResponse.success("Current user identified", mockUser));
        }
        return ResponseEntity.status(401).body(ApiResponse.error("Not authenticated"));
    }
}
