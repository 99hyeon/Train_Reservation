package com.example.japtangjjigae.train.controller;

import com.example.japtangjjigae.global.response.ApiResponse;
import com.example.japtangjjigae.global.response.code.TrainResponseCode;
import com.example.japtangjjigae.train.dto.SeatSearchRequestDTO;
import com.example.japtangjjigae.train.dto.SeatSearchResponseDTO;
import com.example.japtangjjigae.train.dto.TrainSearchRequestDTO;
import com.example.japtangjjigae.train.dto.TrainSearchResponseDTO;
import com.example.japtangjjigae.train.service.TrainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Tag(name = "기차 예매 API", description = "기차 예매 관련 API")
public class TrainController {

    private final TrainService trainService;

    @Operation(
        summary = "기차 조회 api",
        description = "출발역, 도착역, 가는날, 시간, 인원 조건으로 예매 가능한 기차 리턴 api"
    )
    @GetMapping("/train")
    public ResponseEntity<ApiResponse<TrainSearchResponseDTO>> searchTrain(
        @Valid TrainSearchRequestDTO requestDto) {

        return ResponseEntity.ok(ApiResponse.from(TrainResponseCode.TRAIN_FOUND, trainService.searchTrain(requestDto)));
    }

    @Operation(
        summary = "좌석 조회 api",
        description = "기차코드, 출발역, 도착역, 가는날, 시간, 인원 조건으로 예매 가능한 좌석 리턴 api"
    )
    @GetMapping("/seat")
    public ResponseEntity<ApiResponse<SeatSearchResponseDTO>> searchSeat(
        @Valid SeatSearchRequestDTO requestDto) {

        return ResponseEntity.ok(ApiResponse.from(TrainResponseCode.SEAT_FOUND, trainService.searchSeat(requestDto)));
    }

}
