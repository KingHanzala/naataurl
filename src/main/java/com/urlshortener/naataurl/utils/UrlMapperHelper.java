package com.urlshortener.naataurl.utils;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.urlshortener.naataurl.persistence.model.UrlMapper;
import com.urlshortener.naataurl.service.UrlService;

@Component
public class UrlMapperHelper {

    private static final Logger logger = LoggerFactory.getLogger(UrlMapperHelper.class);
    
    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private @Autowired UrlService urlService;
    private @Autowired JwtUtils jwtUtil;
    
    public String getShortUrl(String originalUrl, Long userId){
        UrlMapper urlMapper = urlService.findByOriginalUrl(originalUrl);
        if(urlMapper != null){
            return urlMapper.getShortUrl();
        }
        Long urlId = urlService.getNextUrlId();
        String shortUrl = hashUrl(urlId);
        urlMapper = new UrlMapper();
        urlMapper.setUrlId(urlId);
        urlMapper.setUserId(userId);
        urlMapper.setOriginalUrl(originalUrl);
        urlMapper.setShortUrl(shortUrl);
        urlMapper.setCreatedAt(new Date());
        urlService.saveUrlMapper(urlMapper);
        return shortUrl;
    }

    public String hashUrl(Long value){
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(BASE62.charAt((int) (value % 62)));
            value /= 62;
        }
        return sb.reverse().toString();
    }

    public Long getUserIdFromAuthentication(Authentication authentication) {

        if (authentication == null) {
            logger.error("Authentication is null");
            throw new IllegalArgumentException("Invalid authentication");
        }

        String token = (String) authentication.getPrincipal();

        if (token == null) {
            logger.error("No JWT token found in the authentication");
            throw new IllegalArgumentException("No JWT token found in the authentication");
        }

        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            logger.error("User ID not found in JWT token");
            throw new IllegalArgumentException("User ID not found in JWT token");
        }

        logger.info("Got User ID from JWT: {}", userId);
        return userId;
    }
}
