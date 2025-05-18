package com.urlshortener.naataurl.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.urlshortener.naataurl.persistence.model.UrlMapper;
import com.urlshortener.naataurl.persistence.repository.UrlMapperRepository;

@Service
public class UrlService {
    private @Autowired UrlMapperRepository urlMapperRespository;
    
    public UrlMapper findByOriginalUrl(String originalUrl){
        UrlMapper urlMapper = urlMapperRespository.findByOriginalUrl(originalUrl);
        return urlMapper;
    }

    public UrlMapper findByShortUrl(String shortUrl){
        UrlMapper urlMapper = urlMapperRespository.findByShortUrl(shortUrl);
        return urlMapper;
    }

    public List<UrlMapper> findByUserId(Long userId){
       List<UrlMapper> urlMapperList = urlMapperRespository.findByUserId(userId);
        return urlMapperList;
    }

    public Long getNextUrlId(){
        return urlMapperRespository.getNextId();
    }

    public UrlMapper saveUrlMapper(UrlMapper urlMapper){
        return urlMapperRespository.save(urlMapper);
    }

}
