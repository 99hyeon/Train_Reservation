package com.example.japtangjjigae.exception;

import com.example.japtangjjigae.global.response.code.ResponseCode;

public class UserNotFoundException extends RuntimeException {

    private final ResponseCode responseCode;

    public UserNotFoundException(ResponseCode responseCode){
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

}
