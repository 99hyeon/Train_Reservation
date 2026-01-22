package com.example.japtangjjigae.cart.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CartItem {

    private Long trainRunId;
    private Long seatId;
    private String originCode;
    private LocalTime departureAt;
    private String destinationCode;
    private LocalTime arrivalAt;
    private int price;
    private LocalDateTime createdAt;

    public CartItem(Long trainRunId, Long seatId, String originCode, LocalTime departureAt,
        String destinationCode, LocalTime arrivalAt, int price) {
        this.trainRunId = trainRunId;
        this.seatId = seatId;
        this.originCode = originCode;
        this.departureAt = departureAt;
        this.destinationCode = destinationCode;
        this.arrivalAt = arrivalAt;
        this.price = price;
        this.createdAt = LocalDateTime.now();
    }

}
