package com.example.japtangjjigae.redis.seathold;

import java.util.List;

public interface SeatHoldStore {

    boolean holdSeat(Long userId, Long trainRunId, List<Long> seatIds, int depOrder, int arrOrder, long ttlSeconds);

    List<SeatHold> findOverLappingHolds(
        Long trainRunId,
        int requestDepartureOrder,
        int requestArrivalOrder
    );

    void releaseSeat(Long trainRunId, List<Long> seatIds, int depOrder, int arrOrder);

    record SeatHold(Long seatId, int departureOrder, int arrivalOrder) {}
}
