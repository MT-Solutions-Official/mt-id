package com.mtsolutions.application.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
@Slf4j
public class MongoIndexInitializer {

    @ConfigProperty(name = "quarkus.mongodb.database")
    String database;

    private final MongoClient mongoClient;

    public MongoIndexInitializer(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    void onStart(@Observes StartupEvent event) {
        try {
            MongoCollection<Document> users = this.mongoClient.getDatabase(this.database).getCollection("users");
            MongoCollection<Document> owners = this.mongoClient.getDatabase(this.database).getCollection("owners");

            users.createIndex(
                    Indexes.compoundIndex(Indexes.ascending("appId"), Indexes.ascending("emails.email")),
                    new IndexOptions().name("uniq_users_appId_emails_email").unique(true)
            );
            users.createIndex(
                    Indexes.compoundIndex(Indexes.ascending("appId"), Indexes.ascending("username")),
                    new IndexOptions()
                            .name("uniq_users_appId_username")
                            .unique(true)
                            .partialFilterExpression(Filters.and(
                                    Filters.exists("username"),
                                    Filters.gt("username", ""),
                                    Filters.type("username", "string")
                            ))
            );
            owners.createIndex(
                    Indexes.ascending("email.email"),
                    new IndexOptions().name("uniq_owners_email").unique(true)
            );
            users.createIndex(
                    Indexes.ascending("emails.verificationToken"),
                    new IndexOptions()
                            .name("idx_users_email_verification_token")
                            .sparse(true)
            );
            users.createIndex(
                    Indexes.ascending("password.passwordResetToken"),
                    new IndexOptions()
                            .name("idx_users_password_reset_token")
                            .sparse(true)
            );
            owners.createIndex(
                    Indexes.ascending("email.verificationToken"),
                    new IndexOptions()
                            .name("idx_owners_email_verification_token")
                            .sparse(true)
            );
            owners.createIndex(
                    Indexes.ascending("password.passwordResetToken"),
                    new IndexOptions()
                            .name("idx_owners_password_reset_token")
                            .sparse(true)
            );

            MongoCollection<Document> throttles = this.mongoClient.getDatabase(this.database).getCollection("request_throttles");
            throttles.createIndex(
                    Indexes.ascending("key"),
                    new IndexOptions().name("uniq_request_throttles_key").unique(true)
            );
            throttles.createIndex(
                    Indexes.ascending("updatedAt"),
                    new IndexOptions().name("ttl_request_throttles").expireAfter(2L, java.util.concurrent.TimeUnit.DAYS)
            );

            MongoCollection<Document> userRefreshTokens = this.mongoClient.getDatabase(this.database).getCollection("user_refresh_tokens");
            userRefreshTokens.createIndex(
                    Indexes.compoundIndex(Indexes.ascending("userId"), Indexes.ascending("appId")),
                    new IndexOptions().name("idx_user_refresh_tokens_user_app")
            );
            MongoCollection<Document> ownerRefreshTokens = this.mongoClient.getDatabase(this.database).getCollection("owner_refresh_tokens");
            ownerRefreshTokens.createIndex(
                    Indexes.ascending("ownerId"),
                    new IndexOptions().name("idx_owner_refresh_tokens_owner")
            );

            log.info("Mongo unique indexes ensured for users and owners");
        } catch (Exception e) {
            log.error("Failed to create unique indexes. Remove duplicate emails/usernames and retry.", e);
            throw e;
        }
    }
}
