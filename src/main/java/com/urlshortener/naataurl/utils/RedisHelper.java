package com.urlshortener.naataurl.utils;

import org.springframework.stereotype.Component;

@Component
public class RedisHelper {
    
    // Key prefixes
    private static final String MAPPER_PREFIX = "mapper:";
    private static final String CLICKS_PREFIX = "stats:";
    private static final String DASHBOARD_PREFIX = "dashboard:";
    private static final String ORIGINAL_URL_PREFIX = "original_url";

    public static final String USER_MAPPER_KEY = MAPPER_PREFIX + "user:%d";
    public static final String URL_MAPPER_KEY = MAPPER_PREFIX + "url:%s";
    public static final String ORIGINAL_URL_KEY = ORIGINAL_URL_PREFIX + "url:%s";
    private static final String DASHBOARD_KEY = DASHBOARD_PREFIX + "user:%s";
    
    // Statistics related keys
    public static final String URL_CLICKS_KEY = CLICKS_PREFIX + "url:%s"; // %s will be shortUrl// %s will be date
    public static final String URL_CLICKS_KEY_LAST_DBUPDATE = CLICKS_PREFIX + "url:%s:timestamp:%d";

    public String getUrlMapperKey(String urlId){
        return String.format(URL_MAPPER_KEY, urlId);
    }
    
    public String getUserMapperKey(Long userId) {
        return String.format(USER_MAPPER_KEY, userId);
    }
    
    public String getUrlClicksKey(String shortUrl) {
        return String.format(URL_CLICKS_KEY, shortUrl);
    }

    public String getUrlClicksKeyLastDbUpdate(String shortUrl, Long timestamp) {
        return String.format(URL_CLICKS_KEY_LAST_DBUPDATE, shortUrl, timestamp);
    }

    public String getOriginalUrlKey(String shortUrl){
        return String.format(ORIGINAL_URL_KEY, shortUrl);
    }

    public String getDashboardKey(String userId) {
        return String.format(DASHBOARD_KEY, userId);
    }
}