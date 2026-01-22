package com.example.japtangjjigae.train.dto;

import java.time.LocalTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeatSearchResponseDTO {

    private TrainSummary trainSummary;

    private List<CarriageSeatDTO> carriages;

    @Getter
    @Builder
    public static class TrainSummary {
        private String trainCode;
        private Long originStopId;
        private Long destinationStopId;
        private String originCode;
        private LocalTime departureAt;
        private String destinationCode;
        private LocalTime arrivalAt;
    }

    @Getter
    @Builder
    public static class CarriageSeatDTO {
        private int carriageNo;
        private int totalSeatCount;
        private int availableSeatCount;
        private List<SeatDTO> seats;
    }

    @Getter
    @Builder
    public static class SeatDTO {
        private Long seatId;
        private String seatCode;
        private Integer row;
        private String column;
        private boolean available; //enum 고려

    }

}



