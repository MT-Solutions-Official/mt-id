package com.mtsolutions.domain.service;

import com.mtsolutions.application.cache.AccountStatusCache;
import com.mtsolutions.application.cache.AllowedOriginCache;
import com.mtsolutions.application.common.ContextComponent;
import com.mtsolutions.application.exception.ApplicationForbiddenException;
import com.mtsolutions.application.exception.OwnerNotFoundException;
import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.application.utils.KeyGeneratorUtils;
import com.mtsolutions.application.utils.NormalizeUtils;
import com.mtsolutions.domain.constant.OwnerRole;
import com.mtsolutions.domain.dto.request.AddOwnersToClientApplicationRequestDto;
import com.mtsolutions.domain.dto.request.CreateClientApplicationRequestDto;
import com.mtsolutions.domain.dto.request.EmailSettingsRequestDto;
import com.mtsolutions.domain.dto.request.UpdateClientApplicationSettingsRequestDto;
import com.mtsolutions.domain.dto.request.UpdateRequiredUserFieldsRequestDto;
import com.mtsolutions.domain.dto.response.AppOwnerResponseDto;
import com.mtsolutions.domain.dto.response.ClientApplicationResponseDto;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.model.AppOwnerAccess;
import com.mtsolutions.domain.model.AppOwnerMembership;
import com.mtsolutions.domain.model.ClientApplicationSecretResult;
import com.mtsolutions.domain.model.EmailSettings;
import com.mtsolutions.domain.repository.ClientApplicationRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@ApplicationScoped
@Slf4j
public class ClientApplicationService {

    private final ClientApplicationRepository clientApplicationRepository;
    private final OwnerService ownerService;
    private final KeyGeneratorUtils keyGeneratorUtils;
    private final BcryptService bcryptService;
    private final DateUtils dateUtils;
    private final ContextComponent contextComponent;
    private final AllowedOriginCache allowedOriginCache;
    private final AccountStatusCache accountStatusCache;
    private final UserRefreshTokenService userRefreshTokenService;

    public ClientApplicationService(ClientApplicationRepository clientApplicationRepository,
                                    OwnerService ownerService,
                                    KeyGeneratorUtils keyGeneratorUtils,
                                    BcryptService bcryptService,
                                    DateUtils dateUtils,
                                    ContextComponent contextComponent,
                                    AllowedOriginCache allowedOriginCache,
                                    AccountStatusCache accountStatusCache,
                                    UserRefreshTokenService userRefreshTokenService) {
        this.clientApplicationRepository = clientApplicationRepository;
        this.ownerService = ownerService;
        this.keyGeneratorUtils = keyGeneratorUtils;
        this.bcryptService = bcryptService;
        this.dateUtils = dateUtils;
        this.contextComponent = contextComponent;
        this.allowedOriginCache = allowedOriginCache;
        this.accountStatusCache = accountStatusCache;
        this.userRefreshTokenService = userRefreshTokenService;
    }

    public ClientApplicationSecretResult createClientApplication(CreateClientApplicationRequestDto request) {
        log.info("Creating client application with name: {}", request.name());

        this.requireAuthenticatedOwner();
        String authenticatedOwnerId = this.contextComponent.getAuthenticatedOwnerId();
        if (!authenticatedOwnerId.equals(request.ownerId())) {
            throw new ApplicationForbiddenException();
        }

        Owner owner = this.ownerService.findOwnerById(request.ownerId());
        String apiKey = this.keyGeneratorUtils.generateApiKey();
        String apiSecret = this.keyGeneratorUtils.generateApiSecret();
        String hashedApiSecret = this.bcryptService.encryptPassword(apiSecret);

        ClientApplication clientApplication = ClientApplication.builder()
                .owners(new ArrayList<>(List.of(AppOwnerMembership.builder()
                        .ownerId(owner.getOwnerId())
                        .role(OwnerRole.OWNER_WRITER)
                        .build())))
                .name(request.name())
                .description(request.description())
                .logoUrl(request.logoUrl())
                .emailSettings(mapEmailSettings(request.emailSettings()))
                .apiKey(apiKey)
                .apiSecret(hashedApiSecret)
                .jwtExpirationInMinutes(request.jwtExpirationInMinutes())
                .refreshTokenExpirationInDays(request.refreshTokenExpirationInDays())
                .allowedOrigins(request.allowedOrigins())
                .googleAudience(NormalizeUtils.trimToNull(request.googleAudience()))
                .requiredUserFields(request.requiredUserFields() != null ? request.requiredUserFields() : new ArrayList<>())
                .createdAt(this.dateUtils.now())
                .updatedAt(this.dateUtils.now())
                .active(true)
                .build();

        this.clientApplicationRepository.persist(clientApplication);
        log.info("Client application created with ID: {}", clientApplication.getAppId());

        return new ClientApplicationSecretResult(clientApplication, apiSecret);
    }

