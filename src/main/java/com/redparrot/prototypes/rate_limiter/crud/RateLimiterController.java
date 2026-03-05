package com.redparrot.prototypes.rate_limiter.crud;

import com.redparrot.prototypes.rate_limiter.algorithms.FixedWindowRateLimiter;
import com.redparrot.prototypes.rate_limiter.algorithms.SlidingWindowLogRateLimiter;
import com.redparrot.prototypes.rate_limiter.dto.RateLimitCounter;
import com.redparrot.prototypes.rate_limiter.dto.RateLimitRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RateLimiterController {

    private final FixedWindowRateLimiter fixedWindowRateLimiter;

    private final SlidingWindowLogRateLimiter slidingWindowLogRateLimiter;

    public RateLimiterController(@Qualifier("FixedWindowRateLimiter") FixedWindowRateLimiter fixedWindowRateLimiter,
                                 @Qualifier("SlidingWindowLogRateLimiter")
                                 SlidingWindowLogRateLimiter slidingWindowLogRateLimiter) {
        this.fixedWindowRateLimiter = fixedWindowRateLimiter;
        this.slidingWindowLogRateLimiter = slidingWindowLogRateLimiter;
    }

    @GetMapping("/v1/rate-limiter/health-check/{name}")
    public String healthCheck(@PathVariable String name) {
        return "Hello " + name;
    }

    @PostMapping("/v1/rate-limiter/fixed-window")
    public ResponseEntity<Void> rateLimiterFixedWindow(@RequestBody RateLimitRequest rateLimitRequest) {
        RateLimitCounter rateLimitCounter =  fixedWindowRateLimiter.checkForRateLimit(rateLimitRequest);
        return buildResponseEntity(rateLimitCounter);
    }

    @PostMapping("/v1/rate-limiter/sliding-log")
    public ResponseEntity<Void> rateLimiterSlidingLog(@RequestBody RateLimitRequest rateLimitRequest) {
        RateLimitCounter rateLimitCounter =  slidingWindowLogRateLimiter.checkForRateLimit(rateLimitRequest);
        return buildResponseEntity(rateLimitCounter);
    }

    private ResponseEntity<Void> buildResponseEntity(RateLimitCounter rateLimitCounter) {
        HttpStatus status = rateLimitCounter.rateLimited() ? HttpStatus.TOO_MANY_REQUESTS : HttpStatus.OK;
        ResponseEntity.BodyBuilder builder = ResponseEntity.status(status)
                .header("X-Ratelimit-Remaining", String.valueOf(rateLimitCounter.remaining()))
                .header("X-Ratelimit-Limit", String.valueOf(rateLimitCounter.limit()));
        if (rateLimitCounter.rateLimited()) {
            builder.header("Retry-After", String.valueOf(rateLimitCounter.ttlInSeconds()));
        }
        return builder.build();
    }
}
