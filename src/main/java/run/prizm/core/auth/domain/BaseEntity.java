package run.prizm.core.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseEntity {

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}