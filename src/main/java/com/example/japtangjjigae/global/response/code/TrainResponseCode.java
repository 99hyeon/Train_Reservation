package com.example.japtangjjigae.global.response.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TrainResponseCode implements ResponseCode {

    TRAIN_FOUND(200, "조건에 해당되는 기차 존재"),
    SEAT_FOUND(200, "조건에 해당되는 좌석 존재"),
    MATCH_TRAIN_NOT_FOUND(404, "조건에 해당하는 기차 없음"),
    MATCH_SEAT_NOT_FOUND(404, "조건에 해당하는 좌석 없음"),
    TRAIN_NOT_FOUND(404, "기차 존재 안 함");

    private final int code;
    private final String message;
}
