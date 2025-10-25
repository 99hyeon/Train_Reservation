package com.example.japtangjjigae.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(staticName = "of")
public class ApiResponse<T> {
    private String code;
    private String message;
    private T data;


    public static <T> ApiResponse<T> of(String code, String message, T data){
        return new ApiResponse<>(code, message, data);
    }

}
