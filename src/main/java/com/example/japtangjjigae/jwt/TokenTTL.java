package com.example.japtangjjigae.jwt;

import java.time.Duration;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenTTL {

    ACCESS(Duration.ofMinutes(30)),
    REFRESH(Duration.ofDays(1));

    private final Duration duration;

    public long seconds() {
        return duration.getSeconds();
    }
}
