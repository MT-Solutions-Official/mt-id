package com.mtsolutions.domain.service;

import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.domain.dto.CreateApplicationOwnerRequestDto;
import com.mtsolutions.domain.dto.CreateDocumentRequestDto;
import com.mtsolutions.domain.entity.ApplicationOwner;
import com.mtsolutions.domain.model.Document;
import com.mtsolutions.domain.model.Email;
import com.mtsolutions.domain.model.Password;
import com.mtsolutions.domain.model.Phone;
import com.mtsolutions.domain.repository.ApplicationOwnerRepository;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class ApplicationOwnerService {

    private final ApplicationOwnerRepository applicationOwnerRepository;
    private final BcryptService bcryptService;
    private final DateUtils dateUtils;

    public ApplicationOwnerService(ApplicationOwnerRepository applicationOwnerRepository, BcryptService bcryptService, DateUtils dateUtils) {
        this.applicationOwnerRepository = applicationOwnerRepository;
        this.bcryptService = bcryptService;
        this.dateUtils = dateUtils;
    }

    public ApplicationOwner createApplicationOwner(CreateApplicationOwnerRequestDto request) {
        log.info("Creating application owner with name: {}", request.name());

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

        ApplicationOwner applicationOwner = ApplicationOwner.builder()
                .name(request.name())
                .document(this.fillDocumentFields(request.document()))
                .email(email)
                .phone(phone)
                .password(password)
                .createdAt(this.dateUtils.now())
                .updatedAt(this.dateUtils.now())
                .active(true)
                .build();

        this.applicationOwnerRepository.persist(applicationOwner);
        log.info("Application owner created with ID: {}", applicationOwner.getOwnerId());

        return applicationOwner;
    }

    public ApplicationOwner findApplicationOwnerById(String ownerId) {
        return this.applicationOwnerRepository.findOwnerById(ownerId);
    }

    public Boolean existsApplicationOwnerById(String ownerId) {
        return this.applicationOwnerRepository.existsByOwnerId(ownerId);
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