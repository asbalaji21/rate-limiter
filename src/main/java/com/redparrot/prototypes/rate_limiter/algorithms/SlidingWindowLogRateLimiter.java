package com.redparrot.prototypes.rate_limiter.algorithms;

import com.redparrot.prototypes.rate_limiter.dto.RateLimitCounter;
import com.redparrot.prototypes.rate_limiter.dto.RateLimitRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component("SlidingWindowLogRateLimiter")
public class SlidingWindowLogRateLimiter implements RateLimiter {



    private final StringRedisTemplate redisTemplate;

    private final RedisScript<Long> slidingWindowLogScript;

    public SlidingWindowLogRateLimiter(StringRedisTemplate redisTemplate, RedisScript<Long> slidingWindowLogScript) {
        this.redisTemplate = redisTemplate;
        this.slidingWindowLogScript = slidingWindowLogScript;
    }

    /**
     * Intentionally non-thread-safe implementation kept for comparison.
     * Shows race condition between three separate Redis calls.
     * Use checkForRateLimit() for production use.
     */
    public RateLimitCounter checkForRateLimitNotThreadSafe(RateLimitRequest rateLimitRequest) {
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

    public RateLimitCounter checkForRateLimit(RateLimitRequest rateLimitRequest) {
        String redisKey = generateKey(rateLimitRequest);
        long now = System.currentTimeMillis();
        long windowStart = now - WINDOW_SIZE_IN_MILLISECONDS;

        Long boxedCount = redisTemplate.execute(
                slidingWindowLogScript,
                List.of(redisKey),          // KEYS
                String.valueOf(now),         // ARGV[1]
                String.valueOf(windowStart), // ARGV[2]
                UUID.randomUUID().toString() // ARGV[3]
        );
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
