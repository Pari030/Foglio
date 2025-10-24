package me.foglio.dto;

import me.foglio.model.File;
import me.foglio.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("FileMetadataDTO Tests")
class FileMetadataDTOTest {

    @Test
    @DisplayName("Should convert File entity to DTO correctly")
    void shouldConvertFileEntityToDTOCorrectly() {
        // Given
        UUID fileId = UUID.randomUUID();
        User owner = User.builder().id(1L).name("Owner").build();
        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime lastRequestedAt = LocalDateTime.now().minusHours(1);

        File file = File.builder()
                .id(fileId)
                .originalName("test.txt")
                .extension(".txt")
                .contentType("text/plain")
                .size(1024L)
                .owner(owner)
                .isPublic(true)
                .storedFileName("stored-test.txt")
                .createdAt(createdAt)
                .requestCount(10L)
                .lastRequestedAt(lastRequestedAt)
                .build();

        // When
        FileMetadataDTO dto = FileMetadataDTO.fromEntity(file);

        // Then
        assertThat(dto.id).isEqualTo(fileId);
        assertThat(dto.originalName).isEqualTo("test.txt");
        assertThat(dto.extension).isEqualTo(".txt");
        assertThat(dto.contentType).isEqualTo("text/plain");
        assertThat(dto.size).isEqualTo(1024L);
        assertThat(dto.ownerId).isEqualTo(1L);
        assertThat(dto.isPublic).isTrue();
        assertThat(dto.storedFileName).isEqualTo("stored-test.txt");
        assertThat(dto.createdAt).isEqualTo(createdAt);
        assertThat(dto.requestCount).isEqualTo(10L);
        assertThat(dto.lastRequestedAt).isEqualTo(lastRequestedAt);
    }

    @Test
    @DisplayName("Should handle null owner correctly")
    void shouldHandleNullOwnerCorrectly() {
        // Given
        File file = File.builder()
                .id(UUID.randomUUID())
                .originalName("public.txt")
                .owner(null)
                .build();

        // When
        FileMetadataDTO dto = FileMetadataDTO.fromEntity(file);

        // Then
        assertThat(dto.ownerId).isNull();
    }
}
