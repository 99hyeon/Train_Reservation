package com.example.japtangjjigae.exception;

import com.example.japtangjjigae.global.response.code.ResponseCode;

public class SeatNotFoundException extends RuntimeException {

    private final ResponseCode responseCode;

    public SeatNotFoundException(ResponseCode responseCode){
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

}
