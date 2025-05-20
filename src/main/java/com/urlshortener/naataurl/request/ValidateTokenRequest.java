package com.urlshortener.naataurl.request;

import lombok.Data;

@Data
public class ValidateTokenRequest {
    String confirmationToken;
}
