package com.example.japtangjjigae.exception;

import com.example.japtangjjigae.global.response.code.ResponseCode;

public class TrainNotFoundException extends RuntimeException {

    private final ResponseCode responseCode;

    public TrainNotFoundException(ResponseCode responseCode){
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

}
