package com.urlshortener.naataurl.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.urlshortener.naataurl.utils.UrlMapperHelper;

@RestController
@RequestMapping("/api")
public class UrlController {

    private @Autowired UrlMapperHelper urlMapperHelper;

    @PostMapping("/create-url")
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
        String shortUrl = urlMapperHelper.getShortUrl(request.getLongUrl(), userId);
        if(shortUrl == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Short Url Not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(shortUrl);
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
