package com.example.japtangjjigae.cart.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Cart {

    private Long userId;
    private List<CartItem> items = new ArrayList<>();
    private LocalDateTime createdAt;

    private Cart(Long userId) {
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
    }

    public static Cart create(Long userId){
        return new Cart(userId);
    }

    public void addItem(Long trainRunId, Long seatId, int price) {
        this.items.add(new CartItem(trainRunId, seatId, price));
    }

    public boolean isContainsSeat(Long seatId) {
        return items.stream()
            .anyMatch(item -> Objects.equals(item.getSeatId(), seatId));
    }

    public void clear(){
        this.items.clear();
    }

}
