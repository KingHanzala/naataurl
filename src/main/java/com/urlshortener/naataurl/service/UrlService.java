package com.urlshortener.naataurl.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.urlshortener.naataurl.persistence.model.UrlMapper;
import com.urlshortener.naataurl.persistence.repository.UrlMapperRepository;

@Service
public class UrlService {

    private @Autowired UrlMapperRepository urlMapperRespository;
    
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

}
