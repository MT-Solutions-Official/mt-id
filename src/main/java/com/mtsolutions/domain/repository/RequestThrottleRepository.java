package com.mtsolutions.domain.repository;

import com.mtsolutions.domain.entity.RequestThrottle;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;

@ApplicationScoped
public class RequestThrottleRepository implements PanacheMongoRepositoryBase<RequestThrottle, String> {

    public Optional<RequestThrottle> findByKey(String key) {
        return find("key", key).firstResultOptional();
    }
}
