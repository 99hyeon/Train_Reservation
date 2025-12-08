package com.example.japtangjjigae.cart.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CartItem {

    private Long trainRunId;
    private Long seatId;
    private int price;

    public CartItem(Long trainRunId, Long seatId, int price) {
        this.trainRunId = trainRunId;
        this.seatId = seatId;
        this.price = price;
    }

}
