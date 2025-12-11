package com.webstore.controller;

import com.webstore.constant.UserRole;
import com.webstore.dto.request.GoogleTokenRequest;
import com.webstore.dto.response.AuthResponse;
import com.webstore.service.GoogleTokenVerificationService;
import com.webstore.util.AdminAuthenticationUtil;
import com.webstore.util.SellerAuthenticationUtil;
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
    private AdminAuthenticationUtil adminAuthenticationUtil;

    @Autowired
    private SellerAuthenticationUtil sellerAuthenticationUtil;

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleTokenRequest request) {
        log.info("Google login attempt, userType: {}", request.getUserType());

        try {
            //  Verify Google token and extract user information
            GoogleTokenVerificationService.GoogleUserInfo userInfo = googleTokenVerificationService
                    .verifyTokenAndGetUserInfo(request.getGoogleToken());

            // Determine user type (ADMIN or SELLER), default to SELLER
            String userType = request.getUserType() != null 
                    ? UserRole.normalize(request.getUserType()) 
                    : UserRole.SELLER;

            // Route to appropriate authentication utility
            AuthResponse response;
            if (UserRole.ADMIN.equals(userType)) {
                response = adminAuthenticationUtil.authenticate(userInfo);
            } else {
                response = sellerAuthenticationUtil.authenticate(userInfo);
            }

            //  Return successful authentication response
            return ResponseEntity.ok(response);

        } catch (ResponseStatusException e) {
            // Re-throw HTTP exceptions (404, 403, etc.)
            throw e;
        } catch (Exception e) {
            // Handle unexpected errors
            log.error("Error during Google login: {}", e.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication failed: " + e.getMessage());
        }
    }
}