    public List<ClientApplication> listMyApplications() {
        this.requireAuthenticatedOwner();
        return this.clientApplicationRepository.findByOwnerId(this.contextComponent.getAuthenticatedOwnerId());
    }

    public ClientApplication findOwnedClientApplication(String appId) {
        ClientApplication clientApplication = this.findClientApplicationById(appId);
        this.validateOwnerMembership(clientApplication);
        return clientApplication;
    }

    public ClientApplication findClientApplicationById(String appId) {
        return this.clientApplicationRepository.findClientApplicationById(appId);
    }

    public void addOwnersToClientApplication(AddOwnersToClientApplicationRequestDto request) {
        ClientApplication clientApplication = this.findClientApplicationById(request.appId());
        this.validateOwnerWriteAccess(clientApplication);
        if (clientApplication.getOwners() == null) {
            clientApplication.setOwners(new ArrayList<>());
        }

        OwnerRole membershipRole = request.role() != null ? request.role() : OwnerRole.OWNER_VIEWER;
        for (Owner owner : this.resolveOwnersToAdd(request)) {
            if (clientApplication.getOwners().stream().noneMatch(existing -> existing.getOwnerId().equals(owner.getOwnerId()))) {
                clientApplication.getOwners().add(AppOwnerMembership.builder()
                        .ownerId(owner.getOwnerId())
                        .role(membershipRole)
                        .build());
            }
        }

        clientApplication.setUpdatedAt(this.dateUtils.now());
        this.clientApplicationRepository.persistOrUpdate(clientApplication);

        log.info("Owners added to client application with ID: {}", clientApplication.getAppId());
    }

    public ClientApplication removeOwnerFromClientApplication(String appId, String ownerId) {
        ClientApplication clientApplication = this.findClientApplicationById(appId);
        this.validateOwnerWriteAccess(clientApplication);
        List<AppOwnerMembership> owners = clientApplication.getOwners();
        AppOwnerMembership membership = AppOwnerAccess.membership(clientApplication, ownerId);
        if (membership == null) {
            throw new OwnerNotFoundException();
        }
        if (owners == null || owners.size() <= 1) {
            throw new BadRequestException("Cannot remove the last owner of the application.");
        }
        if (membership.getRole() == OwnerRole.OWNER_WRITER && AppOwnerAccess.writerCount(clientApplication) <= 1) {
            throw new BadRequestException("Cannot remove the last writer of the application.");
        }

        owners.removeIf(owner -> ownerId.equals(owner.getOwnerId()));

        clientApplication.setUpdatedAt(this.dateUtils.now());
        this.clientApplicationRepository.persistOrUpdate(clientApplication);
        log.info("Owner {} removed from client application {}", ownerId, appId);
        return clientApplication;
    }

    public void updateRequiredUserFields(UpdateRequiredUserFieldsRequestDto request) {
        ClientApplication clientApplication = this.findClientApplicationById(request.appId());
        this.validateOwnerWriteAccess(clientApplication);
        clientApplication.setRequiredUserFields(request.requiredUserFields() != null ? request.requiredUserFields() : new ArrayList<>());
        clientApplication.setUpdatedAt(this.dateUtils.now());

        this.clientApplicationRepository.persistOrUpdate(clientApplication);
        this.allowedOriginCache.invalidate(clientApplication.getAppId());
        log.info("Required user fields updated for client application with ID: {}", clientApplication.getAppId());
    }

