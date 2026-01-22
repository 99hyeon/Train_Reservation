package com.example.japtangjjigae.exception.handler;

import com.example.japtangjjigae.exception.TicketNotFoundException;
import com.example.japtangjjigae.exception.TokenNotFoundException;
import com.example.japtangjjigae.exception.UserDuplicateException;
import com.example.japtangjjigae.exception.UserNotFoundException;
import com.example.japtangjjigae.global.response.ApiResponse;
import com.example.japtangjjigae.global.response.code.ResponseCode;
import com.example.japtangjjigae.global.response.code.UserResponseCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TokenExceptionHandler {

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(TokenNotFoundException e){
        ResponseCode rc = e.getResponseCode();

        return ResponseEntity.status(rc.getCode()).body(ApiResponse.from(rc));
    }

}
