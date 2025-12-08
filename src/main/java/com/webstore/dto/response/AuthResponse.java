package com.webstore.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String jwtToken;
    private Integer sellerId;
    private Integer userId;
    private String email;
    private String name;
    private String role;
}