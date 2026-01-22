package com.example.japtangjjigae.ticket;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PayStatus {

    READY("결제 준비중"),
    COMPLETE("결제 완료");

    private final String message;
}
