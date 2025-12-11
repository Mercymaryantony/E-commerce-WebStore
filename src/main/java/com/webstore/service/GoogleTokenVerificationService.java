package com.webstore.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webstore.entity.Seller;
import com.webstore.repository.SellerRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class GoogleTokenVerificationService {

    @Value("${google.oauth.client-id}")
    private String clientId;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private OkHttpClient okHttpClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Data
    public static class GoogleUserInfo {
        private String email;
        private String name;
    }

    public GoogleUserInfo verifyTokenAndGetUserInfo(String googleToken) {
        try {
            // Verify token with Google
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + googleToken;
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Google token verification failed with status: {}", response.code());
                    throw new RuntimeException("Invalid Google token");
                }

                String responseBody = response.body().string();
                JsonNode jsonNode = objectMapper.readTree(responseBody);

                // Verify audience (client ID)
                String audience = jsonNode.get("aud").asText();
                if (!clientId.equals(audience)) {
                    log.error("Token audience mismatch. Expected: {}, Got: {}", clientId, audience);
                    throw new RuntimeException("Invalid token audience");
                }

                // Extract email and name
                String email = jsonNode.get("email").asText();
                String name = jsonNode.has("name") ? jsonNode.get("name").asText() : email;
                log.info("Google token verified for email: {}", email);

                GoogleUserInfo userInfo = new GoogleUserInfo();
                userInfo.setEmail(email);
                userInfo.setName(name);
                return userInfo;

            }
        } catch (IOException e) {
            log.error("Error verifying Google token: {}", e.getMessage());
            throw new RuntimeException("Failed to verify Google token: " + e.getMessage(), e);
        }
    }

    public Seller verifyTokenAndGetSeller(String googleToken) {
        GoogleUserInfo userInfo = verifyTokenAndGetUserInfo(googleToken);
        // Find seller by email
        return sellerRepository.findByEmail(userInfo.getEmail())
                .orElseThrow(() -> new RuntimeException("Seller not found with email: " + userInfo.getEmail()));
    }
}