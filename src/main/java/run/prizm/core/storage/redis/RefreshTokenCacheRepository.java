package run.prizm.core.storage.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import run.prizm.core.properties.AuthProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RefreshTokenCacheRepository {

    private static final String KEY_PREFIX = "refresh:";

    private final AuthProperties authProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    public void save(String refreshToken, Long userId, String role) {
        Map<String, Object> data = new HashMap<>();
        data.put("role", role);
        data.put("id", userId.toString());

        Duration ttl = Duration.ofMillis(authProperties.getJwt().getRefreshTokenExpiration());
        redisTemplate.opsForHash().putAll(KEY_PREFIX + refreshToken, data);
        redisTemplate.expire(KEY_PREFIX + refreshToken, ttl);
    }

    public RefreshTokenData findByToken(String refreshToken) {
        Map<Object, Object> data = redisTemplate.opsForHash().entries(KEY_PREFIX + refreshToken);
        if (data.isEmpty()) {
            return null;
        }
        
        String role = (String) data.get("role");
        String idStr = (String) data.get("id");
        
        if (role == null || idStr == null) {
            return null;
        }
        
        return new RefreshTokenData(refreshToken, Long.parseLong(idStr), role);
    }

    public Long findUserIdByToken(String refreshToken) {
        RefreshTokenData data = findByToken(refreshToken);
        return data != null ? data.id() : null;
    }

    public void delete(String refreshToken) {
        redisTemplate.delete(KEY_PREFIX + refreshToken);
    }

    public boolean exists(String refreshToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + refreshToken));
    }

    public record RefreshTokenData(String token, Long id, String role) {}
}
