package com.example.japtangjjigae.exception;

import com.example.japtangjjigae.global.response.code.ResponseCode;
import lombok.Getter;

@Getter
public class TicketNotFoundException extends RuntimeException {

    private final ResponseCode responseCode;

    public TicketNotFoundException(ResponseCode responseCode){
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

}
