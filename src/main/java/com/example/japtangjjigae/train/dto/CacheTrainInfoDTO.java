package com.example.japtangjjigae.train.dto;

import java.time.LocalTime;

public record CacheTrainInfoDTO (
    Long trainRunId,
    Long trainId,
    String trainCode,
    LocalTime departureAt,
    LocalTime arrivalAt,
    int price,
    int departureOrder,
    int arrivalOrder
) {}
