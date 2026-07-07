package com.mtsolutions.domain.repository;

import com.mtsolutions.application.exception.ApplicationOwnerNotFoundException;
import com.mtsolutions.domain.entity.ApplicationOwner;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.Optional;

@ApplicationScoped
public class ApplicationOwnerRepository implements PanacheMongoRepositoryBase<ApplicationOwner, String> {

    public ApplicationOwner findOwnerById(String ownerId) {
        if (!ObjectId.isValid(ownerId)) throw new ApplicationOwnerNotFoundException();

        return find("_id", new ObjectId(ownerId)).firstResultOptional()
                .orElseThrow(ApplicationOwnerNotFoundException::new);

    }

    public Boolean existsByOwnerId(String ownerId) {
        if (!ObjectId.isValid(ownerId)) return false;
        return find("_id", new ObjectId(ownerId)).firstResultOptional().isPresent();
    }

    public Optional<ApplicationOwner> findOwnerByEmail(String email) {
        return find("email.email", email).firstResultOptional();
    }
}