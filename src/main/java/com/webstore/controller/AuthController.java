package com.webstore.controller;

import com.webstore.dto.request.GoogleTokenRequest;
import com.webstore.dto.response.AuthResponse;
import com.webstore.entity.Seller;
import com.webstore.entity.User;
import com.webstore.repository.SellerRepository;
import com.webstore.repository.UserRepository;
import com.webstore.service.GoogleTokenVerificationService;
import com.webstore.util.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private GoogleTokenVerificationService googleTokenVerificationService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleTokenRequest request) {
        log.info("Google login attempt, userType: {}", request.getUserType());

        try {
            // Verify Google token and get user info
            GoogleTokenVerificationService.GoogleUserInfo userInfo = 
                    googleTokenVerificationService.verifyTokenAndGetUserInfo(request.getGoogleToken());

            String userType = request.getUserType() != null ? request.getUserType().toUpperCase() : "SELLER";

            if ("ADMIN".equals(userType)) {
                // Admin authentication flow
                return authenticateAdmin(userInfo);
            } else {
                // Seller authentication flow (default)
                return authenticateSeller(userInfo);
            }

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during Google login: {}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication failed: " + e.getMessage());
        }
    }

    private ResponseEntity<AuthResponse> authenticateAdmin(GoogleTokenVerificationService.GoogleUserInfo userInfo) {
        log.info("Authenticating as ADMIN for email: {}", userInfo.getEmail());

        // Find user (admin) by email
        User user = userRepository.findByEmail(userInfo.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Admin user not found with email: " + userInfo.getEmail()
                ));

        // Verify user role is ADMIN
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User with email " + userInfo.getEmail() + " is not an admin"
            );
        }

        // Generate JWT token for admin
        String jwtToken = jwtTokenProvider.generateTokenForAdmin(
                user.getUserId(),
                user.getEmail(),
                user.getRole()
        );

        log.info("JWT token generated for admin: {}", user.getEmail());

        AuthResponse response = new AuthResponse();
        response.setJwtToken(jwtToken);
        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setName(user.getFullName() != null ? user.getFullName() : user.getUsername());
        response.setRole(user.getRole());

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<AuthResponse> authenticateSeller(GoogleTokenVerificationService.GoogleUserInfo userInfo) {
        log.info("Authenticating as SELLER for email: {}", userInfo.getEmail());

        // Find seller by email
        Seller seller = sellerRepository.findByEmail(userInfo.getEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Seller not found with email: " + userInfo.getEmail()
                ));

        // Check if seller is active
        if (seller.getStatus() != Seller.SellerStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Seller account is not active");
        }

        // Generate JWT token for seller
        String jwtToken = jwtTokenProvider.generateToken(
                seller.getSellerId(),
                seller.getEmail(),
                seller.getRole().name()
        );

        log.info("JWT token generated for seller: {}", seller.getEmail());

        AuthResponse response = new AuthResponse();
        response.setJwtToken(jwtToken);
        response.setSellerId(seller.getSellerId());
        response.setEmail(seller.getEmail());
        response.setName(seller.getName());
        response.setRole(seller.getRole().name());

        return ResponseEntity.ok(response);
    }
}