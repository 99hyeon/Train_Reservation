package com.example.japtangjjigae.exception.type;

import com.example.japtangjjigae.exception.handler.SeatConflictException;
import com.example.japtangjjigae.exception.handler.SeatNotFoundException;
import com.example.japtangjjigae.exception.handler.TrainNotFoundException;
import com.example.japtangjjigae.global.response.ApiResponse;
import com.example.japtangjjigae.global.response.code.ResponseCode;
import com.example.japtangjjigae.global.response.code.TrainResponseCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TrainExceptionHandler {

    @ExceptionHandler(TrainNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleTrainNotFound(){
        ResponseCode rc = TrainResponseCode.MATCH_TRAIN_NOT_FOUND;

        return ResponseEntity.status(rc.getCode()).body(ApiResponse.from(rc));
    }

    @ExceptionHandler(SeatNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleSeatNotFound(){
        ResponseCode rc = TrainResponseCode.MATCH_TRAIN_NOT_FOUND;

        return ResponseEntity.status(rc.getCode()).body(ApiResponse.from(rc));
    }

    @ExceptionHandler(SeatConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleSeatConflict(){
        ResponseCode rc = TrainResponseCode.EXIST_SEAT;

        return ResponseEntity.status(rc.getCode()).body(ApiResponse.from(rc));
    }

}
