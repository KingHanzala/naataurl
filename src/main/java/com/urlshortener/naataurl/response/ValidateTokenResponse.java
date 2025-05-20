package com.urlshortener.naataurl.response;

import lombok.Data;

@Data
public class ValidateTokenResponse {
    String userEmail;
    boolean tokenExpired;
    String newConfirmationToken;

    public ValidateTokenResponse(String userEmail, boolean tokenExpired, String newConfirmationToken) {
        this.userEmail = userEmail;
        this.tokenExpired = tokenExpired;
        this.newConfirmationToken = newConfirmationToken;
    }
}
