package run.prizm.auth.user.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import run.prizm.auth.common.entity.BaseSoftDeleteEntity;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseSoftDeleteEntity {

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

    @Builder
    public User(UserAuthProvider authProvider, String openidSub, String profileImage, String globalName, String globalEmail) {
        this.uuid = UUID.randomUUID();
        this.authProvider = authProvider;
        this.openidSub = openidSub;
        this.profileImage = profileImage;
        this.globalName = globalName;
        this.globalEmail = globalEmail;
    }

    public void updateProfile(String profileImage, String globalName, String globalEmail) {
        this.profileImage = profileImage;
        this.globalName = globalName;
        this.globalEmail = globalEmail;
    }
}