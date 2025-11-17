package run.prizm.core.storage.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class ChannelAccessCacheRepository {

    private static final String KEY_PREFIX = "channel:access:";
    private static final Duration TTL = Duration.ofHours(1);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void save(Long workspaceId, Long userId, Object data) {
        try {
            String key = buildKey(workspaceId, userId);
            String json = objectMapper.writeValueAsString(data);
            redisTemplate.opsForValue().set(key, json, TTL);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.CACHE_OPERATION_FAILED, e.getMessage());
        }
    }

    public <T> T find(Long workspaceId, Long userId, Class<T> clazz) {
        try {
            String key = buildKey(workspaceId, userId);
            String json = redisTemplate.opsForValue().get(key);
            return json != null ? objectMapper.readValue(json, clazz) : null;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.CACHE_OPERATION_FAILED, e.getMessage());
        }
    }

    public void invalidate(Long workspaceId, Long userId) {
        redisTemplate.delete(buildKey(workspaceId, userId));
    }

    public void invalidateWorkspace(Long workspaceId) {
        String pattern = KEY_PREFIX + workspaceId + ":*";
        redisTemplate.keys(pattern).forEach(redisTemplate::delete);
    }

    private String buildKey(Long workspaceId, Long userId) {
        return KEY_PREFIX + workspaceId + ":" + userId;
    }
}
