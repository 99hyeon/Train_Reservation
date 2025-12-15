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
import jakarta.validation.constraints.Min;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
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
    @GetMapping("/trains")
    public ResponseEntity<ApiResponse<TrainSearchResponseDTO>> searchTrain(
        @RequestParam String originStationCode,
        @RequestParam String destinationStationCode,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate runDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime departureTime,
        @RequestParam @Min(1) int member

    ) {
        TrainSearchRequestDTO request = new TrainSearchRequestDTO(
            originStationCode,
            destinationStationCode,
            runDate,
            departureTime,
            member
        );

        return ResponseEntity.ok(
            ApiResponse.from(TrainResponseCode.TRAIN_FOUND, trainService.searchTrain(request)));
    }

    @Operation(
        summary = "좌석 조회 api",
        description = "기차코드, 출발역, 도착역, 가는날, 시간, 인원 조건으로 예매 가능한 좌석 리턴 api"
    )
    @GetMapping("/seats")
    public ResponseEntity<ApiResponse<SeatSearchResponseDTO>> searchSeat(
        @RequestParam Long trainRunId,
        @RequestParam String originStationCode,
        @RequestParam String destinationStationCode,
        @RequestParam @Min(1) int member
    ) {
        SeatSearchRequestDTO request = new SeatSearchRequestDTO(
            trainRunId,
            originStationCode,
            destinationStationCode,
            member
        );

        return ResponseEntity.ok(
            ApiResponse.from(TrainResponseCode.SEAT_FOUND, trainService.searchSeat(request)));
    }

}
