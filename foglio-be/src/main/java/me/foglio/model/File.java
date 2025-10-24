package me.foglio.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "files")
public class File {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    private String originalName;
    private String extension;
    private String contentType;
    private Long size;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User owner;

    @Builder.Default
    private Boolean isPublic = false;

    @Builder.Default
    private Long requestCount = 0L;

    @Builder.Default
    private LocalDateTime lastRequestedAt = null;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // stored filename on disk
    private String storedFileName;

}
