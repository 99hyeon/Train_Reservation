package com.example.japtangjjigae.cart.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetCartResponseDTO {

    private List<SeatDTO> seats;

    @Getter
    @Builder
    public static class SeatDTO {
        private String originCode;
        private LocalTime departureAt;
        private String destinationCode;
        private LocalTime arrivalAt;
        private int price;
        private LocalDateTime createdAt;
    }

}
