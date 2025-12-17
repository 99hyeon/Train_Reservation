package com.example.japtangjjigae.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AddSeatToCartRequestDTO {

    private List<SeatInfoDTO> seatInfoDTOs;
    @NotNull
    private Long departureStopId;
    @NotNull
    private Long arrivalStopId;

    @Getter
    @Builder
    public static class SeatInfoDTO {
        @NotNull
        private Long seatId;
        @NotNull
        @Min(0)
        private int price;
    }

}