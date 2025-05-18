package com.urlshortener.naataurl.utils;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.urlshortener.naataurl.persistence.model.UrlMapper;
import com.urlshortener.naataurl.service.UrlService;

@Component
public class UrlMapperHelper {
    
    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private @Autowired UrlService urlService;
    
    public String getShortUrl(String originalUrl){
        UrlMapper urlMapper = urlService.findByOriginalUrl(originalUrl);
        if(urlMapper != null){
            return urlMapper.getShortUrl();
        }
        Long urlId = urlService.getNextUrlId();
        String shortUrl = hashUrl(urlId);
        urlMapper = new UrlMapper();
        urlMapper.setUrlId(urlId);
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
}
