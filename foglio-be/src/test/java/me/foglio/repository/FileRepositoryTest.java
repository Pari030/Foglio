package me.foglio.repository;

import me.foglio.model.File;
import me.foglio.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("FileRepository Tests")
class FileRepositoryTest {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should find files by owner id")
    void shouldFindFilesByOwnerId() {
        // Given
        User owner = User.builder().name("Test User").apiKey(UUID.randomUUID()).build();
        userRepository.save(owner);

        File file1 = File.builder().id(UUID.randomUUID()).originalName("file1.txt").owner(owner).build();
        File file2 = File.builder().id(UUID.randomUUID()).originalName("file2.txt").owner(owner).build();
        fileRepository.save(file1);
        fileRepository.save(file2);

        // When
        List<File> files = fileRepository.findByOwner_Id(owner.getId());

        // Then
        assertThat(files).hasSize(2);
        assertThat(files).extracting(File::getOriginalName).containsExactlyInAnyOrder("file1.txt", "file2.txt");
    }

    @Test
    @DisplayName("Should return empty list when no files for owner")
    void shouldReturnEmptyListWhenNoFilesForOwner() {
        // Given
        User owner = User.builder().name("Test User").apiKey(UUID.randomUUID()).build();
        userRepository.save(owner);

        // When
        List<File> files = fileRepository.findByOwner_Id(owner.getId());

        // Then
        assertThat(files).isEmpty();
    }
}
