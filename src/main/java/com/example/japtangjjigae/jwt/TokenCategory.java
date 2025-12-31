package com.example.japtangjjigae.jwt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TokenCategory {

    ACCESS("access"),
    REFRESH("refresh");

    private final String category;
}
