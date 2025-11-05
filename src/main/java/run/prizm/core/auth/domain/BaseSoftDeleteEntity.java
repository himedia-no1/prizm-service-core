package run.prizm.core.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@MappedSuperclass
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseSoftDeleteEntity extends BaseEntity {

    @Column
    private LocalDateTime deletedAt;

    public void softDelete() {
        deletedAt = LocalDateTime.now();
    }
}