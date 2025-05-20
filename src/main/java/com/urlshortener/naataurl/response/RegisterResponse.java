package com.urlshortener.naataurl.response;

import lombok.Data;

@Data
public class RegisterResponse {

    String message;

    public RegisterResponse(String message) {
        this.message = message;

    }
}
