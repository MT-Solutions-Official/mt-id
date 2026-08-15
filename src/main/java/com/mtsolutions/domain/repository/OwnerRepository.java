package com.mtsolutions.domain.repository;

import com.mtsolutions.application.exception.OwnerNotFoundException;
import com.mtsolutions.domain.entity.Owner;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.Optional;
import java.util.regex.Pattern;

@ApplicationScoped
public class OwnerRepository implements PanacheMongoRepositoryBase<Owner, String> {

    public Owner findOwnerById(String ownerId) {
        if (!ObjectId.isValid(ownerId)) throw new OwnerNotFoundException();
        return find("_id", new ObjectId(ownerId)).firstResultOptional()
                .orElseThrow(OwnerNotFoundException::new);
    }

    public Boolean existsByOwnerId(String ownerId) {
        if (!ObjectId.isValid(ownerId)) return false;
        return find("_id", new ObjectId(ownerId)).firstResultOptional().isPresent();
    }

    public boolean existsByEmail(String email) {
        return count("{'email.email': {$regex: ?1, $options: 'i'}}", "^" + Pattern.quote(email) + "$") > 0;
    }

    public Optional<Owner> findOwnerByEmail(String email) {
        return find("{'email.email': {$regex: ?1, $options: 'i'}}", "^" + Pattern.quote(email) + "$").firstResultOptional();
    }

    public Optional<Owner> findOwnerByEmailVerificationToken(String token) {
        return find("email.verificationToken", token).firstResultOptional();
    }

    public Optional<Owner> findOwnerByPasswordResetToken(String token) {
        return find("password.passwordResetToken", token).firstResultOptional();
    }
}