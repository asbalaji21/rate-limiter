package com.redparrot.prototypes.rate_limiter.crud;

import com.redparrot.prototypes.rate_limiter.algorithms.FixedWindowRateLimiter;
import com.redparrot.prototypes.rate_limiter.dto.RateLimitCounter;
import com.redparrot.prototypes.rate_limiter.dto.RateLimitRequest;
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

    public RateLimiterController(FixedWindowRateLimiter fixedWindowRateLimiter) {
        this.fixedWindowRateLimiter = fixedWindowRateLimiter;
    }

    @GetMapping("/v1/rate-limiter/health-check/{name}")
    public String healthCheck(@PathVariable String name) {
        return "Hello " + name;
    }

    @PostMapping("/v1/rate-limiter")
    public ResponseEntity<Void> rateLimiter(@RequestBody RateLimitRequest rateLimitRequest) {
        RateLimitCounter rateLimitCounter =  fixedWindowRateLimiter.checkForRateLimit(rateLimitRequest);
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
