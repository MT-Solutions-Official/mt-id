package com.mtsolutions.domain.service;

import com.mongodb.MongoException;
import com.mtsolutions.application.cache.AccountStatusCache;
import com.mtsolutions.application.common.ContextComponent;
import com.mtsolutions.application.exception.ApplicationForbiddenException;
import com.mtsolutions.application.exception.EmailAlreadyExistsException;
import com.mtsolutions.application.exception.OwnerNotFoundException;
import com.mtsolutions.application.utils.AccountStatusUtils;
import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.application.utils.NormalizeUtils;
import com.mtsolutions.domain.constant.OwnerRole;
import com.mtsolutions.domain.dto.request.CreateDocumentRequestDto;
import com.mtsolutions.domain.dto.request.CreateOwnerRequestDto;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.model.Document;
import com.mtsolutions.domain.model.Email;
import com.mtsolutions.domain.model.Password;
import com.mtsolutions.domain.model.Phone;
import com.mtsolutions.domain.repository.OwnerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@ApplicationScoped
@Slf4j
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final BcryptService bcryptService;
    private final PasswordPolicyService passwordPolicyService;
    private final OwnerRefreshTokenService ownerRefreshTokenService;
    private final AccountStatusCache accountStatusCache;
    private final DateUtils dateUtils;
    private final ContextComponent contextComponent;

    public OwnerService(OwnerRepository ownerRepository,
                        BcryptService bcryptService,
                        PasswordPolicyService passwordPolicyService,
                        OwnerRefreshTokenService ownerRefreshTokenService,
                        AccountStatusCache accountStatusCache,
                        DateUtils dateUtils,
                        ContextComponent contextComponent) {
        this.ownerRepository = ownerRepository;
        this.bcryptService = bcryptService;
        this.passwordPolicyService = passwordPolicyService;
        this.ownerRefreshTokenService = ownerRefreshTokenService;
        this.accountStatusCache = accountStatusCache;
        this.dateUtils = dateUtils;
        this.contextComponent = contextComponent;
    }

    public Owner createOwner(CreateOwnerRequestDto request) {
        log.info("Creating owner with name: {}", request.name());
        boolean bootstrapping = this.ownerRepository.count() == 0;
        this.validateOwnerCreationAccess(bootstrapping);

        String normalizedEmail = NormalizeUtils.normalizeEmail(request.email());
        if (this.ownerRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException();
        }

        this.passwordPolicyService.validate(request.password());
        String hashedPassword = this.bcryptService.encryptPassword(request.password());

        Email email = Email.builder()
                .email(normalizedEmail)
                .verified(bootstrapping)
                .build();

        Phone phone = Phone.builder()
                .phoneNumber(request.phoneNumber())
                .verified(false)
                .build();

        Password password = Password.builder()
                .password(hashedPassword)
                .build();

        Owner owner = Owner.builder()
                .name(request.name())
                .document(this.fillDocumentFields(request.document()))
                .email(email)
                .phone(phone)
                .password(password)
                .role(this.resolveCreatedOwnerRole(bootstrapping, request.role()))
                .createdAt(this.dateUtils.now())
                .updatedAt(this.dateUtils.now())
                .active(true)
                .build();

        this.persistOwner(owner);
        log.info("Owner created with ID: {}", owner.getOwnerId());

        return owner;
    }

    public Owner findOwnerById(String ownerId) {
        return this.ownerRepository.findOwnerById(ownerId);
    }

    public Owner findCurrentOwner() {
        String ownerId = this.contextComponent.getAuthenticatedOwnerId();
        return this.findOwnerById(ownerId);
    }

    public List<Owner> listOwners() {
        this.requireAuthenticatedOwner();
        return this.ownerRepository.listAll();
    }

    public Owner findOwnerForConsole(String ownerId) {
        this.requireAuthenticatedOwner();
        return this.findOwnerById(ownerId);
    }

    public Owner disableOwner(String ownerId) {
        if (!this.contextComponent.hasRole("OWNER_WRITER")) {
            throw new ApplicationForbiddenException();
        }
        String authenticatedOwnerId = this.contextComponent.getAuthenticatedOwnerId();
        if (authenticatedOwnerId.equals(ownerId)) {
            throw new ApplicationForbiddenException();
        }
        Owner owner = this.findOwnerById(ownerId);
        if (!AccountStatusUtils.isDisabled(owner)) {
            owner.setActive(false);
            owner.setDisabledAt(this.dateUtils.now());
            owner.setUpdatedAt(this.dateUtils.now());
            this.ownerRepository.persistOrUpdate(owner);
            this.ownerRefreshTokenService.revokeAllForOwner(owner.getOwnerId());
            this.accountStatusCache.putOwnerDisabled(owner.getOwnerId(), true);
            log.info("Owner disabled with ID: {}", ownerId);
        }
        return owner;
    }

    public Owner enableOwner(String ownerId) {
        if (!this.contextComponent.hasRole("OWNER_WRITER")) {
            throw new ApplicationForbiddenException();
        }
        Owner owner = this.findOwnerById(ownerId);
        owner.setActive(true);
        owner.setDisabledAt(null);
        owner.setUpdatedAt(this.dateUtils.now());
        this.ownerRepository.persistOrUpdate(owner);
        this.accountStatusCache.putOwnerDisabled(owner.getOwnerId(), false);
        log.info("Owner enabled with ID: {}", ownerId);
        return owner;
    }

    public Boolean existsOwnerById(String ownerId) {
        return this.ownerRepository.existsByOwnerId(ownerId);
    }

    public Owner findOwnerByEmail(String email) {
        String normalizedEmail = NormalizeUtils.normalizeEmail(email);
        if (normalizedEmail == null) {
            throw new OwnerNotFoundException();
        }
        return this.ownerRepository.findOwnerByEmail(normalizedEmail)
                .orElseThrow(OwnerNotFoundException::new);
    }

    private void requireAuthenticatedOwner() {
        if (!this.contextComponent.hasRole("OWNER_WRITER") && !this.contextComponent.hasRole("OWNER_VIEWER")) {
            throw new ApplicationForbiddenException();
        }
        this.contextComponent.getAuthenticatedOwnerId();
    }

    private void validateOwnerCreationAccess(boolean bootstrapping) {
        if (bootstrapping) {
            return;
        }
        if (!this.contextComponent.hasRole("OWNER_WRITER")) {
            throw new ApplicationForbiddenException();
        }
    }

    private OwnerRole resolveCreatedOwnerRole(boolean bootstrapping, OwnerRole requestedRole) {
        if (bootstrapping) {
            return OwnerRole.OWNER_WRITER;
        }
        return requestedRole != null ? requestedRole : OwnerRole.OWNER_VIEWER;
    }

    private void persistOwner(Owner owner) {
        try {
            this.ownerRepository.persist(owner);
        } catch (MongoException e) {
            if (e.getCode() == 11000) {
                throw new EmailAlreadyExistsException();
            }
            throw e;
        }
    }

    private Document fillDocumentFields(CreateDocumentRequestDto request) {
        if (request == null) {
            return null;
        }

        Document document = new Document();
        if (hasText(request.cpf())) document.setCpf(request.cpf());
        if (hasText(request.rg())) document.setRg(request.rg());
        if (hasText(request.cnpj())) document.setCnpj(request.cnpj());
        if (hasText(request.cnh())) document.setCnh(request.cnh());
        if (hasText(request.nik())) document.setNik(request.nik());
        if (hasText(request.npwp())) document.setNpwp(request.npwp());
        if (hasText(request.sim())) document.setSim(request.sim());
        if (hasText(request.ssn())) document.setSsn(request.ssn());
        if (hasText(request.ein())) document.setEin(request.ein());
        if (hasText(request.usDriverLicense())) document.setUsDriverLicense(request.usDriverLicense());
        if (hasText(request.nif())) document.setNif(request.nif());
        if (hasText(request.niss())) document.setNiss(request.niss());
        if (hasText(request.cc())) document.setCc(request.cc());
        if (hasText(request.passport())) document.setPassport(request.passport());

        return document;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
