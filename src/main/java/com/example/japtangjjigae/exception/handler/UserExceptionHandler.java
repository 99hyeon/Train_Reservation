package com.example.japtangjjigae.exception.handler;

import com.example.japtangjjigae.exception.TicketNotFoundException;
import com.example.japtangjjigae.exception.UserDuplicateException;
import com.example.japtangjjigae.exception.UserNotFoundException;
import com.example.japtangjjigae.global.response.ApiResponse;
import com.example.japtangjjigae.global.response.code.ResponseCode;
import com.example.japtangjjigae.global.response.code.UserResponseCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFound(){
        ResponseCode rc = UserResponseCode.USER_NOT_FOUND;

        return ResponseEntity.status(rc.getCode()).body(ApiResponse.from(rc));
    }

    @ExceptionHandler(UserDuplicateException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserDuplicate(UserDuplicateException e){
        ResponseCode rc = e.getResponseCode();

        return ResponseEntity.status(rc.getCode()).body(ApiResponse.from(rc));
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleTicketNotFound(TicketNotFoundException e){
        ResponseCode rc = e.getResponseCode();

        return ResponseEntity.status(rc.getCode()).body(ApiResponse.from(rc));
    }

}
