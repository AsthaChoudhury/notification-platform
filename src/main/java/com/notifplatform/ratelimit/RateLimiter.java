package com.notifplatform.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RateLimiter.class);

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.rate-limit.max-requests:5}")
    private int maxRequests;

    @Value("${app.rate-limit.window-seconds:60}")
    private int windowSeconds;

    public RateLimiter(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    public boolean isAllowed(Long userId, String type) {
        String key = buildKey(userId, type);

        Long count = redisTemplate.opsForValue().increment(key);

        if (count == null) {
            log.warn("Redis unavailable — allowing request for user {}", userId);
            return true;
        }
        if (count == 1) {
            redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            log.debug("Rate limit window started for key [{}]", key);
        }

        if (count > maxRequests) {
            log.warn("Rate limit EXCEEDED — user={} type={} count={}/{}",
                    userId, type, count, maxRequests);
            return false;
        }

        log.debug("Rate limit OK — user={} type={} count={}/{}",
                userId, type, count, maxRequests);
        return true;
    }

    public int getRemainingRequests(Long userId, String type) {
        String key = buildKey(userId, type);
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) return maxRequests;
        int used = Integer.parseInt(value);
        return Math.max(0, maxRequests - used);
    }
    public long getSecondsUntilReset(Long userId, String type) {
        String key = buildKey(userId, type);
        Long ttl = redisTemplate.getExpire(key);
        return ttl != null && ttl > 0 ? ttl : windowSeconds;
    }

    private String buildKey(Long userId, String type) {
        return String.format("rate_limit:user:%d:%s", userId, type);
    }
}