package com.webstore.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.Optional;

public class SecurityContextUtils {

    private SecurityContextUtils() {
        // Utility class
    }

    public static Optional<Integer> getCurrentSellerIdOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            String role = getCurrentRole();
            // Only return sellerId if role is SELLER
            if (role != null && "SELLER".equals(role)) {
                Object credentials = authentication.getCredentials();
                if (credentials instanceof Integer) {
                    return Optional.of((Integer) credentials);
                }
            }
        }
        return Optional.empty();
    }
    
    public static Integer getCurrentSellerId() {
        return getCurrentSellerIdOptional().orElse(null);
    }

    public static Optional<Integer> getCurrentUserIdOptional() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            String role = getCurrentRole();
            // Only return userId if role is ADMIN
            if (role != null && "ADMIN".equals(role)) {
                Object credentials = authentication.getCredentials();
                if (credentials instanceof Integer) {
                    return Optional.of((Integer) credentials);
                }
            }
        }
        return Optional.empty();
    }

    public static Integer getCurrentUserId() {
        return getCurrentUserIdOptional().orElse(null);
    }

    public static Optional<String> getCurrentSellerEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            return Optional.of((String) authentication.getPrincipal());
        }
        return Optional.empty();
    }

    public static String getCurrentRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            if (authorities != null && !authorities.isEmpty()) {
                GrantedAuthority authority = authorities.iterator().next();
                String authorityString = authority.getAuthority();
                // Extract role from "ROLE_SELLER" or "ROLE_ADMIN"
                if (authorityString.startsWith("ROLE_")) {
                    return authorityString.substring(5);
                }
                return authorityString;
            }
        }
        return null;
    }

    public static boolean isSeller() {
        String role = getCurrentRole();
        return role != null && "SELLER".equals(role);
    }

    public static boolean isAdmin() {
        String role = getCurrentRole();
        return role != null && "ADMIN".equals(role);
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}