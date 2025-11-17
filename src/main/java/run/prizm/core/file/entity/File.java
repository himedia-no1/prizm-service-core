package run.prizm.core.file.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import run.prizm.core.common.id.UuidV7LongGeneratedValue;

import java.time.Instant;

@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class File {

    @Id
    @GeneratedValue
    @UuidV7LongGeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    private String extension;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String path;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public File(String name, String extension, Long size, String path) {
        this.name = name;
        this.extension = extension;
        this.size = size;
        this.path = path;
    }
}