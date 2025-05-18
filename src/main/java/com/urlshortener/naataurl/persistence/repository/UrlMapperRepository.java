package com.urlshortener.naataurl.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.urlshortener.naataurl.persistence.model.UrlMapper;

@Repository
public interface UrlMapperRepository extends JpaRepository<UrlMapper, Long> {
    
    UrlMapper findByOriginalUrl(String originalUrl);
    
    UrlMapper findByShortUrl(String shortUrl);
    
    List<UrlMapper> findByUserId(Long userId);

    @Query(value = "SELECT nextval('o1_url_id_seq')", nativeQuery = true)
    Long getNextId();
}
