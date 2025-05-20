package com.urlshortener.naataurl.response;

import lombok.Data;

@Data
public class UserResponse {
    private Long userId;
    private String userName;
    private String email;

    public UserResponse(Long userId, String userName, String email){
        this.userId = userId;
        this.userName = userName;
        this.email = email;
    }
}
