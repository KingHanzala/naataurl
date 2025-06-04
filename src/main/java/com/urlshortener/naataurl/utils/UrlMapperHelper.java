package com.urlshortener.naataurl.utils;
import lombok.Getter;

import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.urlshortener.naataurl.service.UrlService;

@Component
public class UrlMapperHelper {

    // private static final Logger logger = LoggerFactory.getLogger(UrlMapperHelper.class);
    
    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    @Getter
    private @Autowired UrlService urlService;
    private @Autowired JwtUtils jwtUtil;

    private static final int CODE_LENGTH = 7;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateRandomShortCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(62);
            sb.append(BASE62.charAt(index));
        }
        return sb.toString();
    }

    public Long getUserIdFromAuthentication(Authentication authentication) {

        if (authentication == null) {
            // logger.error("Authentication is null");
            throw new IllegalArgumentException("Invalid authentication");
        }

        String token = (String) authentication.getPrincipal();

        if (token == null) {
            // logger.error("No JWT token found in the authentication");
            throw new IllegalArgumentException("No JWT token found in the authentication");
        }

        Long userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            // logger.error("User ID not found in JWT token");
            throw new IllegalArgumentException("User ID not found in JWT token");
        }

        return userId;
    }
}
