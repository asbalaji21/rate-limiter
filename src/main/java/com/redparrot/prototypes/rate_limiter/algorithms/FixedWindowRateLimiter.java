package com.redparrot.prototypes.rate_limiter.algorithms;

import com.redparrot.prototypes.rate_limiter.dto.RateLimitCounter;
import com.redparrot.prototypes.rate_limiter.dto.RateLimitRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component("FixedWindowRateLimiter")
public class FixedWindowRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;

    public FixedWindowRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RateLimitCounter checkForRateLimit(RateLimitRequest request) {
        String redisKey = generateKey(request);
        long newValue = redisTemplate.opsForValue().increment(redisKey);
        if(newValue == 1) {
            redisTemplate.expire(redisKey, Duration.ofMillis(WINDOW_SIZE_IN_MILLISECONDS));
        }
        Long ttlInSeconds = redisTemplate.getExpire(redisKey, TimeUnit.SECONDS);
        long ttl = (ttlInSeconds != null && ttlInSeconds > 0) ? ttlInSeconds : WINDOW_SIZE_IN_MILLISECONDS * 1_000;
        return new RateLimitCounter(newValue > LIMIT_THRESHOLD, ttl, LIMIT_THRESHOLD,
                Math.max(0, LIMIT_THRESHOLD - newValue));
    }

    @Override
    public String getAlgorithmName() {
        return "FIXED_WINDOW";
    }
}
