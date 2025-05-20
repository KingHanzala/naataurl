package com.urlshortener.naataurl.response;

import lombok.Data;

@Data
public class ValidateTokenResponse {
    String userEmail;
    boolean tokenExpired;
    String confirmationToken;

    public ValidateTokenResponse(String userEmail, boolean tokenExpired, String confirmationToken) {
        this.userEmail = userEmail;
        this.tokenExpired = tokenExpired;
        this.confirmationToken = confirmationToken;
    }
}
