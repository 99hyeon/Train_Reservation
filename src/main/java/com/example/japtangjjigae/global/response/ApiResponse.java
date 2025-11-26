package com.example.japtangjjigae.global.response;

import com.example.japtangjjigae.global.response.code.ResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> from(ResponseCode responseCode, T data){
        return new ApiResponse<>(responseCode.getCode(), responseCode.getMessage(), data);
    }

    public static <T> ApiResponse<T> from(ResponseCode responseCode){
        return new ApiResponse<>(responseCode.getCode(), responseCode.getMessage(), null);
    }


}
