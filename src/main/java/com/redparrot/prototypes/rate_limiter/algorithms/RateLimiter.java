package com.redparrot.prototypes.rate_limiter.algorithms;

import com.redparrot.prototypes.rate_limiter.dto.RateLimitCounter;
import com.redparrot.prototypes.rate_limiter.dto.RateLimitRequest;

public interface RateLimiter {

    int LIMIT_THRESHOLD = 3;
    long WINDOW_SIZE_IN_MILLISECONDS = 60_000L;

    RateLimitCounter checkForRateLimit(RateLimitRequest rateLimitRequest);

    String getAlgorithmName();

    default String generateKey(RateLimitRequest request) {
        return String.format("prototype:rate-limit:%s:%s:%s", getAlgorithmName(), request.userId(),
                request.targetEndpoint());
    }
}
