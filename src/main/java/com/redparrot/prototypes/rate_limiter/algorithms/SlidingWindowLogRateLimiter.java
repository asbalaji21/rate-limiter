package com.redparrot.prototypes.rate_limiter.algorithms;

import com.redparrot.prototypes.rate_limiter.dto.RateLimitCounter;
import com.redparrot.prototypes.rate_limiter.dto.RateLimitRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("SlidingWindowLogRateLimiter")
public class SlidingWindowLogRateLimiter implements RateLimiter {



    private final StringRedisTemplate redisTemplate;

    public SlidingWindowLogRateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RateLimitCounter checkForRateLimit(RateLimitRequest rateLimitRequest) {
        String redisKey = generateKey(rateLimitRequest);
        redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0,
                System.currentTimeMillis() - WINDOW_SIZE_IN_MILLISECONDS);
        redisTemplate.opsForZSet().add(redisKey, UUID.randomUUID().toString(), System.currentTimeMillis());
        Long boxedCount = redisTemplate.opsForZSet().count(redisKey,
                System.currentTimeMillis() - WINDOW_SIZE_IN_MILLISECONDS, System.currentTimeMillis());
        long count = boxedCount == null ? 0 : boxedCount;
        boolean rateLimited = count > LIMIT_THRESHOLD;
        return new RateLimitCounter(rateLimited, WINDOW_SIZE_IN_MILLISECONDS / 1_000, LIMIT_THRESHOLD,
                Math.max(0, LIMIT_THRESHOLD -  count));
    }

    @Override
    public String getAlgorithmName() {
        return "SLIDING_WINDOW_LOG";
    }
}
