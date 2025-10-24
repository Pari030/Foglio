package me.foglio.dto;

import me.foglio.model.File;

import java.time.LocalDateTime;
import java.util.UUID;

public class FileMetadataDTO {
    public UUID id;
    public String originalName;
    public String extension;
    public String contentType;
    public Long size;
    public Long ownerId;
    public Boolean isPublic;
    public String storedFileName;
    public LocalDateTime createdAt;
    public Long requestCount;
    public LocalDateTime lastRequestedAt;

    public static FileMetadataDTO fromEntity(File f) {
        FileMetadataDTO dto = new FileMetadataDTO();
        dto.id = f.getId();
        dto.originalName = f.getOriginalName();
        dto.extension = f.getExtension();
        dto.contentType = f.getContentType();
        dto.size = f.getSize();
        dto.ownerId = f.getOwner() != null ? f.getOwner().getId() : null;
        dto.isPublic = f.getIsPublic();
        dto.storedFileName = f.getStoredFileName();
        dto.createdAt = f.getCreatedAt();
        dto.requestCount = f.getRequestCount();
        dto.lastRequestedAt = f.getLastRequestedAt();
        return dto;
    }
}

