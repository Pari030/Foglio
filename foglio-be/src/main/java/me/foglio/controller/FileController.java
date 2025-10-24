package me.foglio.controller;

import me.foglio.dto.FileMetadataDTO;
import me.foglio.model.File;
import me.foglio.model.User;
import me.foglio.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("")
    public List<FileMetadataDTO> listMine(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return fileService.listFilesByOwner(user).stream()
                .map(FileMetadataDTO::fromEntity)
                .toList();
    }

    @GetMapping("/{id}/metadata")
    public FileMetadataDTO metadata(@PathVariable UUID id, Authentication authentication) {
        Optional<File> opt = fileService.getFileMetadata(id);
        File f = getFile(authentication, opt);
        return FileMetadataDTO.fromEntity(f);
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> preview(@PathVariable UUID id, Authentication authentication) {
        Optional<File> opt = fileService.getFileMetadata(id);
        File f = getFile(authentication, opt);
        String ct = f.getContentType();
        if (ct == null) ct = "application/octet-stream";
        boolean isPreviewable = ct.startsWith("image/") || ct.startsWith("video/");
        if (!isPreviewable) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Preview only available for images and videos");

        Path p = fileService.getFilePath(f);
        try {
            Resource resource = new UrlResource(p.toUri());
            if (!resource.exists() || !resource.isReadable()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            fileService.touch(f);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(ct));
            headers.setContentLength(f.getSize() != null ? f.getSize() : -1);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + f.getOriginalName() + "\"");
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID id, Authentication authentication) {
        Optional<File> opt = fileService.getFileMetadata(id);
        if (opt.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        File f = getFile(authentication, opt);

        Path p = fileService.getFilePath(f);
        try {
            Resource resource = new UrlResource(p.toUri());
            if (!resource.exists() || !resource.isReadable()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            fileService.touch(f);
            HttpHeaders headers = new HttpHeaders();
            String ct = f.getContentType() != null ? f.getContentType() : "application/octet-stream";
            headers.setContentType(MediaType.parseMediaType(ct));
            headers.setContentLength(f.getSize() != null ? f.getSize() : -1);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + f.getOriginalName() + "\"");
            return new ResponseEntity<>(resource, headers, HttpStatus.OK);
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/upload")
    public FileMetadataDTO upload(@RequestParam("file") MultipartFile file,
                                  @RequestParam(value = "public", required = false, defaultValue = "false") boolean isPublic,
                                  Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        File saved = fileService.uploadFile(file, user, isPublic);
        return FileMetadataDTO.fromEntity(saved);
    }

    private static File getFile(Authentication authentication, Optional<File> opt) {
        if (opt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        File f = opt.get();

        // Public files downloadable by anyone
        if (!Boolean.TRUE.equals(f.getIsPublic())) {
            // private -> must authenticate and be owner
            if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
            }
            if (f.getOwner() == null || !f.getOwner().getId().equals(user.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN);
            }
        }
        return f;
    }
}
