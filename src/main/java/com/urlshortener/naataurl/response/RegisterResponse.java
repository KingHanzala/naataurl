package com.urlshortener.naataurl.response;

import lombok.Data;

@Data
public class RegisterResponse {

    String confirmationToken;

    public RegisterResponse(String confirmationToken) {
        this.confirmationToken = confirmationToken;

    }
}
