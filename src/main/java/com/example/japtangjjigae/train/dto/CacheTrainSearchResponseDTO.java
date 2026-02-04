package com.example.japtangjjigae.train.dto;

import java.util.List;

public record CacheTrainSearchResponseDTO (
    String originStationCode,
    String destinationStationCode,
    List<CacheTrainInfoDTO> trains

) {}
