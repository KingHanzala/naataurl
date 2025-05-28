package com.urlshortener.naataurl.service;

import java.util.Date;
import java.util.List;

import com.urlshortener.naataurl.manager.RedisManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.urlshortener.naataurl.persistence.model.UrlMapper;
import com.urlshortener.naataurl.persistence.repository.UrlMapperRepository;

@Service
public class UrlService {

    @Value("${click.sync.interval.ms}")
    public long clickSyncIntervalMs;

    private @Autowired UrlMapperRepository urlMapperRespository;

    private @Autowired RedisManager redisManager;
    
    public UrlMapper findByOriginalUrl(String originalUrl, Long userId){
        return urlMapperRespository.findByOriginalUrlAndUserId(originalUrl, userId);
    }

    public UrlMapper findByShortUrl(String shortUrl){
        return urlMapperRespository.findByShortUrl(shortUrl);
    }

    public List<UrlMapper> findByUserId(Long userId){
        return urlMapperRespository.findByUserId(userId);
    }

    public Long getNextUrlId(){
        return urlMapperRespository.getNextId();
    }

    public void saveUrlMapper(UrlMapper urlMapper){
        urlMapperRespository.save(urlMapper);
    }

    public void syncClicksToDb(UrlMapper urlMapper, String shortUrl){
        Date updatedAt = urlMapper.getUpdatedAt();
        long now = System.currentTimeMillis();

        boolean shouldUpdate = (updatedAt == null) ||
                (now - updatedAt.getTime() >= clickSyncIntervalMs);

        if (shouldUpdate) {
            Long clickCount = redisManager.getUrlClicks(shortUrl);
            if (clickCount != null) {
                urlMapper.setUrlClicks(clickCount);
                saveUrlMapper(urlMapper);
            }
        }
    }

}
