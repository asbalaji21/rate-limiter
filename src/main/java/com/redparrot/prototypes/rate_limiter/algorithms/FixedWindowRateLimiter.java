package com.redparrot.prototypes.rate_limiter.algorithms;

import com.redparrot.prototypes.rate_limiter.dto.RateLimitCounter;
import com.redparrot.prototypes.rate_limiter.dto.RateLimitRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class FixedWindowRateLimiter {

    private final static long WINDOW_SIZE_IN_SECONDS = 60L;
    private final static int LIMIT_THRESHOLD = 3;

    private final StringRedisTemplate redisTemplate;

    public FixedWindowRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String generateKey(RateLimitRequest request) {
        return String.format("prototype:rate-limit:%s:%s", request.userId(), request.targetEndpoint());
    }

    public RateLimitCounter checkForRateLimit(RateLimitRequest request) {
        String redisKey = generateKey(request);
        long newValue = redisTemplate.opsForValue().increment(redisKey);
        if(newValue == 1) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(WINDOW_SIZE_IN_SECONDS));
        }
        Long ttlInSeconds = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        long ttl = (ttlInSeconds != null && ttlInSeconds > 0) ? ttlInSeconds : WINDOW_SIZE_IN_SECONDS;
        return new RateLimitCounter(newValue > LIMIT_THRESHOLD, ttl, LIMIT_THRESHOLD,
                Math.max(0, LIMIT_THRESHOLD - newValue));
    }
}
