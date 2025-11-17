package run.prizm.core.space.workspace.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import run.prizm.core.space.workspace.cache.WorkspaceInviteCache;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class WorkspaceInviteCacheRepository {

    private static final String KEY_PREFIX = "workspace:invite:";
    private static final String WORKSPACE_INDEX_PREFIX = "workspace:invites:";

    private final RedisTemplate<String, WorkspaceInviteCache> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public void save(WorkspaceInviteCache cache) {
        Duration ttl = calculateTtl(cache);
        String key = key(cache.getCode());
        String indexKey = workspaceIndexKey(cache.getWorkspaceId());

        if (ttl != null) {
            if (ttl.isZero() || ttl.isNegative()) {
                redisTemplate.delete(key);
                stringRedisTemplate.opsForSet()
                                   .remove(indexKey, cache.getCode());
            } else {
                redisTemplate.opsForValue()
                             .set(key, cache, ttl);
                stringRedisTemplate.opsForSet()
                                   .add(indexKey, cache.getCode());
                stringRedisTemplate.expire(indexKey, ttl);
            }
            return;
        }
        redisTemplate.opsForValue()
                     .set(key, cache);
        stringRedisTemplate.opsForSet()
                           .add(indexKey, cache.getCode());
    }

    public Optional<WorkspaceInviteCache> find(String code) {
        return Optional.ofNullable(redisTemplate.opsForValue()
                                                .get(key(code)));
    }

    public List<WorkspaceInviteCache> findByWorkspaceId(Long workspaceId) {
        String indexKey = workspaceIndexKey(workspaceId);
        Set<String> codes = stringRedisTemplate.opsForSet()
                                               .members(indexKey);

        if (codes == null || codes.isEmpty()) {
            return List.of();
        }

        return codes.stream()
                    .map(this::find)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
    }

    public void delete(String code) {
        WorkspaceInviteCache cache = redisTemplate.opsForValue()
                                                  .get(key(code));
        if (cache != null) {
            String indexKey = workspaceIndexKey(cache.getWorkspaceId());
            stringRedisTemplate.opsForSet()
                               .remove(indexKey, code);
        }
        redisTemplate.delete(key(code));
    }

    private Duration calculateTtl(WorkspaceInviteCache cache) {
        if (cache.getExpiresAt() == null) {
            return null;
        }
        long remainingMillis = cache.getExpiresAt() - Instant.now()
                                                             .toEpochMilli();
        if (remainingMillis <= 0) {
            return Duration.ZERO;
        }
        return Duration.ofMillis(remainingMillis);
    }

    private String key(String code) {
        return KEY_PREFIX + code;
    }

    private String workspaceIndexKey(Long workspaceId) {
        return WORKSPACE_INDEX_PREFIX + workspaceId;
    }
}