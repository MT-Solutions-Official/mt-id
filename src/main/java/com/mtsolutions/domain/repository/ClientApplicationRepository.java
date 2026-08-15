package com.mtsolutions.domain.repository;

import com.mtsolutions.application.exception.ClientApplicationNotFoundException;
import com.mtsolutions.domain.entity.ClientApplication;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.Optional;
import java.util.regex.Pattern;

@ApplicationScoped
public class ClientApplicationRepository implements PanacheMongoRepositoryBase<ClientApplication, String> {

    public ClientApplication findClientApplicationById(String appId) {
        if (!ObjectId.isValid(appId)) throw new ClientApplicationNotFoundException();

        return find("_id", new ObjectId(appId)).firstResultOptional()
                .orElseThrow(ClientApplicationNotFoundException::new);
    }

    public Optional<ClientApplication> findClientApplicationByApiKey(String apiKey) {
        return find("apiKey", apiKey).firstResultOptional();
    }

    public boolean existsByAllowedOrigin(String origin) {
        if (origin == null || origin.isBlank()) {
            return false;
        }
        String withoutSlash = origin.endsWith("/") ? origin.substring(0, origin.length() - 1) : origin;
        return count("{allowedOrigins: {$regex: ?1, $options: 'i'}, active: {$ne: false}}",
                "^" + Pattern.quote(withoutSlash) + "/?$") > 0;
    }
}
