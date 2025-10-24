package me.foglio.service;

import me.foglio.model.File;
import me.foglio.model.User;
import me.foglio.repository.FileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final FileRepository fileRepository;
    private final Path uploadDir;

    public FileService(FileRepository repository, @Value("${app.files.storage:./files}") String uploadDir) {
        this.fileRepository = repository;
        this.uploadDir = Paths.get(uploadDir);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create upload dir", e);
        }
    }

    public File uploadFile(MultipartFile file, User owner, boolean isPublic) {
        try {
            // Get file extension
            String originalName = file.getOriginalFilename();
            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(originalName.lastIndexOf("."));
            }

            // Generate new file id and filename
            UUID fileId = UUID.randomUUID();
            String newFileName = fileId + extension;
            Path path = uploadDir.resolve(newFileName);

            Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            File fileEntity = File.builder()
                    .id(fileId)
                    .originalName(file.getOriginalFilename())
                    .extension(extension)
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .owner(owner)
                    .isPublic(isPublic)
                    .storedFileName(newFileName)
                    .createdAt(LocalDateTime.now())
                    .build();

            return fileRepository.save(fileEntity);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Optional<File> getFileMetadata(UUID id) {
        return fileRepository.findById(id);
    }

    public List<File> listFilesByOwner(User owner) {
        if (owner == null || owner.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return fileRepository.findByOwner_Id(owner.getId());
    }

    public Path getFilePath(File fileEntity) {
        return uploadDir.resolve(fileEntity.getStoredFileName());
    }

    public void touch(File fileEntity) {
        if (fileEntity.getRequestCount() == null) {
            fileEntity.setRequestCount(0L);
        }
        fileEntity.setRequestCount(fileEntity.getRequestCount() + 1);
        fileEntity.setLastRequestedAt(LocalDateTime.now());
        fileRepository.save(fileEntity);
    }

    public void deleteFile(File fileEntity) {
        // Delete physical file from storage
        Path filePath = getFilePath(fileEntity);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but continue to delete DB entry
            System.err.println("Warning: Could not delete physical file: " + filePath);
        }
        
        // Delete from database
        fileRepository.delete(fileEntity);
    }
}
