package com.mtsolutions.domain.dto.request;

import com.mtsolutions.domain.constant.ImageType;

import java.nio.file.Path;

public record UploadOwnerImageRequestDto(
        ImageType imageType,
        Path uploadedFilePath,
        String fileName,
        Long sizeInBytes,
        String contentType
) {
}
