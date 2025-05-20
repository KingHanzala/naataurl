package com.urlshortener.naataurl.persistence.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "url_mapper")
public class UrlMapper{
    @Id
    @Column(name = "url_id")
    private Long urlId;

    @Column(name = "original_url")
    private String originalUrl;

    @Column(name = "short_url")
    private String shortUrl;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "updated_at")  
    private Date updatedAt;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "deleted_at")
    private Date deletedAt;

    @Column(name = "url_clicks", nullable = false)
    private Long urlClicks = 0L;

    public void incrementClicks() {
        this.urlClicks++;
    }

}
