package com.mtsolutions.domain.service;

import com.mongodb.MongoException;
import com.mtsolutions.application.cache.AccountStatusCache;
import com.mtsolutions.application.common.ContextComponent;
import com.mtsolutions.application.constant.CloudinaryFolder;
import com.mtsolutions.application.service.CloudinaryService;
import com.mtsolutions.application.exception.EmailAlreadyExistsException;
import com.mtsolutions.application.exception.RequiredUserFieldMissingException;
import com.mtsolutions.application.exception.ApplicationForbiddenException;
import com.mtsolutions.application.exception.UsernameAlreadyExistsException;
import com.mtsolutions.application.utils.AccountStatusUtils;
import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.application.utils.NormalizeUtils;
import com.mtsolutions.domain.dto.request.CreateAddressRequestDto;
import com.mtsolutions.domain.constant.UserRequiredField;
import com.mtsolutions.domain.constant.ImageType;
import com.mtsolutions.domain.dto.request.CreateDocumentRequestDto;
import com.mtsolutions.domain.dto.request.CreateUserRequestDto;
import com.mtsolutions.domain.dto.request.GenerateUserGoogleTokenRequestDto;
import com.mtsolutions.domain.dto.request.RemoveUserImageRequestDto;
import com.mtsolutions.domain.dto.request.UploadUserImageRequestDto;
import com.mtsolutions.domain.entity.ClientApplication;
import com.mtsolutions.domain.entity.User;
import com.mtsolutions.domain.model.Address;
import com.mtsolutions.domain.model.Document;
import com.mtsolutions.domain.model.Email;
import com.mtsolutions.domain.model.Password;
import com.mtsolutions.domain.model.Phone;
import com.mtsolutions.domain.model.UserImage;
import com.mtsolutions.domain.repository.UserRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@ApplicationScoped
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ClientApplicationService clientApplicationService;
    private final UserRoleService userRoleService;
    private final AddressService addressService;
    private final CloudinaryService cloudinaryService;
    private final BcryptService bcryptService;
    private final PasswordPolicyService passwordPolicyService;
    private final UserRefreshTokenService userRefreshTokenService;
    private final AccountStatusCache accountStatusCache;
    private final DateUtils dateUtils;
    private final ContextComponent contextComponent;

    public UserService(UserRepository userRepository,
                       ClientApplicationService clientApplicationService,
                       UserRoleService userRoleService,
                       AddressService addressService,
                       CloudinaryService cloudinaryService,
                       BcryptService bcryptService,
                       PasswordPolicyService passwordPolicyService,
                       UserRefreshTokenService userRefreshTokenService,
                       AccountStatusCache accountStatusCache,
                       DateUtils dateUtils,
                       ContextComponent contextComponent) {
        this.userRepository = userRepository;
        this.clientApplicationService = clientApplicationService;
        this.userRoleService = userRoleService;
        this.addressService = addressService;
        this.cloudinaryService = cloudinaryService;
        this.bcryptService = bcryptService;
        this.passwordPolicyService = passwordPolicyService;
        this.userRefreshTokenService = userRefreshTokenService;
        this.accountStatusCache = accountStatusCache;
        this.dateUtils = dateUtils;
        this.contextComponent = contextComponent;
    }

    public User createUser(CreateUserRequestDto request) {
        log.info("Creating user with username: {}", request.username());
        String appId = this.contextComponent.getAuthenticatedAppId();
        this.validateApplicationAccess();

        ClientApplication clientApplication = this.clientApplicationService.findClientApplicationById(appId);
        this.validateRequiredFields(request, clientApplication.getRequiredUserFields(), false);
        this.validateUniqueUserIdentifiers(appId, request);

        User user = User.builder()
                .appId(appId)
                .name(request.name())
                .username(NormalizeUtils.trimToNull(request.username()))
                .primaryEmail(this.resolvePrimaryEmail(request.email()))
                .emails(this.buildEmails(request.email()))
                .password(this.buildPassword(request.password()))
                .phones(this.buildPhones(request.phones()))
                .document(this.fillDocumentFields(request.document()))
                .maritalStatus(request.maritalStatus())
                .roleIds(this.userRoleService.resolveRoleIdsByAppIdAndRoleNames(appId, request.roles()))
                .createdAt(this.dateUtils.now())
                .updatedAt(this.dateUtils.now())
                .active(true)
                .build();

        this.persistUser(user);
        log.info("User created with ID: {}", user.getUserId());

        return user;
    }

    public User provisionGoogleUser(String appId, String email, String googleName, GenerateUserGoogleTokenRequestDto request) {
        ClientApplication clientApplication = this.clientApplicationService.findClientApplicationById(appId);
        if (Boolean.FALSE.equals(clientApplication.getActive())) {
            throw new ApplicationForbiddenException();
        }
        if (this.userRepository.existsByAppIdAndEmail(appId, email)) {
            throw new EmailAlreadyExistsException();
        }

        String name = this.hasText(request != null ? request.name() : null)
                ? request.name().trim()
                : (this.hasText(googleName) ? googleName.trim() : email);
        String username = request != null ? NormalizeUtils.trimToNull(request.username()) : null;
        CreateUserRequestDto profile = new CreateUserRequestDto(
                name,
                username,
                List.of(email),
                null,
                request != null ? request.phones() : null,
                request != null ? request.document() : null,
                request != null ? request.maritalStatus() : null,
                null
        );
        this.validateRequiredFields(profile, clientApplication.getRequiredUserFields(), true);
        this.validateUniqueUserIdentifiers(appId, profile);

        Email googleEmail = Email.builder()
                .email(email)
                .primary(true)
                .verified(true)
                .build();

        User user = User.builder()
                .appId(appId)
                .name(name)
                .username(username)
                .primaryEmail(email)
                .emails(new ArrayList<>(List.of(googleEmail)))
                .phones(this.buildPhones(profile.phones()))
                .document(this.fillDocumentFields(profile.document()))
                .maritalStatus(profile.maritalStatus())
                .createdAt(this.dateUtils.now())
                .updatedAt(this.dateUtils.now())
                .active(true)
                .build();

        this.persistUser(user);
        log.info("Google user provisioned with ID: {}", user.getUserId());
        return user;
    }

    public User findCurrentUser() {
        if (!this.contextComponent.hasRole("USER")) {
            throw new ApplicationForbiddenException();
        }
        String userId = this.contextComponent.getAuthenticatedUserIdOrNull();
        if (userId == null || userId.isBlank()) {
            throw new ApplicationForbiddenException();
        }
        return this.findUserById(userId);
    }

    public User findUserById(String userId) {
        User user = this.userRepository.findUserById(userId);
        this.validateAppOrSelfAccess(user);
        return user;
    }

    public User disableUser(String userId) {
        this.validateApplicationAccess();
        User user = this.userRepository.findUserById(userId);
        this.validateAppOrSelfAccess(user);
        if (!AccountStatusUtils.isDisabled(user)) {
            user.setActive(false);
            user.setDisabledAt(this.dateUtils.now());
            user.setUpdatedAt(this.dateUtils.now());
            this.userRepository.persistOrUpdate(user);
            this.userRefreshTokenService.revokeAllForUser(user.getUserId(), user.getAppId());
            this.accountStatusCache.putUserDisabled(user.getUserId(), true);
            log.info("User disabled with ID: {}", userId);
        }
        return user;
    }

    public User enableUser(String userId) {
        this.validateApplicationAccess();
        User user = this.userRepository.findUserById(userId);
        this.validateAppOrSelfAccess(user);
        user.setActive(true);
        user.setDisabledAt(null);
        user.setUpdatedAt(this.dateUtils.now());
        this.userRepository.persistOrUpdate(user);
        this.accountStatusCache.putUserDisabled(user.getUserId(), false);
        log.info("User enabled with ID: {}", userId);
        return user;
    }

    public User attachAddressToUser(String userId, CreateAddressRequestDto request) {
        User user = this.userRepository.findUserById(userId);
        this.validateAppOrSelfAccess(user);
        Address resolvedAddress = this.addressService.resolveAddress(request);

        if (user.getAddresses() == null) {
            user.setAddresses(new ArrayList<>());
        }

        user.getAddresses().add(resolvedAddress);
        user.setUpdatedAt(this.dateUtils.now());
        this.userRepository.persistOrUpdate(user);
        log.info("Address attached to user with ID: {}", userId);

        return user;
    }

    public void removeAddressFromUser(String userId, Integer addressIndex) {
        User user = this.userRepository.findUserById(userId);
        this.validateAppOrSelfAccess(user);
        List<Address> addresses = user.getAddresses();

        if (addresses == null || addresses.isEmpty()) {
            throw new BadRequestException("User has no addresses to remove.");
        }
        if (addressIndex == null || addressIndex < 0 || addressIndex >= addresses.size()) {
            throw new BadRequestException("Address index is out of range.");
        }

        addresses.remove(addressIndex.intValue());
        user.setUpdatedAt(this.dateUtils.now());
        this.userRepository.persistOrUpdate(user);
        log.info("Address at index {} removed from user with ID: {}", addressIndex, userId);
    }

    public User uploadUserImage(UploadUserImageRequestDto request) {
        User user = this.userRepository.findUserById(request.userId());
        this.validateAppOrSelfAccess(user);
        this.validateImageRequest(request);

        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(request.uploadedFilePath());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded image", e);
        }

        CloudinaryFolder folder = this.resolveCloudinaryFolder(request.imageType());
        String publicId = this.buildImagePublicId(request.userId(), request.imageType());
        String imageUrl = this.cloudinaryService.upload(fileBytes, publicId, folder);

        UserImage uploadedImage = UserImage.builder()
                .imageUrl(imageUrl)
                .imageType(request.imageType())
                .fileName(request.fileName())
                .sizeInBytes(request.sizeInBytes())
                .verified(false)
                .uploadedAt(this.dateUtils.now())
                .build();

        if (user.getImages() == null) {
            user.setImages(new ArrayList<>());
        }

        this.replaceImage(user, uploadedImage);
        user.setUpdatedAt(this.dateUtils.now());
        this.userRepository.persistOrUpdate(user);

        log.info("Image uploaded for user with ID: {}, type: {}", user.getUserId(), request.imageType());
        return user;
    }

    public User removeUserImage(RemoveUserImageRequestDto request) {
        User user = this.userRepository.findUserById(request.userId());
        this.validateAppOrSelfAccess(user);

        if (user.getImages() == null || user.getImages().isEmpty()) {
            throw new BadRequestException("User has no images to remove.");
        }

        boolean removed = false;
        Iterator<UserImage> iterator = user.getImages().iterator();
        while (iterator.hasNext()) {
            UserImage image = iterator.next();
            if (image != null && image.getImageType() == request.imageType()) {
                this.deleteCloudinaryImage(image);
                iterator.remove();
                removed = true;
            }
        }

        if (!removed) {
            throw new BadRequestException("Image type not found for user.");
        }

        user.setUpdatedAt(this.dateUtils.now());
        this.userRepository.persistOrUpdate(user);
        log.info("Image removed for user with ID: {}, type: {}", user.getUserId(), request.imageType());
        return user;
    }

    private void validateApplicationAccess() {
        if (!this.contextComponent.hasRole("APPLICATION")) {
            throw new ApplicationForbiddenException();
        }
    }

    private void validateAppOrSelfAccess(User user) {
        this.contextComponent.validateAppOrSelfAccess(user.getAppId(), user.getUserId());
    }

    private void validateImageRequest(UploadUserImageRequestDto request) {
        if (request.imageType() == null) {
            throw new BadRequestException("Image type is required.");
        }
        if (request.uploadedFilePath() == null) {
            throw new BadRequestException("Image file is required.");
        }
        if (request.contentType() == null || !request.contentType().startsWith("image/")) {
            throw new BadRequestException("Only image uploads are supported.");
        }
    }

    private void replaceImage(User user, UserImage uploadedImage) {
        user.getImages().removeIf(image -> image != null && image.getImageType() == uploadedImage.getImageType());
        user.getImages().add(uploadedImage);
    }

    private void deleteCloudinaryImage(UserImage image) {
        String publicId = this.cloudinaryService.extractPublicId(image.getImageUrl());
        if (publicId != null) {
            this.cloudinaryService.delete(publicId, "image");
        }
    }

    private CloudinaryFolder resolveCloudinaryFolder(ImageType imageType) {
        return switch (imageType) {
            case PROFILE, SELFIE_KYC -> CloudinaryFolder.USER_PICTURE;
            case DOCUMENT_FRONT, DOCUMENT_BACK, PROOF_OF_ADDRESS, OTHER -> CloudinaryFolder.USER_DOCUMENT;
        };
    }

    private String buildImagePublicId(String userId, ImageType imageType) {
        return userId + "/" + imageType.name().toLowerCase();
    }


    private void persistUser(User user) {
        try {
            this.userRepository.persist(user);
        } catch (MongoException e) {
            if (e.getCode() == 11000) {
                String message = e.getMessage() != null ? e.getMessage() : "";
                if (message.contains("username")) {
                    throw new UsernameAlreadyExistsException();
                }
                throw new EmailAlreadyExistsException();
            }
            throw e;
        }
    }

    private void validateUniqueUserIdentifiers(String appId, CreateUserRequestDto request) {
        List<String> normalizedEmails = this.normalizeEmails(request.email());
        Set<String> uniqueEmails = new HashSet<>();
        for (String email : normalizedEmails) {
            if (!uniqueEmails.add(email) || this.userRepository.existsByAppIdAndEmail(appId, email)) {
                throw new EmailAlreadyExistsException();
            }
        }

        String username = NormalizeUtils.trimToNull(request.username());
        if (username != null && this.userRepository.existsByAppIdAndUsername(appId, username)) {
            throw new UsernameAlreadyExistsException();
        }
    }

    private List<String> normalizeEmails(List<String> emails) {
        if (emails == null || emails.isEmpty()) {
            return new ArrayList<>();
        }

        return emails.stream()
                .map(NormalizeUtils::normalizeEmail)
                .filter(Objects::nonNull)
                .toList();
    }

    private void validateRequiredFields(CreateUserRequestDto request, List<UserRequiredField> requiredFields, boolean googleSignup) {
        if (requiredFields == null || requiredFields.isEmpty()) {
            return;
        }

        for (UserRequiredField field : requiredFields) {
            boolean missing = switch (field) {
                case NAME -> !this.hasText(request.name());
                case USERNAME -> !this.hasText(request.username());
                case EMAIL -> request.email() == null || request.email().isEmpty() || request.email().stream().noneMatch(this::hasText);
                case PASSWORD -> !googleSignup && !this.hasText(request.password());
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
        List<String> normalizedEmails = this.normalizeEmails(emails);
        if (normalizedEmails.isEmpty()) {
            return new ArrayList<>();
        }

        final boolean[] primaryAssigned = {false};
        return normalizedEmails.stream()
                .map(email -> {
                    boolean isPrimary = !primaryAssigned[0];
                    primaryAssigned[0] = true;

                    return Email.builder()
                            .email(email)
                            .primary(isPrimary)
                            .verified(false)
                            .build();
                })
                .toList();
    }

    private String resolvePrimaryEmail(List<String> emails) {
        return this.normalizeEmails(emails).stream()
                .findFirst()
                .orElse(null);
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

        this.passwordPolicyService.validate(rawPassword);
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
