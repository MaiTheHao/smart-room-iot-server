package com.iviet.ivshs.config;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.iviet.ivshs.enumeration.CacheDefinition;

@Configuration
@EnableCaching
@Deprecated
public class CacheConfig {

	@Bean
	public CacheManager cacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();

		List<Cache> caches = List.of(CacheDefinition.values()).stream()
			.map(spec -> buildCache(spec.getCacheName(), spec.getTtl(), spec.getUnit(), spec.getMaxSize()))
			.collect(Collectors.toList());

		cacheManager.setCaches(caches);
		return cacheManager;
	}

	private Cache buildCache(String name, long ttl, TimeUnit unit, long maxSize) {
		return new CaffeineCache(
			name,
			Caffeine.newBuilder()
				.expireAfterWrite(ttl, unit)
				.maximumSize(maxSize)
				.recordStats()
				.build()
		);
	}
}
