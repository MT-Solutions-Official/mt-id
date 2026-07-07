package com.mtsolutions.domain.service;

import com.mtsolutions.domain.dto.response.AppTokenResponseDto;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApplicationAuthService {

    private final JwtService jwtService;

    public ApplicationAuthService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public AppTokenResponseDto generateOwnerToken(String email, String password) {
        return this.jwtService.generateOwnerToken(email, password);
    }

    public AppTokenResponseDto generateApplicationToken(String apiKey, String apiSecret) {
        return this.jwtService.generateApplicationToken(apiKey, apiSecret);
    }
}
