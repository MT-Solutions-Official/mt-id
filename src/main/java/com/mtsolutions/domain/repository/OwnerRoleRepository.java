package com.mtsolutions.domain.repository;

import com.mtsolutions.application.exception.OwnerRoleNotFoundException;
import com.mtsolutions.domain.entity.OwnerRole;
import io.quarkus.mongodb.panache.PanacheMongoRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class OwnerRoleRepository implements PanacheMongoRepositoryBase<OwnerRole, String> {

    public boolean existsByRoleName(String roleName) {
        String normalizedRoleName = roleName.trim().toUpperCase();
        return count("roleName", normalizedRoleName) > 0;
    }

    public OwnerRole findOwnerRoleByName(String roleName) {
        return find("roleName", roleName).firstResult();
    }

    public Optional<OwnerRole> findByRoleName(String roleName) {
        String normalizedRoleName = roleName.trim().toUpperCase();
        return find("roleName", normalizedRoleName).firstResultOptional();
    }

    public OwnerRole findOwnerRoleById(String ownerRoleId) {
        if (!ObjectId.isValid(ownerRoleId)) throw new OwnerRoleNotFoundException();

        return find("_id", new ObjectId(ownerRoleId)).firstResultOptional()
                .orElseThrow(OwnerRoleNotFoundException::new);
    }

    public List<OwnerRole> findAllOwnerRoles() {
        return listAll();
    }
}
