package com.example.japtangjjigae.redis.seathold;

import java.util.List;

public interface SeatHoldStore {

    void holdSeat(Long trainRunId, Long seatId, int depOrder, int arrOrder, long ttlSeconds);

    List<SeatHold> findOverLappingHolds(
        Long trainRunId,
        int requestDepartureOrder,
        int requestArrivalOrder
    );

    void releaseSeat(Long trainRunId, Long seatId);

    record SeatHold(Long seatId, int departureOrder, int arrivalOrder) {}

}
