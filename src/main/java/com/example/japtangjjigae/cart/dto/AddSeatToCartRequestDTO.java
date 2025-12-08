package com.example.japtangjjigae.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AddSeatToCartRequestDTO {

    //todo: 배포할 때 토큰에서 가져오는 걸로 변경
    @NotNull
    private Long userId;
    @NotNull
    private Long trainRunId;
    @NotNull
    private Long seatId;
    @NotNull
    @Min(0)
    private int price;

}
