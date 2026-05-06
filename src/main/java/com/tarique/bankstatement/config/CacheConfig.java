package com.tarique.bankstatement.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * CacheConfig — Caffeine in-process cache.
 *
 * <p><b>Why Caffeine over Redis for this service?</b>
 * For a read-heavy, latency-critical endpoint serving 10K rps:
 * <ul>
 *   <li>Caffeine has ~50ns get latency vs ~1ms for Redis network round-trip.</li>
 *   <li>For financial data we use short TTLs (5 min) — acceptable staleness.</li>
 *   <li>In a multi-node deployment, Redis would be preferred for consistency;
 *       swap the manager bean to RedisCacheManager with zero service code changes.</li>
 * </ul>
 *
 * Cache key strategy: accountId_from_to_page_size (see StatementServiceImpl).
 * Max 10,000 entries to serve the hottest ~0.02% of 50M accounts in memory.
 */
@Configuration
public class CacheConfig {

    @SuppressWarnings("null")
	@Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("statements", "accounts");
        manager.setCaffeine(
                Caffeine.newBuilder()
                        .maximumSize(10_000)
                        .expireAfterWrite(5, TimeUnit.MINUTES)
                        .recordStats()          // enables /actuator/metrics cache stats
        );
        return manager;
    }
}
