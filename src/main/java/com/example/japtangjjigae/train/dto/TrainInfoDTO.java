package com.example.japtangjjigae.train.dto;

import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrainInfoDTO {

    private Long trainRunId;
    private String trainCode;
    private LocalTime departureAt;
    private String departureStation;
    private LocalTime arrivalAt;
    private String arrivalStation;
    private int price;

}
