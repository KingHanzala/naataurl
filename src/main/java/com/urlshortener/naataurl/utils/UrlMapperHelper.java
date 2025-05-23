package com.urlshortener.naataurl.utils;

import java.util.Date;

import com.urlshortener.naataurl.persistence.model.User;
import com.urlshortener.naataurl.response.UrlResponse;
import com.urlshortener.naataurl.service.UserService;
import lombok.Getter;
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
    private static final long MAX_ID = 1_000_000_000L;

    @Getter
    private @Autowired UrlService urlService;
    private @Autowired UserService userService;
    private @Autowired JwtUtils jwtUtil;

    public UrlResponse getShortUrl(String originalUrl, Long userId) throws Exception {
        UrlMapper urlMapper = urlService.findByOriginalUrl(originalUrl, userId);
        UrlResponse urlResponse = null;
        User user = userService.findByUserId(userId);
        if (urlMapper != null) {
            urlResponse = new UrlResponse(urlMapper.getShortUrl(), user.getUsageCredits());
            return urlResponse;
        }
        if (user == null) {
            logger.info("User not found");
            return null;
        } else if (user.getUsageCredits() == 0) {
            throw new Exception();
        } else {
            user.decrementCredits();
        }

        Long urlId = urlService.getNextUrlId();
        String shortUrl = null;
        try {
            shortUrl = hashUrl(urlId);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }
        urlMapper = new UrlMapper();
        urlMapper.setUrlId(urlId);
        urlMapper.setUserId(userId);
        urlMapper.setOriginalUrl(originalUrl);
        urlMapper.setShortUrl(shortUrl);
        urlMapper.setCreatedAt(new Date());
        try {
            urlService.saveUrlMapper(urlMapper);
        } catch (Exception e) {
            user.incrementCredits();
            throw new RuntimeException(e);
        }
        userService.saveUser(user);
        return new UrlResponse(shortUrl,user.getUsageCredits());
    }

    public String hashUrl(long id) {
        if (id < 0 || id > MAX_ID) {
            throw new IllegalArgumentException("ID must be between 0 and " + MAX_ID);
        }

        long reversed = MAX_ID - id;

        if (reversed == 0) {
            return String.valueOf(BASE62.charAt(0));
        }

        StringBuilder sb = new StringBuilder();
        while (reversed > 0) {
            int remainder = (int) (reversed % 62);
            sb.insert(0, BASE62.charAt(remainder));
            reversed /= 62;
        }

        return sb.toString();
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
