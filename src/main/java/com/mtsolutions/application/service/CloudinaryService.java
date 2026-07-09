package com.mtsolutions.application.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.mtsolutions.application.constant.CloudinaryFolder;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Map;

@ApplicationScoped
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(
            @ConfigProperty(name = "cloudinary.cloud-name") String cloudName,
            @ConfigProperty(name = "cloudinary.api-key") String apiKey,
            @ConfigProperty(name = "cloudinary.api-secret") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    public String upload(byte[] fileBytes, String publicId, CloudinaryFolder folder) {
        try {
            log.info("Uploading file to Cloudinary: folder={}, publicId={}, resourceType={}",
                    folder.getPath(), publicId, folder.getResourceType());

            // For raw files, append .pdf so Cloudinary serves the correct Content-Type
            String finalPublicId = "raw".equals(folder.getResourceType()) ? publicId + ".pdf" : publicId;

            Map uploadParams = ObjectUtils.asMap(
                    "public_id", finalPublicId,
                    "folder", folder.getPath(),
                    "overwrite", true,
                    "resource_type", folder.getResourceType(),
                    "access_mode", "public"
            );

            Map result = this.cloudinary.uploader().upload(fileBytes, uploadParams);

            String url = (String) result.get("secure_url");
            log.info("File uploaded successfully: {}", url);
            return url;

        } catch (Exception e) {
            log.error("Error uploading file to Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file to Cloudinary", e);
        }
    }

    public void delete(String publicId, String resourceType) {
        try {
            log.info("Deleting file from Cloudinary: publicId={}, resourceType={}", publicId, resourceType);
            this.cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", resourceType));
            log.info("File deleted from Cloudinary: publicId={}", publicId);
        } catch (Exception e) {
            log.error("Error deleting file from Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Failed to delete file from Cloudinary", e);
        }
    }

    public String extractPublicId(String url) {
        if (url == null || url.isBlank()) return null;
        int uploadIndex = url.indexOf("/upload/");
        if (uploadIndex == -1) return null;
        String afterUpload = url.substring(uploadIndex + 8);
        // strip version segment (e.g. v1771953774/)
        if (afterUpload.matches("v\\d+/.*")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf("/") + 1);
        }
        // raw files keep extension (e.g. .pdf) — Cloudinary requires it for destroy
        if (afterUpload.endsWith(".pdf")) {
            return afterUpload;
        }
        // image files strip extension
        int dotIndex = afterUpload.lastIndexOf(".");
        return dotIndex != -1 ? afterUpload.substring(0, dotIndex) : afterUpload;
    }
}
