package com.urlshortener.naataurl.response;

import lombok.Data;

@Data
public class AuthCheckResponse {
    private boolean authenticated;

    public AuthCheckResponse(boolean authenticated){
        this.authenticated = authenticated;
    }
}
