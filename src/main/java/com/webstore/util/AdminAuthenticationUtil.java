package com.webstore.util;

import com.webstore.constant.UserRole;
import com.webstore.dto.response.AuthResponse;
import com.webstore.entity.User;
import com.webstore.repository.UserRepository;
import com.webstore.service.GoogleTokenVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

//Handles all admin-specific authentication logic
 
@Slf4j
@Component
public class AdminAuthenticationUtil {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AdminAuthenticationUtil(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /*@param userInfo Google user information extracted from token
     * @return AuthResponse containing JWT token and user details
     * @throws ResponseStatusException if user not found or not an admin*/
    public AuthResponse authenticate(GoogleTokenVerificationService.GoogleUserInfo userInfo) {
        log.info("Authenticating as ADMIN for email: {}", userInfo.getEmail());

        // Find user by email
        User user = findUserByEmail(userInfo.getEmail());

        // Validate user is an admin
        validateAdminRole(user, userInfo.getEmail());

        // Generate JWT token
        String jwtToken = generateToken(user);

        //  Log successful token generation
        log.info("JWT token generated for admin: {}", user.getEmail());

        //  Build and return response
        return buildResponse(jwtToken, user);
    }

    /*Finds a user by email from the database
     * @param email User's email address
     * @return User entity if found
     * @throws ResponseStatusException if user not found
     */
    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Admin user not found with email: " + email));
    }

    /*Validates that the user has ADMIN role
     *@param user User entity to validate
     * @param email User's email for error message
     * @throws ResponseStatusException if user is not an admin
     */
    private void validateAdminRole(User user, String email) {
        if (!UserRole.ADMIN.equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User with email " + email + " is not an admin");
        }
    }

    /*Generates JWT token for admin user
     * @param user User entity
     * @return JWT token string
     */
    private String generateToken(User user) {
        return jwtTokenProvider.generateTokenForAdmin(
                user.getUserId(),
                user.getEmail(),
                user.getRole());
    }

    /*Builds authentication response for admin
     *@param jwtToken Generated JWT token
     * @param user User entity
     * @return AuthResponse with all admin details
     */
    private AuthResponse buildResponse(String jwtToken, User user) {
        AuthResponse response = new AuthResponse();
        response.setJwtToken(jwtToken);
        response.setUserId(user.getUserId());
        response.setEmail(user.getEmail());
        response.setName(user.getFullName() != null ? user.getFullName() : user.getUsername());
        response.setRole(user.getRole());
        return response;
    }
}