package com.niveshtrack.portfolio.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;

/**
 * General application beans: caching, HTTP client.
 *
 * <p>The {@link SimpleCacheManager} uses in-memory concurrent maps.
 * For production with multiple instances, replace with Redis:
 * {@code spring-boot-starter-data-redis} + {@code @EnableCaching}.
 */
@Configuration
@EnableCaching
public class AppConfig {

    /**
     * In-memory cache manager with named caches.
     *
     * <ul>
     *   <li>{@code stockPrices}    - current market prices (updated every 10 min)</li>
     *   <li>{@code holdings}       - calculated holding positions (invalidated on transaction changes)</li>
     *   <li>{@code mutualFundNav}  - mutual fund NAV values (updated daily)</li>
     * </ul>
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache("stockPrices"),
                new ConcurrentMapCache("holdings"),
                new ConcurrentMapCache("mutualFundNav")
        ));
        return cacheManager;
    }

    /**
     * RestTemplate for external API calls (Alpha Vantage stock prices).
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
