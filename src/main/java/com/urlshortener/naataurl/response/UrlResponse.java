package com.urlshortener.naataurl.response;

import lombok.Data;

@Data
public class UrlResponse {
    private String shortUrl;

    public UrlResponse(String shortUrl){
        this.shortUrl = shortUrl;
    }
}
