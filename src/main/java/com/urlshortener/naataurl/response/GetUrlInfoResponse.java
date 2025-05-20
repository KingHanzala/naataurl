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
}
