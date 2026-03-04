package com.redparrot.prototypes.rate_limiter.dto;

public record RateLimitRequest(String userId, String targetEndpoint) {}
