package com.mtsolutions.domain.service;

import com.mtsolutions.application.common.ContextComponent;
import com.mtsolutions.application.constant.CloudinaryFolder;
import com.mtsolutions.application.service.CloudinaryService;
import com.mtsolutions.application.exception.RequiredUserFieldMissingException;
import com.mtsolutions.application.exception.ApplicationForbiddenException;
import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.domain.dto.request.CreateAddressRequestDto;
import com.mtsolutions.domain.constant.UserRequiredField;
import com.mtsolutions.domain.constant.ImageType;
import com.mtsolutions.domain.dto.request.CreateDocumentRequestDto;
import com.mtsolutions.domain.dto.request.CreateUserRequestDto;
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
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
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
    private final DateUtils dateUtils;
    private final ContextComponent contextComponent;

    public UserService(UserRepository userRepository, ClientApplicationService clientApplicationService, UserRoleService userRoleService, AddressService addressService, CloudinaryService cloudinaryService, BcryptService bcryptService, DateUtils dateUtils, ContextComponent contextComponent) {
        this.userRepository = userRepository;
        this.clientApplicationService = clientApplicationService;
        this.userRoleService = userRoleService;
        this.addressService = addressService;
        this.cloudinaryService = cloudinaryService;
        this.bcryptService = bcryptService;
        this.dateUtils = dateUtils;
        this.contextComponent = contextComponent;
    }

    public User createUser(CreateUserRequestDto request) {
        log.info("Creating user with username: {}", request.username());
        this.validateAppAccess(request.appId());

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

    public User attachAddressToUser(String userId, CreateAddressRequestDto request) {
        User user = this.userRepository.findUserById(userId);
        this.validateAppAccess(user.getAppId());
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
        this.validateAppAccess(user.getAppId());
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
        this.validateAppAccess(user.getAppId());
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
        this.validateAppAccess(user.getAppId());

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

    private void validateAppAccess(String userAppId) {
        String authenticatedAppId = this.contextComponent.getAuthenticatedAppId();
        if (authenticatedAppId == null || authenticatedAppId.isBlank() || !authenticatedAppId.equals(userAppId)) {
            throw new ApplicationForbiddenException();
        }
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
            case PROFILE -> CloudinaryFolder.USER_PICTURE;
            default -> CloudinaryFolder.USER_PICTURE;
        };
    }

    private String buildImagePublicId(String userId, ImageType imageType) {
        return userId + "/" + imageType.name().toLowerCase();
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
