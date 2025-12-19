package com.example.japtangjjigae.cart.service;

import com.example.japtangjjigae.cart.dto.AddSeatToCartRequestDTO;
import com.example.japtangjjigae.cart.dto.AddSeatToCartRequestDTO.SeatInfoDTO;
import com.example.japtangjjigae.cart.dto.GetCartResponseDTO;
import com.example.japtangjjigae.cart.dto.GetCartResponseDTO.SeatDTO;
import com.example.japtangjjigae.cart.entity.Cart;
import com.example.japtangjjigae.cart.entity.CartItem;
import com.example.japtangjjigae.exception.SeatConflictException;
import com.example.japtangjjigae.exception.TrainNotFoundException;
import com.example.japtangjjigae.global.response.code.TrainResponseCode;
import com.example.japtangjjigae.global.response.code.UserResponseCode;
import com.example.japtangjjigae.redis.cart.CartStore;
import com.example.japtangjjigae.redis.seathold.SeatHoldStore;
import com.example.japtangjjigae.train.entity.TrainStop;
import com.example.japtangjjigae.train.repository.TrainStopRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final long CART_TTL_SECONDS = 60 * 10L; // 예: 10분


    private final CartStore cartStore;
    private final SeatHoldStore seatHoldStore;
    private final TrainStopRepository trainStopRepository;

    public void addSeat(Long userId, AddSeatToCartRequestDTO request) {
        Cart cart = cartStore.getOrCreate(userId);

        TrainStop departureStop = getTrainStop(request.getDepartureStopId());
        TrainStop arrivalStop = getTrainStop(request.getArrivalStopId());

        validateSeatConflicts(cart, request.getSeatInfoDTOs());

        for (SeatInfoDTO seatInfo : request.getSeatInfoDTOs()) {
            holdSeatInCart(cart, departureStop, arrivalStop, seatInfo);
        }
    }

    private TrainStop getTrainStop(Long request) {
        return trainStopRepository.findById(request)
            .orElseThrow(
                () -> new TrainNotFoundException(TrainResponseCode.MATCH_TRAIN_NOT_FOUND)
            );
    }

    private static void validateSeatConflicts(Cart cart, List<SeatInfoDTO> seatInfoDTOS) {
        for (SeatInfoDTO seatInfo : seatInfoDTOS) {
            if (cart.isContainsSeat(seatInfo.getSeatId())) {
                throw new SeatConflictException(TrainResponseCode.EXIST_SEAT);
            }
        }

        Set<Long> seatIds = new HashSet<>();
        for (SeatInfoDTO seatInfo : seatInfoDTOS) {
            if (!seatIds.add(seatInfo.getSeatId())) {
                throw new SeatConflictException(TrainResponseCode.EXIST_SEAT);
            }
        }
    }

    private void holdSeatInCart(Cart cart, TrainStop departureStop, TrainStop arrivalStop,
        SeatInfoDTO seatInfo) {
        Long trainRunId = departureStop.getTrainRun().getId();
        Long seatId = seatInfo.getSeatId();

        cart.addItem(
            trainRunId,
            seatId,
            departureStop.getStation().getCode(),
            departureStop.getDepartureAt(),
            arrivalStop.getStation().getCode(),
            arrivalStop.getArrivalAt(),
            seatInfo.getPrice()
        );

        cartStore.save(cart, CART_TTL_SECONDS);
        seatHoldStore.holdSeat(
            trainRunId,
            seatId,
            departureStop.getStopOrder(),
            arrivalStop.getStopOrder(),
            CART_TTL_SECONDS
        );
    }

    public GetCartResponseDTO getSeat(Long userId) {
        Cart cart = cartStore.getOrCreate(userId);

        List<SeatDTO> seatList = new ArrayList<>();
        if (cart.isSeatsEmpty()) {
            throw new TrainNotFoundException(UserResponseCode.CART_SEAT_NOT_FOUND);
        }

        for (CartItem seat : cart.getSeats()) {
            seatList.add(seatDTOfrom(seat));
        }

        return GetCartResponseDTO.builder()
            .seats(seatList)
            .build();
    }

    private SeatDTO seatDTOfrom(CartItem cartItem) {
        return SeatDTO.builder()
            .originCode(cartItem.getOriginCode())
            .departureAt(cartItem.getDepartureAt())
            .destinationCode(cartItem.getDestinationCode())
            .arrivalAt(cartItem.getArrivalAt())
            .price(cartItem.getPrice())
            .createdAt(cartItem.getCreatedAt())
            .build();
    }
}
