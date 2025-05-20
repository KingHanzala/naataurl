package com.urlshortener.naataurl.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.urlshortener.naataurl.utils.UrlMapperHelper;
import com.urlshortener.naataurl.persistence.model.UrlMapper;

@RestController
public class UrlController {

    private @Autowired UrlMapperHelper urlMapperHelper;

    @PostMapping("/api/create-url")
    public ResponseEntity<?> createShortUrl(@RequestBody UrlRequest request, Authentication authentication){
        Long userId = null;
        try{
            userId = urlMapperHelper.getUserIdFromAuthentication(authentication);
        } catch(Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Authentication");
        }
        if(userId==null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Authentication");
        }
        UrlResponse urlResponse = null;
        try {
            urlResponse = urlMapperHelper.getShortUrl(request.getLongUrl(), userId);
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
        UrlMapper urlMapper = urlMapperHelper.getUrlService().findByShortUrl(shortUrl);
        if(urlMapper == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Short URL not found");
        }
        return ResponseEntity.status(HttpStatus.FOUND)
            .header("Location", urlMapper.getOriginalUrl())
            .build();
    }
}

class UrlRequest {
    private String longUrl;

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }
}
