package com.redparrot.prototypes.rate_limiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.script.RedisScript;

@Configuration
public class RedisScriptConfig {

    @Bean
    public RedisScript<Long> slidingWindowLogScript(ResourceLoader resourceLoader) {
        Resource resource = resourceLoader.getResource(
                "classpath:scripts/sliding_window_log.lua");
        return RedisScript.of(resource, Long.class);
    }

//    @Bean
//    public RedisScript<Long> tokenBucketScript(ResourceLoader resourceLoader)
//            throws IOException {
//        Resource resource = resourceLoader.getResource(
//                "classpath:scripts/token_bucket.lua");
//        return RedisScript.of(resource, Long.class);
//    }
}
