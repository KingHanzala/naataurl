package com.urlshortener.naataurl.request;

import lombok.Data;

@Data
public class GetResetTokenRequest {
    String email;
}
