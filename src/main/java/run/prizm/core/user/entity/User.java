package run.prizm.core.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import run.prizm.core.common.constraint.Language;
import run.prizm.core.common.id.UuidV7LongGeneratedValue;
import run.prizm.core.file.entity.File;
import run.prizm.core.user.constraint.UserAuthProvider;

import java.time.Instant;

@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue
    @UuidV7LongGeneratedValue
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserAuthProvider authProvider;

    @Column(nullable = false)
    private String openidSub;

    @ManyToOne(fetch = FetchType.LAZY)
    private File image;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Column(nullable = false)
    private boolean active;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant deletedAt;

    @Builder
    public User(UserAuthProvider authProvider, String openidSub, File image, String name, String email, Language language, boolean active) {
        this.authProvider = authProvider;
        this.openidSub = openidSub;
        this.image = image;
        this.name = name;
        this.email = email;
        this.language = language;
        this.active = active;
    }
}