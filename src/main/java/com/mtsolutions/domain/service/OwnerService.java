package com.mtsolutions.domain.service;

import com.mongodb.MongoException;
import com.mtsolutions.application.common.ContextComponent;
import com.mtsolutions.application.constant.CloudinaryFolder;
import com.mtsolutions.application.exception.EmailAlreadyExistsException;
import com.mtsolutions.application.exception.OwnerNotFoundException;
import com.mtsolutions.application.service.CloudinaryService;
import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.application.utils.NormalizeUtils;
import com.mtsolutions.domain.constant.ImageType;
import com.mtsolutions.domain.dto.request.CreateAddressRequestDto;
import com.mtsolutions.domain.dto.request.CreateDocumentRequestDto;
import com.mtsolutions.domain.dto.request.CreateOwnerRequestDto;
import com.mtsolutions.domain.dto.request.UpdateOwnerRequestDto;
import com.mtsolutions.domain.dto.request.UploadOwnerImageRequestDto;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.model.Address;
import com.mtsolutions.domain.model.Document;
import com.mtsolutions.domain.model.Email;
import com.mtsolutions.domain.model.Password;
import com.mtsolutions.domain.model.Phone;
import com.mtsolutions.domain.model.UserImage;
import com.mtsolutions.domain.repository.OwnerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ApplicationScoped
@Slf4j
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final BcryptService bcryptService;
    private final PasswordPolicyService passwordPolicyService;
    private final DateUtils dateUtils;
    private final ContextComponent contextComponent;
    private final AddressService addressService;
    private final CloudinaryService cloudinaryService;

    public OwnerService(OwnerRepository ownerRepository,
                        BcryptService bcryptService,
                        PasswordPolicyService passwordPolicyService,
                        DateUtils dateUtils,
                        ContextComponent contextComponent,
                        AddressService addressService,
                        CloudinaryService cloudinaryService) {
        this.ownerRepository = ownerRepository;
        this.bcryptService = bcryptService;
        this.passwordPolicyService = passwordPolicyService;
        this.dateUtils = dateUtils;
        this.contextComponent = contextComponent;
        this.addressService = addressService;
        this.cloudinaryService = cloudinaryService;
    }

    public Owner createOwner(CreateOwnerRequestDto request) {
        log.info("Creating owner with name: {}", request.name());
        boolean bootstrapping = this.ownerRepository.count() == 0;

        String normalizedEmail = NormalizeUtils.normalizeEmail(request.email());
        if (this.ownerRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException();
        }

        this.passwordPolicyService.validate(request.password());
        String hashedPassword = this.bcryptService.encryptPassword(request.password());
        Address resolvedAddress = this.addressService.resolveAddress(request.address());

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
                .addresses(new ArrayList<>(List.of(resolvedAddress)))
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

    public Owner updateCurrentOwner(UpdateOwnerRequestDto request) {
        Owner owner = this.findCurrentOwner();
        if (hasText(request.name())) {
            owner.setName(request.name().trim());
        }
        if (hasText(request.phoneNumber())) {
            String nextPhone = request.phoneNumber().trim();
            Phone phone = owner.getPhone() != null ? owner.getPhone() : Phone.builder().build();
            if (!nextPhone.equals(phone.getPhoneNumber())) {
                phone.setVerified(false);
            }
            phone.setPhoneNumber(nextPhone);
            owner.setPhone(phone);
        }
        if (request.document() != null) {
            owner.setDocument(this.mergeDocument(owner.getDocument(), request.document()));
        }
        owner.setUpdatedAt(this.dateUtils.now());
        this.ownerRepository.persistOrUpdate(owner);
        log.info("Owner profile updated with ID: {}", owner.getOwnerId());
        return owner;
    }

    public Owner attachAddressToCurrentOwner(CreateAddressRequestDto request) {
        Owner owner = this.findCurrentOwner();
        Address resolvedAddress = this.addressService.resolveAddress(request);
        if (owner.getAddresses() == null) {
            owner.setAddresses(new ArrayList<>());
        }
        owner.getAddresses().add(resolvedAddress);
        owner.setUpdatedAt(this.dateUtils.now());
        this.ownerRepository.persistOrUpdate(owner);
        log.info("Address attached to owner with ID: {}", owner.getOwnerId());
        return owner;
    }

    public Owner removeAddressFromCurrentOwner(Integer addressIndex) {
        Owner owner = this.findCurrentOwner();
        List<Address> addresses = owner.getAddresses();
        if (addresses == null || addresses.isEmpty()) {
            throw new BadRequestException("Owner has no addresses to remove.");
        }
        if (addressIndex == null || addressIndex < 0 || addressIndex >= addresses.size()) {
            throw new BadRequestException("Address index is out of range.");
        }
        addresses.remove(addressIndex.intValue());
        owner.setUpdatedAt(this.dateUtils.now());
        this.ownerRepository.persistOrUpdate(owner);
        log.info("Address at index {} removed from owner with ID: {}", addressIndex, owner.getOwnerId());
        return owner;
    }

    public Owner uploadCurrentOwnerImage(UploadOwnerImageRequestDto request) {
        Owner owner = this.findCurrentOwner();
        if (request.imageType() == null) {
            throw new BadRequestException("Image type is required.");
        }
        if (request.uploadedFilePath() == null) {
            throw new BadRequestException("Image file is required.");
        }
        if (request.contentType() == null || !request.contentType().startsWith("image/")) {
            throw new BadRequestException("Only image uploads are supported.");
        }

        byte[] fileBytes;
        try {
            fileBytes = Files.readAllBytes(request.uploadedFilePath());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded image", e);
        }

        String publicId = owner.getOwnerId() + "/" + request.imageType().name().toLowerCase();
        String imageUrl = this.cloudinaryService.upload(fileBytes, publicId, CloudinaryFolder.OWNER_PICTURE);
        UserImage uploadedImage = UserImage.builder()
                .imageUrl(imageUrl)
                .imageType(request.imageType())
                .fileName(request.fileName())
                .sizeInBytes(request.sizeInBytes())
                .verified(false)
                .uploadedAt(this.dateUtils.now())
                .build();

        if (owner.getImages() == null) {
            owner.setImages(new ArrayList<>());
        }
        owner.getImages().removeIf(image -> image != null && image.getImageType() == uploadedImage.getImageType());
        owner.getImages().add(uploadedImage);
        owner.setUpdatedAt(this.dateUtils.now());
        this.ownerRepository.persistOrUpdate(owner);
        log.info("Image uploaded for owner with ID: {}, type: {}", owner.getOwnerId(), request.imageType());
        return owner;
    }

    public Owner removeCurrentOwnerImage(ImageType imageType) {
        Owner owner = this.findCurrentOwner();
        if (owner.getImages() == null || owner.getImages().isEmpty()) {
            throw new BadRequestException("Owner has no images to remove.");
        }
        boolean removed = false;
        Iterator<UserImage> iterator = owner.getImages().iterator();
        while (iterator.hasNext()) {
            UserImage image = iterator.next();
            if (image != null && image.getImageType() == imageType) {
                String publicId = this.cloudinaryService.extractPublicId(image.getImageUrl());
                if (publicId != null) {
                    this.cloudinaryService.delete(publicId, "image");
                }
                iterator.remove();
                removed = true;
            }
        }
        if (!removed) {
            throw new BadRequestException("Image type not found for owner.");
        }
        owner.setUpdatedAt(this.dateUtils.now());
        this.ownerRepository.persistOrUpdate(owner);
        log.info("Image removed for owner with ID: {}, type: {}", owner.getOwnerId(), imageType);
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

    private Document mergeDocument(Document current, CreateDocumentRequestDto request) {
        Document document = current != null ? current : new Document();
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
