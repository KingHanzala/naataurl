package com.urlshortener.naataurl.response;

import lombok.Data;

@Data
public class UrlResponse {
    private String shortUrl;

    private Long availableCredits;

    public UrlResponse(String shortUrl, Long availableCredits){

        this.shortUrl = shortUrl;
        this.availableCredits = availableCredits;
    }
}
