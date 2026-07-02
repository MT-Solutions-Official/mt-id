package com.mtsolutions.domain.repository;

import com.mtsolutions.domain.entity.ClientApplication;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ClientApplicationRepository implements PanacheMongoRepositoryBase<ClientApplication, String> {
}
