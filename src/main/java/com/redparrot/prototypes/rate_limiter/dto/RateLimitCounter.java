package com.redparrot.prototypes.rate_limiter.dto;

public record RateLimitCounter(boolean rateLimited, long ttlInSeconds, long limit, long remaining) {}
