package ru.sasha77.spring.pepsbook.config;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sasha77.spring.pepsbook.services.MindService;

@Configuration
public class CacheConfig extends CachingConfigurerSupport {
    @Bean
    public CacheErrorHandler errorHandler () {
        return new CacheErrorHandler() {
            @Autowired
            MindService mindService;
            private final Logger log = LoggerFactory.getLogger(this.getClass());
            @Override
            public void handleCacheGetError(@NotNull RuntimeException exception, @NotNull Cache cache, @NotNull Object key) {
                log.error(exception.getMessage());
                mindService.notToCache();
            }

            @Override
            public void handleCachePutError(@NotNull RuntimeException exception, @NotNull Cache cache, @NotNull Object key, Object value) {
                log.error(exception.getMessage());
                mindService.notToCache();
            }

            @Override
            public void handleCacheEvictError(@NotNull RuntimeException exception, @NotNull Cache cache, @NotNull Object key) {
                log.error(exception.getMessage());
                mindService.notToCache();
            }

            @Override
            public void handleCacheClearError(@NotNull RuntimeException exception, @NotNull Cache cache) {
                log.error(exception.getMessage());
                mindService.notToCache();
            }
        };
    }
}
