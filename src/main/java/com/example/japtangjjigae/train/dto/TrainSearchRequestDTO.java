package com.example.japtangjjigae.train.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrainSearchRequestDTO {

    @NotNull
    private String originStationCode;
    @NotNull
    private String destinationStationCode;

    @NotNull
    private LocalDate runDate;
    @NotNull
    private LocalTime departureTime;

    @Min(1)
    private int member;

}
