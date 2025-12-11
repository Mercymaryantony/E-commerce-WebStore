package com.webstore.constant;

/*Centralized constants for user roles to avoid string literal duplication*/
public final class UserRole {
    
    
    public static final String ADMIN = "ADMIN";
    
    public static final String SELLER = "SELLER";
    
    private UserRole() {
        // Utility class - prevent instantiation
    }
    
    /* @param role Role string to validate
     * @return true if role is ADMIN or SELLER, false otherwise*/
    public static boolean isValid(String role) {
        return ADMIN.equalsIgnoreCase(role) || SELLER.equalsIgnoreCase(role);
    }
    
    /* @param role Role string to normalize
     * @return Uppercase role string, or null if input is null*/
    public static String normalize(String role) {
        return role != null ? role.toUpperCase() : null;
    }
}