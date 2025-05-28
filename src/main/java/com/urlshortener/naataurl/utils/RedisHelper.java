package com.urlshortener.naataurl.utils;

import org.springframework.stereotype.Component;

@Component
public class RedisHelper {
    
    // Key prefixes
    private static final String MAPPER_PREFIX = "mapper:";
    private static final String USER_PREFIX = "user:";
    private static final String AUTH_PREFIX = "auth:";
    private static final String CLICKS_PREFIX = "stats:";
    private static final String DASHBOARD_PREFIX = "dashboard:";
    private static final String ORIGINAL_URL_PREFIX = "original_url";
    
    // URL related keys
    public static final String URL_ORIGINAL_KEY = MAPPER_PREFIX + "original:%s"; // %s will be originalUrl
    public static final String USER_MAPPER_KEY = MAPPER_PREFIX + "user:%d";
    public static final String URL_MAPPER_KEY = MAPPER_PREFIX + "url:%s";
    public static final String ORIGINAL_URL_KEY = ORIGINAL_URL_PREFIX + "url:%s";
    // %d will be userId
    
    // User related keys
    public static final String USER_PROFILE_KEY = USER_PREFIX + "profile:%d"; // %d will be userId
    public static final String USER_CREDITS_KEY = USER_PREFIX + "credits:%d"; // %d will be userId
    
    // Authentication related keys
    public static final String AUTH_TOKEN_KEY = AUTH_PREFIX + "token:%s-deviceId:%s"; // %s will be token
    public static final String AUTH_REFRESH_KEY = AUTH_PREFIX + "refresh:%s"; // %s will be refreshToken
    
    // Statistics related keys
    public static final String URL_CLICKS_KEY = CLICKS_PREFIX + "url:%s"; // %s will be shortUrl
    public static final String STATS_DAILY_KEY = CLICKS_PREFIX + "daily:%s"; // %s will be date

    public String getUrlMapperKey(String urlId){
        return String.format(URL_MAPPER_KEY, urlId);
    }
    public String getUrlOriginalKey(String originalUrl) {
        return String.format(URL_ORIGINAL_KEY, originalUrl);
    }
    
    public String getUserMapperKey(Long userId) {
        return String.format(USER_MAPPER_KEY, userId);
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
    
    public String getUrlClicksKey(String shortUrl) {
        return String.format(URL_CLICKS_KEY, shortUrl);
    }

    public String getOriginalUrlKey(String shortUrl){
        return String.format(ORIGINAL_URL_KEY, shortUrl);
    }
    
    public String getStatsDailyKey(String date) {
        return String.format(STATS_DAILY_KEY, date);
    }
    
    // Method to check if a key matches a pattern
    public boolean isUrlMapperKey(String key) {
        return key.startsWith(MAPPER_PREFIX + "mapper:");
    }
    
    public boolean isUserProfileKey(String key) {
        return key.startsWith(USER_PREFIX + "profile:");
    }
    
    public boolean isAuthTokenKey(String key) {
        return key.startsWith(AUTH_PREFIX + "token:");
    }
    
    public boolean isStatsKey(String key) {
        return key.startsWith(CLICKS_PREFIX);
    }
} 