    public ClientApplication updateSettings(UpdateClientApplicationSettingsRequestDto request) {
        ClientApplication clientApplication = this.findClientApplicationById(request.appId());
        this.validateOwnerWriteAccess(clientApplication);
        if (request.name() != null) {
            String name = NormalizeUtils.trimToNull(request.name());
            if (name == null) {
                throw new BadRequestException("Name cannot be blank.");
            }
            clientApplication.setName(name);
        }
        if (request.description() != null) {
            clientApplication.setDescription(NormalizeUtils.trimToNull(request.description()));
        }
        if (request.logoUrl() != null) {
            clientApplication.setLogoUrl(NormalizeUtils.trimToNull(request.logoUrl()));
        }
        if (request.emailSettings() != null) {
            clientApplication.setEmailSettings(mapEmailSettings(request.emailSettings()));
        }
        if (request.allowedOrigins() != null) {
            if (request.allowedOrigins().isEmpty()) {
                throw new BadRequestException("Allowed origins list cannot be empty.");
            }
            clientApplication.setAllowedOrigins(request.allowedOrigins());
        }
        if (request.jwtExpirationInMinutes() != null) {
            clientApplication.setJwtExpirationInMinutes(request.jwtExpirationInMinutes());
        }
        if (request.refreshTokenExpirationInDays() != null) {
            clientApplication.setRefreshTokenExpirationInDays(request.refreshTokenExpirationInDays());
        }
        if (request.googleAudience() != null) {
            clientApplication.setGoogleAudience(NormalizeUtils.trimToNull(request.googleAudience()));
        }
        if (request.requiredUserFields() != null) {
            clientApplication.setRequiredUserFields(request.requiredUserFields());
        }
        clientApplication.setUpdatedAt(this.dateUtils.now());
        this.clientApplicationRepository.persistOrUpdate(clientApplication);
        this.allowedOriginCache.invalidate(clientApplication.getAppId());
        log.info("Client application settings updated for ID: {}", clientApplication.getAppId());
        return clientApplication;
    }

    public ClientApplication disableClientApplication(String appId) {
        ClientApplication clientApplication = this.findClientApplicationById(appId);
        this.validateOwnerWriteAccess(clientApplication);
        clientApplication.setActive(false);
        clientApplication.setUpdatedAt(this.dateUtils.now());
        this.clientApplicationRepository.persistOrUpdate(clientApplication);
        this.allowedOriginCache.invalidate(clientApplication.getAppId());
        this.accountStatusCache.putApplicationDisabled(clientApplication.getAppId(), true);
        this.userRefreshTokenService.revokeAllForApp(clientApplication.getAppId());
        log.info("Client application disabled with ID: {}", appId);
        return clientApplication;
    }

    public ClientApplication enableClientApplication(String appId) {
        ClientApplication clientApplication = this.findClientApplicationById(appId);
        this.validateOwnerWriteAccess(clientApplication);
        clientApplication.setActive(true);
        clientApplication.setUpdatedAt(this.dateUtils.now());
        this.clientApplicationRepository.persistOrUpdate(clientApplication);
        this.allowedOriginCache.invalidate(clientApplication.getAppId());
        this.accountStatusCache.putApplicationDisabled(clientApplication.getAppId(), false);
        log.info("Client application enabled with ID: {}", appId);
        return clientApplication;
    }

    public ClientApplicationSecretResult rotateClientApplicationSecret() {
        if (!this.contextComponent.hasRole("APPLICATION")) {
            throw new ApplicationForbiddenException();
        }
        return this.rotateSecret(this.findClientApplicationById(this.contextComponent.getAuthenticatedAppId()));
    }

    public ClientApplicationSecretResult rotateOwnedClientApplicationSecret(String appId) {
        ClientApplication clientApplication = this.findClientApplicationById(appId);
        this.validateOwnerWriteAccess(clientApplication);
        return this.rotateSecret(clientApplication);
    }

    public ClientApplication updateOwnerRole(String appId, String ownerId, OwnerRole role) {
        ClientApplication clientApplication = this.findClientApplicationById(appId);
        this.validateOwnerWriteAccess(clientApplication);
        if (role == null) {
            throw new BadRequestException("Role is required.");
        }
        AppOwnerMembership membership = AppOwnerAccess.membership(clientApplication, ownerId);
        if (membership == null) {
            throw new OwnerNotFoundException();
        }
        if (membership.getRole() == OwnerRole.OWNER_WRITER
                && role != OwnerRole.OWNER_WRITER
                && AppOwnerAccess.writerCount(clientApplication) <= 1) {
            throw new BadRequestException("Cannot demote the last writer of the application.");
        }
        membership.setRole(role);
        clientApplication.setUpdatedAt(this.dateUtils.now());
        this.clientApplicationRepository.persistOrUpdate(clientApplication);
        log.info("Owner {} role updated to {} on client application {}", ownerId, role, appId);
        return clientApplication;
    }

    public ClientApplicationResponseDto toResponse(ClientApplication clientApplication) {
        return this.toResponse(clientApplication, null);
    }

