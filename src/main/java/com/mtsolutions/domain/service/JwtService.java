package com.mtsolutions.domain.service;

import com.mtsolutions.domain.constant.OwnerRole;
import com.mtsolutions.domain.dto.response.AppTokenResponseDto;
import com.mtsolutions.domain.dto.response.OwnerTokenResponseDto;
import com.mtsolutions.domain.dto.response.UserTokenResponseDto;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.entity.User;
import com.mtsolutions.domain.model.Email;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Set;

@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String jwtIssuer;

    public OwnerTokenResponseDto generateOwnerToken(Owner owner, String refreshTokenId, Duration accessExpiration, Duration refreshExpiration) {
        OwnerRole ownerRole = owner.getRole() != null ? owner.getRole() : OwnerRole.OWNER_VIEWER;
        Set<String> groups = Set.of(ownerRole.name());

        String accessToken = Jwt.issuer(jwtIssuer)
                .subject(owner.getOwnerId())
                .upn(owner.getEmail().getEmail())
                .claim("ownerId", owner.getOwnerId())
                .claim("name", owner.getName())
                .claim("emailVerified", owner.getEmail().getVerified())
                .claim("token_type", "access")
                .groups(groups)
                .expiresIn(accessExpiration)
                .sign();

        String refreshToken = Jwt.issuer(jwtIssuer)
                .subject(owner.getOwnerId())
                .upn(owner.getEmail().getEmail())
                .claim("ownerId", owner.getOwnerId())
                .claim("name", owner.getName())
                .claim("emailVerified", owner.getEmail().getVerified())
                .claim("token_type", "refresh")
                .claim("jti", refreshTokenId)
                .groups(Set.of("REFRESH_TOKEN"))
                .expiresIn(refreshExpiration)
                .sign();

        return new OwnerTokenResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                accessExpiration.getSeconds(),
                refreshExpiration.getSeconds()
        );
    }

    public AppTokenResponseDto generateApplicationToken(ClientApplication clientApplication, Duration expiration) {
        String accessToken = Jwt.issuer(jwtIssuer)
                .subject(clientApplication.getAppId())
                .claim("app_id", clientApplication.getAppId())
                .claim("app_name", clientApplication.getName())
                .claim("token_type", "access")
                .groups(Set.of("APPLICATION"))
                .expiresIn(expiration)
                .sign();

        return tokenResponse(accessToken, expiration);
    }

    public UserTokenResponseDto generateUserToken(User user, Email loginEmail, String refreshTokenId, Duration accessExpiration, Duration refreshExpiration) {
        String accessToken = Jwt.issuer(jwtIssuer)
                .subject(user.getUserId())
                .upn(loginEmail.getEmail())
                .claim("userId", user.getUserId())
                .claim("app_id", user.getAppId())
                .claim("name", user.getName())
                .claim("emailVerified", loginEmail.getVerified())
                .claim("token_type", "access")
                .groups(Set.of("USER"))
                .expiresIn(accessExpiration)
                .sign();

        String refreshToken = Jwt.issuer(jwtIssuer)
                .subject(user.getUserId())
                .upn(loginEmail.getEmail())
                .claim("userId", user.getUserId())
                .claim("app_id", user.getAppId())
                .claim("name", user.getName())
                .claim("emailVerified", loginEmail.getVerified())
                .claim("token_type", "refresh")
                .claim("jti", refreshTokenId)
                .groups(Set.of("REFRESH_TOKEN"))
                .expiresIn(refreshExpiration)
                .sign();

        return new UserTokenResponseDto(
                accessToken,
                refreshToken,
                "Bearer",
                accessExpiration.getSeconds(),
                refreshExpiration.getSeconds()
        );
    }

    private AppTokenResponseDto tokenResponse(String accessToken, Duration expiration) {
        return new AppTokenResponseDto(accessToken, "Bearer", expiration.getSeconds());
    }
}
