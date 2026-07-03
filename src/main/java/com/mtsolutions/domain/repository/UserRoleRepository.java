package com.mtsolutions.domain.repository;

import com.mtsolutions.application.exception.UserRoleNotFoundException;
import com.mtsolutions.domain.entity.UserRole;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class UserRoleRepository implements PanacheMongoRepositoryBase<UserRole, String> {

    public boolean existsByAppIdAndRoleName(String appId, String roleName) {
        String normalizedRoleName = roleName.trim().toUpperCase();
        return count("appId = ?1 and roleName = ?2", appId, normalizedRoleName) > 0;
    }

    public Optional<UserRole> findByAppIdAndRoleName(String appId, String roleName) {
        String normalizedRoleName = roleName.trim().toUpperCase();
        return find("appId = ?1 and roleName = ?2", appId, normalizedRoleName).firstResultOptional();
    }

    public List<UserRole> findByAppIdAndRoleNames(String appId, List<String> roleNames) {
        List<String> normalizedRoleNames = roleNames.stream()
                .map(String::trim)
                .map(String::toUpperCase)
                .toList();
        return find("appId = ?1 and roleName in ?2", appId, normalizedRoleNames).list();
    }

    public UserRole findUserRoleById(String userRoleId) {
        if (!ObjectId.isValid(userRoleId)) throw new UserRoleNotFoundException();

        return find("_id", new ObjectId(userRoleId)).firstResultOptional()
                .orElseThrow(UserRoleNotFoundException::new);
    }

    public List<UserRole> findByAppId(String appId) {
        return find("appId", appId).list();
    }
}
