package com.example.japtangjjigae.train.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrainSearchResponseDTO {

    private String originStationCode;
    private String destinationStationCode;

    private List<TrainInfoDTO> trains;

}
