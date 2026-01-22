package com.example.japtangjjigae.exception;

import com.example.japtangjjigae.global.response.code.ResponseCode;
import lombok.Getter;

@Getter
public class SeatConflictException extends RuntimeException {

    private final ResponseCode responseCode;

    public SeatConflictException(ResponseCode responseCode){
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

}
