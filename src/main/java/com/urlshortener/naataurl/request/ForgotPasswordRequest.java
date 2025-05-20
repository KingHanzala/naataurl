package com.urlshortener.naataurl.request;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    String userEmail;
    String password;
    String confirmationToken;
}
