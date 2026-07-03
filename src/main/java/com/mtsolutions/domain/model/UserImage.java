package com.mtsolutions.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mtsolutions.domain.constant.ImageType;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserImage {

    private String imageUrl;       // A URL do S3, Cloud Storage, etc.
    private ImageType imageType;
    private String fileName;       // Opcional: "cnh_frente.png"
    private Long sizeInBytes;      // Opcional: útil para controle de armazenamento
    private Boolean verified;      // Se o documento/foto foi aprovado pelo backoffice
    private LocalDateTime uploadedAt;
}