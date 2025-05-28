package com.urlshortener.naataurl.manager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.urlshortener.naataurl.response.GetUrlInfoResponse;
import com.urlshortener.naataurl.service.RedisService;
import com.urlshortener.naataurl.utils.RedisHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RedisManager {
    private static final Logger log = LoggerFactory.getLogger(RedisManager.class);
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

    public void addUrlIdToUserMapping(Long userId, String urlId) {
        String userMapperKey = redisHelper.getUserMapperKey(userId);
        redisService.addToSet(userMapperKey, urlId);
    }

    public void handleCreateUrlMapper(Long urlId, GetUrlInfoResponse getUrlInfoResponse){
        if(urlId == null || getUrlInfoResponse == null) {
            return;
        }
        Long userId = getUrlInfoResponse.getUserId();
        try {
            addUrlIdToUserMapping(userId, urlId.toString());
            saveUrlInfoResponse(urlId.toString(), getUrlInfoResponse);
        } catch(Exception e){
            log.error("Unable to save url info from url id {} in redis, Exception {}",urlId, e.getMessage());
        }
    }

    public GetUrlInfoResponse getUrlInfoResponse(String urlId) {
        String getUrlIdKey = redisHelper.getUrlMapperKey(urlId);
        Object urlMapper = redisService.get(getUrlIdKey);

        if (urlMapper == null) {
            return null;
        }
        GetUrlInfoResponse getUrlInfoResponse = null;
        if (urlMapper instanceof GetUrlInfoResponse) {
            getUrlInfoResponse = (GetUrlInfoResponse) urlMapper;
        }
        else {
            // Optional: handle deserialization if redis returns JSON/string instead of object
            try {
                getUrlInfoResponse = objectMapper.readValue(urlMapper.toString(), GetUrlInfoResponse.class);
            } catch (Exception e) {
                // Log the exception if needed
                log.error("unable to parse url for url id: {}, Error: {}", urlId, e.getMessage());
                return null;
            }
        }
        if(getUrlInfoResponse == null){
            return null;
        }
        String shortUrl = getUrlInfoResponse.getShortUrl();
        Long clicks = getUrlClicks(shortUrl);
        if( clicks != null)
            getUrlInfoResponse.setUrlClicks(clicks);
        return getUrlInfoResponse;
    }


    public Long getUrlClicks(String shortUrl) {
        String getUrlClicksKey = redisHelper.getUrlClicksKey(shortUrl);
        Long clicks = (Long) redisService.get(getUrlClicksKey);
        return clicks != null ? clicks : 0L;
    }

    public void incrementUrlClicks(String shortUrl) {
        String urlClicksKey = redisHelper.getUrlClicksKey(shortUrl);
        redisService.increment(urlClicksKey);
    }

    public void saveUrlInfoResponse(String urlId, GetUrlInfoResponse response) {
        String key = redisHelper.getUrlMapperKey(urlId);
        try {
            // Assuming you store as a JSON string
            String value = objectMapper.writeValueAsString(response);
            redisService.set(key, value);
        } catch (JsonProcessingException e) {
            // Log and handle serialization failure
            throw new RuntimeException("Failed to serialize GetUrlInfoResponse", e);
        }
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


}
