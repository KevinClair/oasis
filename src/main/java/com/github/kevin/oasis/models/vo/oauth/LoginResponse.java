package com.github.kevin.oasis.models.vo.oauth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {

    /**
     * JWT Token
     */
    private String token;

    /**
     * Refresh Token
     */
    private String refreshToken;
}
