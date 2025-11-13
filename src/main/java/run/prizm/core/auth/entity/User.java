package run.prizm.core.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import run.prizm.core.common.id.UuidV7LongGenerator;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(generator = "uuidv7-long")
    @SuppressWarnings("deprecation")
    @GenericGenerator(
            name = "uuidv7-long",
            strategy = "run.prizm.core.common.id.UuidV7LongGenerator"
    )
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserAuthProvider authProvider;

    @Column(nullable = false)
    private String openidSub;

    @Lob
    private String profileImagePath;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    @Builder
    public User(UserAuthProvider authProvider, String openidSub, String profileImagePath, String name, String email, Language language) {
        this.authProvider = authProvider;
        this.openidSub = openidSub;
        this.profileImagePath = profileImagePath;
        this.name = name;
        this.email = email;
        this.language = language;
    }
}
