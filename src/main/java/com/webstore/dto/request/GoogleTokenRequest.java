package com.webstore.dto.request;

import lombok.Data;

@Data
public class GoogleTokenRequest {
    private String googleToken;
    private String userType; // "SELLER" or "ADMIN"
}