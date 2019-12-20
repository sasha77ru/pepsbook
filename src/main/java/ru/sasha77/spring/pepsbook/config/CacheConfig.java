package ru.sasha77.spring.pepsbook.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
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
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.error(exception.getMessage());
                mindService.notToCache();
            }

            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.error(exception.getMessage());
                mindService.notToCache();
            }

            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.error(exception.getMessage());
                mindService.notToCache();
            }

            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.error(exception.getMessage());
                mindService.notToCache();
            }
        };
    }
}
