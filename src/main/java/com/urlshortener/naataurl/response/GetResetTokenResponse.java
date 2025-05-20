package com.urlshortener.naataurl.response;

import lombok.Data;

@Data
public class GetResetTokenResponse {
    String confirmationToken;
    boolean alreadySent;

    public GetResetTokenResponse(String confirmationToken, boolean alreadySent) {
        this.confirmationToken = confirmationToken;
        this.alreadySent = alreadySent;
    }
}
