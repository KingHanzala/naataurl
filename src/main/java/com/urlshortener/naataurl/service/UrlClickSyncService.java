package com.urlshortener.naataurl.service;

import com.urlshortener.naataurl.manager.RedisManager;
import com.urlshortener.naataurl.persistence.model.UrlMapper;
import com.urlshortener.naataurl.utils.RedisHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Set;

@Service
public class UrlClickSyncService {
    private static final Logger log = LoggerFactory.getLogger(UrlClickSyncService.class);
    private static final long SYNC_INTERVAL_MS = 3600000;

    @Value("${click.sync.interval.ms:3600000}")
    private Long clickSyncIntervalMs;

    @Autowired
    private RedisManager redisManager;

    @Autowired
    private UrlService urlService;

    @Autowired
    private RedisService redisService;

    @Scheduled(fixedRateString = "${click.sync.interval.ms:3600000}")
    public void syncUrlClicks() {
        log.info("Starting URL clicks sync task");
        try {
            // Get all URL click keys from Redis
            Set<String> urlClickKeys = redisService.keys(RedisHelper.URL_CLICKS_KEY_FORMAT + "*");
            
            for (String clickKey : urlClickKeys) {
                String shortUrl = extractShortUrlFromKey(clickKey);
                if (shortUrl == null) continue;

                // Get last DB update timestamp
                String lastUpdateKey = String.format(RedisHelper.URL_CLICKS_KEY_LAST_DBUPDATE, shortUrl);
                Object lastUpdateObj = redisService.get(lastUpdateKey);
                long lastUpdateTime = lastUpdateObj != null ? (Long) lastUpdateObj : 0;

                // Check if sync interval has passed since last update
                if (System.currentTimeMillis() - lastUpdateTime >= getSyncIntervalMs()) {
                    syncUrlClicksToDb(shortUrl);
                }
            }
        } catch (Exception e) {
            log.error("Error during URL clicks sync: {}", e.getMessage(), e);
        }
        log.info("Completed URL clicks sync task");
    }

    private void syncUrlClicksToDb(String shortUrl) {
        try {
            // Get URL mapper from DB
            UrlMapper urlMapper = urlService.findByShortUrl(shortUrl);
            if (urlMapper == null) {
                log.warn("URL mapper not found for shortUrl: {}", shortUrl);
                return;
            }

            // Get current clicks from Redis
            Long redisClicks = redisManager.getUrlClicks(shortUrl);
            if (redisClicks == null) {
                log.warn("No click data in Redis for shortUrl: {}", shortUrl);
                return;
            }

            // Update DB if Redis clicks are different
            if (!redisClicks.equals(urlMapper.getUrlClicks())) {
                urlMapper.setUrlClicks(redisClicks);
                urlMapper.setUpdatedAt(new Date());
                urlService.saveUrlMapper(urlMapper);
                
                // Update last sync timestamp with new key format
                String lastUpdateKey = String.format(RedisHelper.URL_CLICKS_KEY_LAST_DBUPDATE, shortUrl);
                redisService.set(lastUpdateKey, System.currentTimeMillis());
                
                log.info("Updated clicks for shortUrl: {} to {}", shortUrl, redisClicks);
            }
        } catch (Exception e) {
            log.error("Error syncing clicks for shortUrl {}:", e.getMessage());
        }
    }

    private String extractShortUrlFromKey(String key) {
        try {
            // Extract shortUrl from Redis key pattern "stats:url:{shortUrl}"
            String[] parts = key.split(":");
            if (parts.length >= 3) {
                return parts[2];
            }
        } catch (Exception e) {
            log.error("Error extracting shortUrl from key {}: {}", key, e.getMessage());
        }
        return null;
    }

    private long getSyncIntervalMs() {
        return clickSyncIntervalMs != null ? clickSyncIntervalMs : SYNC_INTERVAL_MS;
    }
} 