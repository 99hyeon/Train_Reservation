package com.example.japtangjjigae.train.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SeatSearchRequestDTO {

    @NotNull
    private Long trainRunId;

    @NotNull
    private String originStationCode;

    @NotNull
    private String destinationStationCode;

    @Min(1)
    private int member;
}
