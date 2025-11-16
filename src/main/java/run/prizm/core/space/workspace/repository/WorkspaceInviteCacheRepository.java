package run.prizm.core.space.workspace.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import run.prizm.core.space.workspace.cache.WorkspaceInviteCache;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class WorkspaceInviteCacheRepository {

    private static final String KEY_PREFIX = "workspace:invite:";

    private final RedisTemplate<String, WorkspaceInviteCache> redisTemplate;

    public void save(WorkspaceInviteCache cache) {
        Duration ttl = calculateTtl(cache);
        String key = key(cache.getCode());
        if (ttl != null) {
            if (ttl.isZero() || ttl.isNegative()) {
                redisTemplate.delete(key);
            } else {
                redisTemplate.opsForValue()
                             .set(key, cache, ttl);
            }
            return;
        }
        redisTemplate.opsForValue()
                     .set(key, cache);
    }

    public Optional<WorkspaceInviteCache> find(String code) {
        return Optional.ofNullable(redisTemplate.opsForValue()
                                                .get(key(code)));
    }

    public void delete(String code) {
        redisTemplate.delete(key(code));
    }

    private Duration calculateTtl(WorkspaceInviteCache cache) {
        if (cache.getExpiresAtEpochMillis() == null) {
            return null;
        }
        long remainingMillis = cache.getExpiresAtEpochMillis() - Instant.now()
                                                                        .toEpochMilli();
        if (remainingMillis <= 0) {
            return Duration.ZERO;
        }
        return Duration.ofMillis(remainingMillis);
    }

    private String key(String code) {
        return KEY_PREFIX + code;
    }
}