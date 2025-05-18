package com.urlshortener.naataurl.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.urlshortener.naataurl.utils.UrlMapperHelper;

@RestController("/api")
public class UrlController {

    private @Autowired UrlMapperHelper urlMapperHelper;

    @PostMapping("/create-url")
    public ResponseEntity<?> createShortUrl(@RequestBody String longUrl){

        String shortUrl = urlMapperHelper.getShortUrl(longUrl);
        if(shortUrl == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Short Url Not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(shortUrl);
    }

    
}
