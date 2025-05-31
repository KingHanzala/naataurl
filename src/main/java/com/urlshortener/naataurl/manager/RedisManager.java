package com.urlshortener.naataurl.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.naataurl.response.GetUserDashboardResponse;
import com.urlshortener.naataurl.service.RedisService;
import com.urlshortener.naataurl.utils.RedisHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class RedisManager {
    private static final Logger log = LoggerFactory.getLogger(RedisManager.class);

    @Value("${dashboard.cache.ttl:3600000}")
    private Long dashboardCacheTtl;

    @Value("${clicks.cache.ttl:86400000}")
    private Long clicksCacheTtl;

    private @Autowired RedisService redisService;
    private @Autowired RedisHelper redisHelper;
    private @Autowired ObjectMapper objectMapper;

    public Set<String> getUrlIds(Long userId) {
        String getUrlIdsKey = redisHelper.getUserMapperKey(userId);
        Set<Object> urlsId = redisService.getSetMembers(getUrlIdsKey);

        if (urlsId == null) {
            return Collections.emptySet();
        }

        return urlsId.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }


    public Long getUrlClicks(String shortUrl) {
        String getUrlClicksKey = redisHelper.getUrlClicksKey(shortUrl);
        Object urlClicks = redisService.get(getUrlClicksKey);
        if(urlClicks == null){
            return null;
        }
        if (urlClicks instanceof Integer) {
            return ((Integer) urlClicks).longValue();
        }
        return (Long) urlClicks;
    }

    public GetUserDashboardResponse getUserDashboardResponse(String userId){
        String dashboardKey = redisHelper.getDashboardKey(userId);
        Object dashboard = redisService.get(dashboardKey);
        if(dashboard == null){
            return null;
        }
        GetUserDashboardResponse getUserDashboardResponse = null;
        if (dashboard instanceof GetUserDashboardResponse) {
            getUserDashboardResponse = (GetUserDashboardResponse) dashboard;
        }
        else {
            // Optional: handle deserialization if redis returns JSON/string instead of object
            try {
                getUserDashboardResponse = objectMapper.readValue(dashboard.toString(), GetUserDashboardResponse.class);
            } catch (Exception e) {
                // Log the exception if needed
                log.error("unable to parse dashboard for user id: {}, Error: {}", userId, e.getMessage());
                return null;
            }
        }
        if(getUserDashboardResponse == null) {
            return null;
        }
        return getUserDashboardResponse;
    }

    public String getOriginalUrl(String shortUrl){
        String originalUrlKey = redisHelper.getOriginalUrlKey(shortUrl);
        Object originalUrl = redisService.get(originalUrlKey);
        return (originalUrl instanceof String) ? (String) originalUrl : null;
    }

    public void addShortUrlToCache(String shortUrl, String originalUrl){
        String originalUrlKey = redisHelper.getOriginalUrlKey(shortUrl);
        redisService.set(originalUrlKey, originalUrl);
    }

    public void addClickToCache(String shortUrl, Long click) {
        String getUrlClicksKey = redisHelper.getUrlClicksKey(shortUrl);
        String lastUpdateKey = redisHelper.getUrlClicksKeyLastDbUpdate(shortUrl);
        if(clicksCacheTtl!=null) {
            redisService.set(getUrlClicksKey, click, clicksCacheTtl, TimeUnit.MILLISECONDS);
            redisService.set(lastUpdateKey, System.currentTimeMillis(), clicksCacheTtl, TimeUnit.MILLISECONDS);
        }
    }

    public void saveUserDashboard(String userId, GetUserDashboardResponse getUserDashboardResponse){
        String dashboardKey = redisHelper.getDashboardKey(userId);
        if(dashboardCacheTtl!=null) {
            redisService.set(dashboardKey, getUserDashboardResponse, dashboardCacheTtl, TimeUnit.MILLISECONDS);
        } else {
            redisService.set(dashboardKey, getUserDashboardResponse);
        }
    }
}
