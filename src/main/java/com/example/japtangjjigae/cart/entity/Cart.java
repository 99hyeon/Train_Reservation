package com.example.japtangjjigae.cart.entity;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Cart {

    private Long userId;
    private List<CartItem> seats = new ArrayList<>();

    private Cart(Long userId) {
        this.userId = userId;
    }

    public static Cart create(Long userId) {
        return new Cart(userId);
    }

    public void addItem(Long trainRunId, Long seatId, String originCode, LocalTime departureAt,
        String destinationCode, LocalTime arrivalAt, int price) {
        this.seats.add(
            new CartItem(trainRunId, seatId, originCode, departureAt, destinationCode, arrivalAt,
                price));
    }

    public boolean isContainsSeat(Long seatId) {
        return seats.stream()
            .anyMatch(item -> Objects.equals(item.getSeatId(), seatId));
    }

    public boolean isSeatsEmpty() {
        return seats.isEmpty();
    }


    public void clear() {
        this.seats.clear();
    }

}
