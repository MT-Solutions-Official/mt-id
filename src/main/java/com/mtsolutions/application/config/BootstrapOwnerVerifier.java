package com.mtsolutions.application.config;

import com.mtsolutions.application.utils.DateUtils;
import com.mtsolutions.domain.entity.Owner;
import com.mtsolutions.domain.repository.OwnerRepository;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class BootstrapOwnerVerifier {

    private final OwnerRepository ownerRepository;
    private final DateUtils dateUtils;

    public BootstrapOwnerVerifier(OwnerRepository ownerRepository, DateUtils dateUtils) {
        this.ownerRepository = ownerRepository;
        this.dateUtils = dateUtils;
    }

    void onStart(@Observes StartupEvent event) {
        if (this.ownerRepository.count() != 1L) {
            return;
        }
        Owner owner = this.ownerRepository.listAll().stream().findFirst().orElse(null);
        if (owner == null || owner.getEmail() == null || Boolean.TRUE.equals(owner.getEmail().getVerified())) {
            return;
        }
        owner.getEmail().setVerified(true);
        owner.getEmail().setVerificationToken(null);
        owner.getEmail().setVerificationTokenExpiry(null);
        owner.setUpdatedAt(this.dateUtils.now());
        this.ownerRepository.persistOrUpdate(owner);
        log.info("Bootstrap owner email marked as verified so the console is reachable without SMTP.");
    }
}
