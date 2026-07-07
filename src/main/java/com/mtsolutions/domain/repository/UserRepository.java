package com.mtsolutions.domain.repository;

import com.mtsolutions.application.exception.UserNotFoundException;
import com.mtsolutions.domain.entity.User;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

@ApplicationScoped
public class UserRepository implements PanacheMongoRepositoryBase<User, String> {

    public User findUserById(String userId) {
        if (!ObjectId.isValid(userId)) throw new UserNotFoundException();

        return find("_id", new ObjectId(userId)).firstResultOptional()
                .orElseThrow(UserNotFoundException::new);
    }
}
