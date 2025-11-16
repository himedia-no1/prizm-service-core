package run.prizm.core.storage.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RefreshTokenCacheRepository {

    private static final String KEY_PREFIX = "refresh:";
    private static final Duration TTL = Duration.ofDays(7);

    private final RedisTemplate<String, String> redisTemplate;

    public void save(String refreshToken, Long userId) {
        redisTemplate.opsForValue().set(KEY_PREFIX + refreshToken, userId.toString(), TTL);
    }

    public Long findUserIdByToken(String refreshToken) {
        String userId = redisTemplate.opsForValue().get(KEY_PREFIX + refreshToken);
        return userId != null ? Long.parseLong(userId) : null;
    }

    public void delete(String refreshToken) {
        redisTemplate.delete(KEY_PREFIX + refreshToken);
    }

    public boolean exists(String refreshToken) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(KEY_PREFIX + refreshToken));
    }
}
