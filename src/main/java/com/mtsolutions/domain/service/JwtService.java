package com.mtsolutions.domain.service;

import com.mtsolutions.application.exception.ApplicationAuthenticationFailedException;
import com.mtsolutions.domain.constant.OwnerRole;
import com.mtsolutions.domain.dto.response.AppTokenResponseDto;
import com.mtsolutions.domain.entity.ApplicationOwner;
import com.mtsolutions.domain.repository.ApplicationOwnerRepository;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.time.Duration;
import java.util.Set;

@ApplicationScoped
@Slf4j
public class JwtService {

    @ConfigProperty(name = "mp.jwt.verify.issuer") String jwtIssuer;
    @ConfigProperty(name = "app.mt.id.token.expiration.hours") Integer tokenExpirationInHours;

    private final ApplicationOwnerRepository applicationOwnerRepository;
    private final BcryptService bcryptService;

    public JwtService(ApplicationOwnerRepository applicationOwnerRepository, BcryptService bcryptService) {
        this.applicationOwnerRepository = applicationOwnerRepository;
        this.bcryptService = bcryptService;
    }

    public AppTokenResponseDto generateOwnerToken(String ownerEmail, String ownerPassword) {
        String normalizedOwnerEmail = ownerEmail != null ? ownerEmail.trim() : null;

        if (normalizedOwnerEmail == null || normalizedOwnerEmail.isBlank()
                || ownerPassword == null || ownerPassword.isBlank()) {
            throw new ApplicationAuthenticationFailedException();
        }

        ApplicationOwner owner = this.applicationOwnerRepository.findOwnerByEmail(normalizedOwnerEmail)
                .orElseThrow(ApplicationAuthenticationFailedException::new);

        if (Boolean.FALSE.equals(owner.getActive())
                || owner.getPassword() == null
                || owner.getPassword().getPassword() == null
                || !this.bcryptService.verifyPassword(ownerPassword, owner.getPassword().getPassword())) {
            throw new ApplicationAuthenticationFailedException();
        }

        OwnerRole ownerRole = owner.getRole();
        if (ownerRole == null) {
            ownerRole = OwnerRole.OWNER_VIEWER;
            owner.setRole(ownerRole);
            this.applicationOwnerRepository.persistOrUpdate(owner);
        }

        Set<String> groups = Set.of(ownerRole.name());

        String accessToken = Jwt.issuer(jwtIssuer)
                .subject(owner.getOwnerId())
                .upn(owner.getEmail().getEmail())
                .claim("ownerId", owner.getOwnerId() != null ? owner.getOwnerId() : null)
                .claim("name", owner.getName())
                .claim("emailVerified", owner.getEmail().getVerified())
                .groups(groups)
                .expiresIn(Duration.ofHours(tokenExpirationInHours))
                .sign();

        return new AppTokenResponseDto(
                accessToken,
                "Bearer",
                Duration.ofHours(tokenExpirationInHours).getSeconds()
        );
    }
}
