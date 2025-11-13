package run.prizm.core.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID uuid;

    @Column(nullable = false, columnDefinition = "user_auth_provider")
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserAuthProvider authProvider;

    @Column(nullable = false)
    private String openidSub;

    @Column
    @Lob
    private String profileImage;

    @Column
    private String globalName;

    @Column
    private String globalEmail;

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime deletedAt;

    @Builder
    public User(UserAuthProvider authProvider, String openidSub, String profileImage, String globalName, String globalEmail) {
        this.uuid = UUID.randomUUID();
        this.authProvider = authProvider;
        this.openidSub = openidSub;
        this.profileImage = profileImage;
        this.globalName = globalName;
        this.globalEmail = globalEmail;
    }

    public void updateProfile(String globalName, String globalEmail) {
        this.globalName = globalName;
        this.globalEmail = globalEmail;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
}