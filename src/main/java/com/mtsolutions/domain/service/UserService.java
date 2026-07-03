package com.mtsolutions.domain.service;

import com.mtsolutions.application.exception.RequiredUserFieldMissingException;
import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.domain.constant.UserRequiredField;
import com.mtsolutions.domain.dto.CreateDocumentRequestDto;
import com.mtsolutions.domain.dto.CreateUserRequestDto;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.entity.User;
import com.mtsolutions.domain.model.Document;
import com.mtsolutions.domain.model.Email;
import com.mtsolutions.domain.model.Password;
import com.mtsolutions.domain.model.Phone;
import com.mtsolutions.domain.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@ApplicationScoped
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ClientApplicationService clientApplicationService;
    private final UserRoleService userRoleService;
    private final BcryptService bcryptService;
    private final DateUtils dateUtils;

    public UserService(UserRepository userRepository, ClientApplicationService clientApplicationService, UserRoleService userRoleService, BcryptService bcryptService, DateUtils dateUtils) {
        this.userRepository = userRepository;
        this.clientApplicationService = clientApplicationService;
        this.userRoleService = userRoleService;
        this.bcryptService = bcryptService;
        this.dateUtils = dateUtils;
    }

    public User createUser(CreateUserRequestDto request) {
        log.info("Creating user with username: {}", request.username());

        ClientApplication clientApplication = this.clientApplicationService.findClientApplicationById(request.appId());
        this.validateRequiredFields(request, clientApplication.getRequiredUserFields());

        User user = User.builder()
                .appId(request.appId())
                .name(request.name())
                .username(request.username())
                .emails(this.buildEmails(request.email()))
                .password(this.buildPassword(request.password()))
                .phones(this.buildPhones(request.phones()))
                .document(this.fillDocumentFields(request.document()))
                .maritalStatus(request.maritalStatus())
                .roleIds(this.userRoleService.resolveRoleIdsByAppIdAndRoleNames(request.appId(), request.roles()))
                .createdAt(this.dateUtils.now())
                .updatedAt(this.dateUtils.now())
                .active(true)
                .build();

        this.userRepository.persist(user);
        log.info("User created with ID: {}", user.getUserId());

        return user;
    }







    private void validateRequiredFields(CreateUserRequestDto request, List<UserRequiredField> requiredFields) {
        if (requiredFields == null || requiredFields.isEmpty()) {
            return;
        }

        for (UserRequiredField field : requiredFields) {
            boolean missing = switch (field) {
                case NAME -> !this.hasText(request.name());
                case USERNAME -> !this.hasText(request.username());
                case EMAIL -> request.email() == null || request.email().isEmpty() || request.email().stream().noneMatch(this::hasText);
                case PASSWORD -> !this.hasText(request.password());
                case PHONE -> request.phones() == null || request.phones().isEmpty() || request.phones().stream()
                        .filter(Objects::nonNull)
                        .noneMatch(phone -> this.hasText(phone.getPhoneNumber()));
                case DOCUMENT -> !this.hasDocumentData(request.document());
                case MARITAL_STATUS -> request.maritalStatus() == null;
            };

            if (missing) {
                throw new RequiredUserFieldMissingException(field);
            }
        }
    }

    private List<Email> buildEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return new ArrayList<>();
        }

        return emails.stream()
                .filter(this::hasText)
                .map(email -> Email.builder()
                        .email(email)
                        .verified(false)
                        .build())
                .toList();
    }

    private List<Phone> buildPhones(List<Phone> phones) {
        if (phones == null || phones.isEmpty()) {
            return new ArrayList<>();
        }

        return phones.stream()
                .filter(phone -> phone != null && this.hasText(phone.getPhoneNumber()))
                .map(phone -> Phone.builder()
                        .phoneNumber(phone.getPhoneNumber())
                        .verified(false)
                        .build())
                .toList();
    }

    private Password buildPassword(String rawPassword) {
        if (!this.hasText(rawPassword)) {
            return null;
        }

        String encryptedPassword = this.bcryptService.encryptPassword(rawPassword);
        return Password.builder()
                .password(encryptedPassword)
                .build();
    }

    private Document fillDocumentFields(CreateDocumentRequestDto request) {
        if (request == null) {
            return null;
        }

        Document document = new Document();
        if (this.hasText(request.cpf())) document.setCpf(request.cpf());
        if (this.hasText(request.rg())) document.setRg(request.rg());
        if (this.hasText(request.cnpj())) document.setCnpj(request.cnpj());
        if (this.hasText(request.cnh())) document.setCnh(request.cnh());
        if (this.hasText(request.nik())) document.setNik(request.nik());
        if (this.hasText(request.npwp())) document.setNpwp(request.npwp());
        if (this.hasText(request.sim())) document.setSim(request.sim());
        if (this.hasText(request.ssn())) document.setSsn(request.ssn());
        if (this.hasText(request.ein())) document.setEin(request.ein());
        if (this.hasText(request.usDriverLicense())) document.setUsDriverLicense(request.usDriverLicense());
        if (this.hasText(request.nif())) document.setNif(request.nif());
        if (this.hasText(request.niss())) document.setNiss(request.niss());
        if (this.hasText(request.cc())) document.setCc(request.cc());
        if (this.hasText(request.passport())) document.setPassport(request.passport());

        return this.hasDocumentData(request) ? document : null;
    }

    private boolean hasDocumentData(CreateDocumentRequestDto request) {
        if (request == null) {
            return false;
        }

        return Stream.of(
                        request.cpf(),
                        request.rg(),
                        request.cnpj(),
                        request.cnh(),
                        request.nik(),
                        request.npwp(),
                        request.sim(),
                        request.ssn(),
                        request.ein(),
                        request.usDriverLicense(),
                        request.nif(),
                        request.niss(),
                        request.cc(),
                        request.passport()
                )
                .anyMatch(this::hasText);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
