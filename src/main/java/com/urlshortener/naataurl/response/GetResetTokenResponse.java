package com.urlshortener.naataurl.response;

import lombok.Data;

@Data
public class GetResetTokenResponse {
    boolean alreadySent;

    public GetResetTokenResponse( boolean alreadySent) {
        this.alreadySent = alreadySent;
    }
}
