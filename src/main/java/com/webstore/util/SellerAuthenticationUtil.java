package com.webstore.util;

import com.webstore.dto.response.AuthResponse;
import com.webstore.entity.Seller;
import com.webstore.repository.SellerRepository;
import com.webstore.service.GoogleTokenVerificationService;
import com.webstore.util.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;


@Slf4j
@Component
public class SellerAuthenticationUtil {

    private final SellerRepository sellerRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public SellerAuthenticationUtil(SellerRepository sellerRepository, JwtTokenProvider jwtTokenProvider) {
        this.sellerRepository = sellerRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /*@param userInfo Google user information extracted from token
     * @return AuthResponse containing JWT token and seller details
     * @throws ResponseStatusException if seller not found or inactive
     */
    public AuthResponse authenticate(GoogleTokenVerificationService.GoogleUserInfo userInfo) {
        log.info("Authenticating as SELLER for email: {}", userInfo.getEmail());

        //  Find seller by email
        Seller seller = findSellerByEmail(userInfo.getEmail());

        //  Validate seller is active
        validateSellerStatus(seller);

        //  Generate JWT token
        String jwtToken = generateToken(seller);

        //  Log successful token generation
        log.info("JWT token generated for seller: {}", seller.getEmail());

        //  Build and return response
        return buildResponse(jwtToken, seller);
    }

    /*@param email Seller's email address
     * @return Seller entity if found
     * @throws ResponseStatusException if seller not found
     */
    private Seller findSellerByEmail(String email) {
        return sellerRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Seller not found with email: " + email));
    }

    /*@param seller Seller entity to validate
     * @throws ResponseStatusException if seller is not active
     */
    private void validateSellerStatus(Seller seller) {
        if (seller.getStatus() != Seller.SellerStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Seller account is not active");
        }
    }

    /*@param seller Seller entity
     * @return JWT token string
     */
    private String generateToken(Seller seller) {
        return jwtTokenProvider.generateToken(
                seller.getSellerId(),
                seller.getEmail(),
                seller.getRole().name());
    }

    /*@param jwtToken Generated JWT token
     * @param seller Seller entity
     * @return AuthResponse with all seller details
     */
    private AuthResponse buildResponse(String jwtToken, Seller seller) {
        AuthResponse response = new AuthResponse();
        response.setJwtToken(jwtToken);
        response.setSellerId(seller.getSellerId());
        response.setEmail(seller.getEmail());
        response.setName(seller.getName());
        response.setRole(seller.getRole().name());
        return response;
    }
}