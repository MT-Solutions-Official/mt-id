package com.mtsolutions.domain.repository;

import com.mtsolutions.application.exception.UserNotFoundException;
import com.mtsolutions.domain.entity.User;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheMongoRepositoryBase<User, String> {

    public User findUserById(String userId) {
        if (!ObjectId.isValid(userId)) throw new UserNotFoundException();
        return find("_id", new ObjectId(userId)).firstResultOptional()
                .orElseThrow(UserNotFoundException::new);
    }

    public User findUserByEmail(String email) {
        return find("emails.email", email).firstResultOptional()
                .orElseThrow(UserNotFoundException::new);
    }

    public Optional<User> findUserByEmailVerificationToken(String token) {
        return find("emails.verificationToken", token).firstResultOptional();
    }

    public Optional<User> findUserByPasswordResetToken(String token) {
        return find("password.passwordResetToken", token).firstResultOptional();
    }
}
