package com.urlshortener.naataurl.response;

import lombok.Data;

@Data
public class ExceptionResponse {
    private String message;

    private int errorCode;
    
    public ExceptionResponse(int code, String message){
        this.errorCode = code;
        this.message = message;
    }
}
