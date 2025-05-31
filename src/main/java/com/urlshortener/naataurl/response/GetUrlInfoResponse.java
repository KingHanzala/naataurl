package com.urlshortener.naataurl.response;

import java.util.Date;
import lombok.Data;

@Data
public class GetUrlInfoResponse {
    private String originalUrl;
    private String shortUrl;
    private Long urlClicks;
    private Date createdDtm;
    private Long userId;

    public GetUrlInfoResponse() {
    }

    public GetUrlInfoResponse(String originalUrl, String shortUrl, Long urlClicks, Date createdDtm, Long userId) {
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
        this.urlClicks = urlClicks;
        this.createdDtm = createdDtm;
        this.userId = userId;
    }
}
