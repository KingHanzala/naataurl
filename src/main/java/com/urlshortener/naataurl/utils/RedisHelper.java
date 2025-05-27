package com.urlshortener.naataurl.utils;

import org.springframework.stereotype.Component;

@Component
public class RedisHelper {
    
    // Key prefixes
    private static final String URL_PREFIX = "url:";
    private static final String USER_PREFIX = "user:";
    private static final String AUTH_PREFIX = "auth:";
    private static final String STATS_PREFIX = "stats:";
    private static final String DASHBOARD_PREFIX = "dashboard:";
    
    // URL related keys
    public static final String URL_MAPPER_KEY = URL_PREFIX + "mapper:%s"; // %s will be shortUrl
    public static final String URL_ORIGINAL_KEY = URL_PREFIX + "original:%s"; // %s will be originalUrl
    public static final String URL_USER_KEY = URL_PREFIX + "user:%d"; // %d will be userId
    
    // User related keys
    public static final String USER_PROFILE_KEY = USER_PREFIX + "profile:%d"; // %d will be userId
    public static final String USER_CREDITS_KEY = USER_PREFIX + "credits:%d"; // %d will be userId
    
    // Authentication related keys
    public static final String AUTH_TOKEN_KEY = AUTH_PREFIX + "token:%s-deviceId:%s"; // %s will be token
    public static final String AUTH_REFRESH_KEY = AUTH_PREFIX + "refresh:%s"; // %s will be refreshToken
    
    // Statistics related keys
    public static final String STATS_CLICKS_KEY = STATS_PREFIX + "clicks:%s"; // %s will be shortUrl
    public static final String STATS_DAILY_KEY = STATS_PREFIX + "daily:%s"; // %s will be date
    
    // Helper methods to generate keys
    public String getUrlMapperKey(String shortUrl) {
        return String.format(URL_MAPPER_KEY, shortUrl);
    }
    
    public String getUrlOriginalKey(String originalUrl) {
        return String.format(URL_ORIGINAL_KEY, originalUrl);
    }
    
    public String getUrlUserKey(Long userId) {
        return String.format(URL_USER_KEY, userId);
    }
    
    public String getUserProfileKey(Long userId) {
        return String.format(USER_PROFILE_KEY, userId);
    }
    
    public String getUserCreditsKey(Long userId) {
        return String.format(USER_CREDITS_KEY, userId);
    }
    
    public String getAuthTokenKey(Long userId) {
        return String.format(AUTH_TOKEN_KEY, userId);
    }
    
    public String getAuthRefreshKey(String refreshToken) {
        return String.format(AUTH_REFRESH_KEY, refreshToken);
    }
    
    public String getStatsClicksKey(String shortUrl) {
        return String.format(STATS_CLICKS_KEY, shortUrl);
    }
    
    public String getStatsDailyKey(String date) {
        return String.format(STATS_DAILY_KEY, date);
    }
    
    // Method to check if a key matches a pattern
    public boolean isUrlMapperKey(String key) {
        return key.startsWith(URL_PREFIX + "mapper:");
    }
    
    public boolean isUserProfileKey(String key) {
        return key.startsWith(USER_PREFIX + "profile:");
    }
    
    public boolean isAuthTokenKey(String key) {
        return key.startsWith(AUTH_PREFIX + "token:");
    }
    
    public boolean isStatsKey(String key) {
        return key.startsWith(STATS_PREFIX);
    }
} 