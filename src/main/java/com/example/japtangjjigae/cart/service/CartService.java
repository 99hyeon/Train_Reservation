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
import com.example.japtangjjigae.jwt.TokenUtil;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final long CART_TTL_SECONDS = 60 * 10L; // 예: 10분

    private final TokenUtil tokenUtil;
    private final CartStore cartStore;
    private final SeatHoldStore seatHoldStore;
    private final TrainStopRepository trainStopRepository;

    @Transactional
    public void addSeat(AddSeatToCartRequestDTO request) {
        Long userId = tokenUtil.currentUserId();
        Cart cart = cartStore.getOrCreate(userId);

        TrainStop departureStop = getTrainStop(request.getDepartureStopId());
        TrainStop arrivalStop = getTrainStop(request.getArrivalStopId());
        Long trainRunId = checkTrainRunValidate(departureStop, arrivalStop);

        validateSeatConflicts(cart, request.getSeatInfoDTOs());
        holdSeatsInCart(request, userId, cart, departureStop, arrivalStop, trainRunId);
    }

    private void holdSeatsInCart(AddSeatToCartRequestDTO request, Long userId, Cart cart,
        TrainStop departureStop, TrainStop arrivalStop, Long trainRunId) {
        List<Long> seatIds = request.getSeatInfoDTOs().stream()
            .map(SeatInfoDTO::getSeatId)
            .toList();

        int depOrder = departureStop.getStopOrder();
        int arrOrder = arrivalStop.getStopOrder();

        boolean held = seatHoldStore.holdSeat(userId, trainRunId, seatIds, depOrder, arrOrder,
            CART_TTL_SECONDS);
        if (!held) {
            throw new SeatConflictException(TrainResponseCode.CONFLICT_SEAT);
        }

        try {
            for (SeatInfoDTO seatInfo : request.getSeatInfoDTOs()) {
                cart.addItem(
                    trainRunId,
                    seatInfo.getSeatId(),
                    departureStop.getStation().getCode(),
                    departureStop.getDepartureAt(),
                    arrivalStop.getStation().getCode(),
                    arrivalStop.getArrivalAt(),
                    seatInfo.getPrice()
                );
            }
            cartStore.save(cart, CART_TTL_SECONDS);
        } catch (Exception e) {
            seatHoldStore.releaseSeat(trainRunId, seatIds, depOrder, arrOrder);
            throw e;
        }
    }

    private static Long checkTrainRunValidate(TrainStop departureStop, TrainStop arrivalStop) {
        Long departureRunId = departureStop.getTrainRun().getId();
        Long arrivalRunId = arrivalStop.getTrainRun().getId();

        if (!departureRunId.equals(arrivalRunId)) {
            throw new TrainNotFoundException(TrainResponseCode.MATCH_TRAIN_NOT_FOUND);
        }
        return departureRunId;
    }

    private TrainStop getTrainStop(Long request) {
        return trainStopRepository.findById(request)
            .orElseThrow(
                () -> new TrainNotFoundException(TrainResponseCode.MATCH_TRAIN_NOT_FOUND)
            );
    }

    private void validateSeatConflicts(Cart cart, List<SeatInfoDTO> seatInfoDTOs) {
        Set<Long> requestedSeatIds = new HashSet<>();
        for (SeatInfoDTO s : seatInfoDTOs) {
            requestedSeatIds.add(s.getSeatId());
        }
        if(requestedSeatIds.size() != seatInfoDTOs.size()) {
            throw new SeatConflictException(TrainResponseCode.EXIST_SEAT);
        }

        for (SeatInfoDTO seatInfo : seatInfoDTOs) {
            if (cart.isContainsSeat(seatInfo.getSeatId())) {
                throw new SeatConflictException(TrainResponseCode.EXIST_SEAT);
            }
        }
    }

    public GetCartResponseDTO getSeat() {
        Long userId = tokenUtil.currentUserId();
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
