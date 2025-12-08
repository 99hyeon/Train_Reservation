package com.example.japtangjjigae.cart.service;

import com.example.japtangjjigae.cart.dto.AddSeatToCartRequestDTO;
import com.example.japtangjjigae.cart.entity.Cart;
import com.example.japtangjjigae.exception.handler.SeatConflictException;
import com.example.japtangjjigae.global.response.code.TrainResponseCode;
import com.example.japtangjjigae.redis.CartStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartStore cartStore;

    //이건 왜 전역변수? 카트 추가하는거에만 지역변수로 사용해도되지않나?
    private static final long CART_TTL_SECONDS = 60 * 10L; // 예: 10분


    //todo: 해당 좌석 예매 여부 확인 추후 추가
    public void addSeat(AddSeatToCartRequestDTO request) {
        Cart cart = cartStore.getOrCreate(request.getUserId());

        if (cart.isContainsSeat(request.getSeatId())) {
            throw new SeatConflictException(TrainResponseCode.EXIST_SEAT);
        }

        cart.addItem(request.getTrainRunId(), request.getSeatId(), request.getPrice());
        cartStore.save(cart, CART_TTL_SECONDS);
    }

}