    public ClientApplicationResponseDto toResponse(ClientApplication clientApplication, String apiSecret) {
        return ClientApplicationResponseDto.from(clientApplication, this.hydrateOwners(clientApplication), apiSecret);
    }

    public void requireAppMember(String appId) {
        this.findOwnedClientApplication(appId);
    }

    public void requireAppWriter(String appId) {
        ClientApplication clientApplication = this.findClientApplicationById(appId);
        this.validateOwnerWriteAccess(clientApplication);
    }

    private ClientApplicationSecretResult rotateSecret(ClientApplication clientApplication) {
        String apiSecret = this.keyGeneratorUtils.generateApiSecret();
        String hashedApiSecret = this.bcryptService.encryptPassword(apiSecret);

        clientApplication.setApiSecret(hashedApiSecret);
        clientApplication.setUpdatedAt(this.dateUtils.now());
        this.clientApplicationRepository.persistOrUpdate(clientApplication);

        log.info("Client application secret rotated for ID: {}", clientApplication.getAppId());
        return new ClientApplicationSecretResult(clientApplication, apiSecret);
    }

    private List<Owner> resolveOwnersToAdd(AddOwnersToClientApplicationRequestDto request) {
        Set<String> ownerIds = new LinkedHashSet<>();
        if (request.ownerIds() != null) {
            for (String ownerId : request.ownerIds()) {
                if (NormalizeUtils.trimToNull(ownerId) != null) {
                    ownerIds.add(ownerId.trim());
                }
            }
        }
        if (request.emails() != null) {
            for (String email : request.emails()) {
                String normalizedEmail = NormalizeUtils.normalizeEmail(email);
                if (normalizedEmail == null) {
                    continue;
                }
                ownerIds.add(this.ownerService.findOwnerByEmail(normalizedEmail).getOwnerId());
            }
        }
        if (ownerIds.isEmpty()) {
            throw new BadRequestException("Owner IDs or emails are required.");
        }
        return ownerIds.stream().map(this.ownerService::findOwnerById).toList();
    }

    private EmailSettings mapEmailSettings(EmailSettingsRequestDto request) {
        if (request == null) {
            return null;
        }

        return EmailSettings.builder()
                .fromEmail(request.fromEmail())
                .fromName(request.fromName())
                .replyTo(request.replyTo())
                .supportEmail(request.supportEmail())
                .supportUrl(request.supportUrl())
                .verificationRedirectUrl(request.verificationRedirectUrl())
                .passwordResetRedirectUrl(request.passwordResetRedirectUrl())
                .loginUrl(request.loginUrl())
                .build();
    }

    private void requireAuthenticatedOwner() {
        if (!this.contextComponent.isOwnerActor()) {
            throw new ApplicationForbiddenException();
        }
        this.contextComponent.getAuthenticatedOwnerId();
    }

    private void validateOwnerMembership(ClientApplication clientApplication) {
        this.requireAuthenticatedOwner();
        String authenticatedOwnerId = this.contextComponent.getAuthenticatedOwnerId();
        if (!AppOwnerAccess.isMember(clientApplication, authenticatedOwnerId)) {
            throw new ApplicationForbiddenException();
        }
    }

    private void validateOwnerWriteAccess(ClientApplication clientApplication) {
        this.requireAuthenticatedOwner();
        String authenticatedOwnerId = this.contextComponent.getAuthenticatedOwnerId();
        if (clientApplication != null && !AppOwnerAccess.isWriter(clientApplication, authenticatedOwnerId)) {
            throw new ApplicationForbiddenException();
        }
    }

    private List<AppOwnerResponseDto> hydrateOwners(ClientApplication clientApplication) {
        if (clientApplication.getOwners() == null || clientApplication.getOwners().isEmpty()) {
            return List.of();
        }
        List<AppOwnerResponseDto> owners = new ArrayList<>();
        for (AppOwnerMembership membership : clientApplication.getOwners()) {
            if (membership == null || membership.getOwnerId() == null) {
                continue;
            }
            try {
                Owner owner = this.ownerService.findOwnerById(membership.getOwnerId());
                owners.add(new AppOwnerResponseDto(owner, membership.getRole()));
            } catch (OwnerNotFoundException e) {
                owners.add(new AppOwnerResponseDto(
                        membership.getOwnerId(),
                        null,
                        null,
                        null,
                        null,
                        membership.getRole() != null ? membership.getRole() : OwnerRole.OWNER_VIEWER,
                        null,
                        null,
                        null
                ));
            }
        }
        return owners;
    }
}
