package com.mtsolutions.domain.repository;

import com.mtsolutions.application.exception.UserNotFoundException;
import com.mtsolutions.domain.entity.User;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.Optional;
import java.util.regex.Pattern;

@ApplicationScoped
public class UserRepository implements PanacheMongoRepositoryBase<User, String> {

    public User findUserById(String userId) {
        if (!ObjectId.isValid(userId)) throw new UserNotFoundException();
        return find("_id", new ObjectId(userId)).firstResultOptional()
                .orElseThrow(UserNotFoundException::new);
    }

    public Optional<User> findUserByAppIdAndEmail(String appId, String email) {
        if (appId == null || email == null || email.isBlank()) {
            return Optional.empty();
        }
        return find("{appId: ?1, 'emails.email': ?2}", appId, email).firstResultOptional();
    }

    public boolean existsByAppIdAndEmail(String appId, String email) {
        if (appId == null || email == null || email.isBlank()) {
            return false;
        }
        return count("{appId: ?1, 'emails.email': ?2}", appId, email) > 0;
    }

    public boolean existsByAppIdAndUsername(String appId, String username) {
        return count("{appId: ?1, username: {$regex: ?2, $options: 'i'}}", appId, "^" + Pattern.quote(username) + "$") > 0;
    }

    public Optional<User> findUserByEmailVerificationToken(String token) {
        return find("emails.verificationToken", token).firstResultOptional();
    }

    public Optional<User> findUserByPasswordResetToken(String token) {
        return find("password.passwordResetToken", token).firstResultOptional();
    }
}
