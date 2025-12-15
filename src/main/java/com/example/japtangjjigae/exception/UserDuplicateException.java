package com.example.japtangjjigae.exception;

import com.example.japtangjjigae.global.response.code.ResponseCode;

public class UserDuplicateException extends RuntimeException {

    private final ResponseCode responseCode;

    public UserDuplicateException(ResponseCode responseCode){
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

}
