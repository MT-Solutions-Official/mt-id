package com.mtsolutions.domain.service;

import com.mtsolutions.application.exception.OwnerNotFoundException;
import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.domain.constant.OwnerRole;
import com.mtsolutions.domain.dto.request.CreateDocumentRequestDto;
import com.mtsolutions.domain.dto.request.CreateOwnerRequestDto;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.model.Document;
import com.mtsolutions.domain.model.Email;
import com.mtsolutions.domain.model.Password;
import com.mtsolutions.domain.model.Phone;
import com.mtsolutions.domain.repository.ClientApplicationRepository;
import com.mtsolutions.domain.repository.OwnerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class OwnerService {

    private final OwnerRepository ownerRepository;
    private final BcryptService bcryptService;
    private final DateUtils dateUtils;

    public OwnerService(OwnerRepository ownerRepository, BcryptService bcryptService, DateUtils dateUtils) {
        this.ownerRepository = ownerRepository;
        this.bcryptService = bcryptService;
        this.dateUtils = dateUtils;
    }

    public Owner createOwner(CreateOwnerRequestDto request) {
        log.info("Creating owner with name: {}", request.name());

        String hashedPassword = this.bcryptService.encryptPassword(request.password());

        Email email = Email.builder()
                .email(request.email())
                .verified(false)
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
                .role(OwnerRole.OWNER_WRITER)
                .createdAt(this.dateUtils.now())
                .updatedAt(this.dateUtils.now())
                .active(true)
                .build();

        this.ownerRepository.persist(owner);
        log.info("Owner created with ID: {}", owner.getOwnerId());

        return owner;
    }

    public Owner findOwnerById(String ownerId) {
        return this.ownerRepository.findOwnerById(ownerId);
    }

    public Boolean existsOwnerById(String ownerId) {
        return this.ownerRepository.existsByOwnerId(ownerId);
    }

    public Owner findOwnerByEmail(String email) {
        return this.ownerRepository.findOwnerByEmail(email)
                .orElseThrow(OwnerNotFoundException::new);
    }

    private Document fillDocumentFields(CreateDocumentRequestDto request) {
        Document document = new Document();
        if (request.cpf() != null) document.setCpf(request.cpf());
        if (request.rg() != null) document.setRg(request.rg());
        if (request.cnpj() != null) document.setCnpj(request.cnpj());
        if (request.cnh() != null) document.setCnh(request.cnh());
        if (request.nik() != null) document.setNik(request.nik());
        if (request.npwp() != null) document.setNpwp(request.npwp());
        if (request.sim() != null) document.setSim(request.sim());
        if (request.ssn() != null) document.setSsn(request.ssn());
        if (request.ein() != null) document.setEin(request.ein());
        if (request.usDriverLicense() != null) document.setUsDriverLicense(request.usDriverLicense());
        if (request.nif() != null) document.setNif(request.nif());
        if (request.niss() != null) document.setNiss(request.niss());
        if (request.cc() != null) document.setCc(request.cc());
        if (request.passport() != null) document.setPassport(request.passport());

        return document;
    }
}