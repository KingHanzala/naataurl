package com.urlshortener.naataurl.controller;

import com.urlshortener.naataurl.manager.RedisManager;
import com.urlshortener.naataurl.manager.UrlManager;
import com.urlshortener.naataurl.request.UrlRequest;
import com.urlshortener.naataurl.response.UrlResponse;
import com.urlshortener.naataurl.service.RedisService;
import com.urlshortener.naataurl.service.UrlService;
import com.urlshortener.naataurl.utils.RedisHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.urlshortener.naataurl.response.ExceptionResponse;
import com.urlshortener.naataurl.utils.UrlMapperHelper;

@RestController
public class UrlController {

    private @Autowired UrlMapperHelper urlMapperHelper;
    private @Autowired UrlManager urlManager;
    private @Autowired RedisService redisService;

    @Value("${frontend.url}")
    private String frontendUrl;

    @PostMapping("/api/create-url")
    public ResponseEntity<?> createShortUrl(@RequestBody UrlRequest request, Authentication authentication) {
        Long userId = null;
        try {
            userId = urlMapperHelper.getUserIdFromAuthentication(authentication);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ExceptionResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid Authentication"));
        }
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ExceptionResponse(HttpStatus.UNAUTHORIZED.value(), "Invalid Authentication"));
        }
        UrlResponse urlResponse = null;
        try {
            urlResponse = urlManager.createUrlMapper(request.getLongUrl(), userId);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ExceptionResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), "Service unavailable"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ExceptionResponse(HttpStatus.SERVICE_UNAVAILABLE.value(), "Usage Credits Limit Exhausted"));
        }
        if (urlResponse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ExceptionResponse(HttpStatus.NOT_FOUND.value(), "Invalid Authentication"));
        }
        return ResponseEntity.status(HttpStatus.OK).body(urlResponse);
    }

    @GetMapping("/{shortUrl}")
    public ResponseEntity<?> getOriginalUrl(@PathVariable String shortUrl){
        
        String originalUrl = urlManager.getOriginalUrl(shortUrl);
        if(originalUrl == null){
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", frontendUrl)
                    .build();
        }
        // Set last operation flag to 1 before processing
        redisService.set(RedisHelper.LAST_OP_FLAG, 1);

        return ResponseEntity.status(HttpStatus.FOUND)
            .header("Location", originalUrl)
            .build();
    }
}
