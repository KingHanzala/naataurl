package com.urlshortener.naataurl.response;

import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private UserResponse userResponse;

    public LoginResponse(String token, UserResponse userResponse) {
        this.userResponse = userResponse;
        this.token = token;
    }
}
