package com.example.japtangjjigae.train.service;

import com.example.japtangjjigae.exception.SeatNotFoundException;
import com.example.japtangjjigae.exception.TrainNotFoundException;
import com.example.japtangjjigae.global.response.code.TrainResponseCode;
import com.example.japtangjjigae.redis.seathold.SeatHoldStore;
import com.example.japtangjjigae.redis.seathold.SeatHoldStore.SeatHold;
import com.example.japtangjjigae.ticket.repository.TicketRepository;
import com.example.japtangjjigae.train.dto.CacheTrainInfoDTO;
import com.example.japtangjjigae.train.dto.CacheTrainSearchResponseDTO;
import com.example.japtangjjigae.train.dto.SeatSearchRequestDTO;
import com.example.japtangjjigae.train.dto.SeatSearchResponseDTO;
import com.example.japtangjjigae.train.dto.SeatSearchResponseDTO.CarriageSeatDTO;
import com.example.japtangjjigae.train.dto.SeatSearchResponseDTO.SeatDTO;
import com.example.japtangjjigae.train.dto.SeatSearchResponseDTO.TrainSummary;
import com.example.japtangjjigae.train.dto.TrainInfoDTO;
import com.example.japtangjjigae.train.dto.TrainSearchRequestDTO;
import com.example.japtangjjigae.train.dto.TrainSearchResponseDTO;
import com.example.japtangjjigae.train.entity.Carriage;
import com.example.japtangjjigae.train.entity.Seat;
import com.example.japtangjjigae.train.entity.TrainRun;
import com.example.japtangjjigae.train.entity.TrainStop;
import com.example.japtangjjigae.train.repository.CarriageRepository;
import com.example.japtangjjigae.train.repository.SeatRepository;
import com.example.japtangjjigae.train.repository.TrainRunRepository;
import com.example.japtangjjigae.train.repository.TrainStopRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainService {

    private final TrainRunRepository trainRunRepository;
    private final TrainStopRepository trainStopRepository;
    private final CarriageRepository carriageRepository;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final SeatHoldStore seatHoldStore;
    private final TrainCacheService trainCacheService;

    @Transactional(readOnly = true)
    public TrainSearchResponseDTO searchTrain(TrainSearchRequestDTO request, int page) {
        CacheTrainSearchResponseDTO caches = trainCacheService.searchTrainDB(request, page);

        List<TrainInfoDTO> trains = new ArrayList<>();
        for (CacheTrainInfoDTO cache : caches.trains()) {
            TrainRun trainRun = trainRunRepository.getReferenceById(cache.trainRunId());

            boolean soldOut = isSoldOut(
                cache.trainRunId(),
                cache.trainId(),
                cache.departureOrder(),
                cache.arrivalOrder()
            );

            trains.add(new TrainInfoDTO(
                trainRun.getId(),
                trainRun.getTrain().getTrainCode(),
                cache.departureAt(),
                cache.arrivalAt(),
                cache.price(),
                soldOut
            ));
        }

        return new TrainSearchResponseDTO(caches.originStationCode(),
            caches.destinationStationCode(), trains);
    }

    private boolean isSoldOut(Long trainRunId, Long trainId, int departureOrder, int arrivalOrder) {
        int totalSeats = trainCacheService.getTotalSeatsCached(trainId);
        int bookedSeats = ticketRepository.countBookedSeatsInSection(trainRunId, departureOrder,
            arrivalOrder);

        List<SeatHold> holdSeats = seatHoldStore.findOverLappingHolds(trainRunId,
            departureOrder, arrivalOrder);

        int holdSeatCount = (int) holdSeats.stream()
            .map(SeatHold::seatId)
            .distinct()
            .count();

        return totalSeats <= (bookedSeats + holdSeatCount);
    }

    private TrainStop getTrainStop(TrainRun trainRun, String stationCode) {
        return trainStopRepository.findByTrainRunAndStation_Code(
            trainRun, stationCode).orElseThrow(
            () -> new TrainNotFoundException(TrainResponseCode.TRAIN_NOT_FOUND)
        );
    }

    @Transactional(readOnly = true)
    public SeatSearchResponseDTO searchSeat(SeatSearchRequestDTO request) {
        TrainRun trainRun = trainRunRepository.findById(request.getTrainRunId()).orElseThrow(
            () -> new TrainNotFoundException(TrainResponseCode.MATCH_TRAIN_NOT_FOUND)
        );

        TrainStop departureStop = getTrainStop(trainRun, request.getOriginStationCode());
        TrainStop arrivalStop = getTrainStop(trainRun, request.getDestinationStationCode());

        int departureOrder = departureStop.getStopOrder();
        int arrivalOrder = arrivalStop.getStopOrder();

        List<Long> bookedSeatIdList = ticketRepository.findBookedSeatIdsInSection(trainRun,
            departureOrder, arrivalOrder);
        Set<Long> bookedSeatIds = new HashSet<>(bookedSeatIdList);

        List<SeatHold> holdSeats = seatHoldStore.findOverLappingHolds(trainRun.getId(),
            departureOrder, arrivalOrder);
        Set<Long> holdSeatIds = holdSeats.stream()
            .map(SeatHold::seatId)
            .collect(Collectors.toSet());

        List<Carriage> carriages = carriageRepository.findByTrainOrderByCarriageNumberAsc(
            trainRun.getTrain());

        if (carriages.isEmpty()) {
            throw new SeatNotFoundException(TrainResponseCode.MATCH_SEAT_NOT_FOUND);
        }

        List<Seat> allSeats = seatRepository.findByCarriageInOrderByCarriage_IdAscRowNumberAscColumnCodeAsc(
            carriages);
        Map<Long, List<Seat>> seatsByCarriageId = allSeats.stream()
            .collect(Collectors.groupingBy(seat -> seat.getCarriage().getId(), HashMap::new,
                Collectors.toList()));
        List<CarriageSeatDTO> carriageSeats = new ArrayList<>();
        for (Carriage carriage : carriages) {
            List<Seat> seats = seatsByCarriageId.getOrDefault(carriage.getId(), List.of());
            carriageSeats.add(toCarriageSeatDTOFrom(carriage, seats, bookedSeatIds, holdSeatIds));
        }

        TrainSummary trainSummary = TrainSummary.builder()
            .trainCode(trainRun.getTrain().getTrainCode())
            .originStopId(departureStop.getId())
            .originCode(request.getOriginStationCode())
            .destinationStopId(arrivalStop.getId())
            .destinationCode(request.getDestinationStationCode())
            .build();

        return SeatSearchResponseDTO.builder()
            .trainSummary(trainSummary)
            .carriages(carriageSeats)
            .build();
    }

    private CarriageSeatDTO toCarriageSeatDTOFrom(Carriage carriage, List<Seat> seats,
        Set<Long> bookedSeatIds, Set<Long> holdSeatIds) {
        List<SeatDTO> seatDTOS = seats.stream()
            .map(seat -> toSeatDTOFrom(seat, bookedSeatIds, holdSeatIds))
            .toList();

        int availableSeatCount = (int) seatDTOS.stream()
            .filter(SeatDTO::isAvailable)
            .count();

        return CarriageSeatDTO.builder()
            .carriageNo(carriage.getCarriageNumber())
            .totalSeatCount(seatDTOS.size())
            .availableSeatCount(availableSeatCount)
            .seats(seatDTOS)
            .build();
    }

    private SeatDTO toSeatDTOFrom(Seat seat, Set<Long> bookedSeatIds, Set<Long> holdSeatIds) {
        String seatCode = String.valueOf(seat.getRowNumber()) + seat.getColumnCode();
        boolean booked = bookedSeatIds.contains(seat.getId());
        boolean held = holdSeatIds.contains(seat.getId());
        boolean available = !(booked || held);

        return SeatDTO.builder()
            .seatId(seat.getId())
            .seatCode(seatCode)
            .row(seat.getRowNumber())
            .column(String.valueOf(seat.getColumnCode()))
            .available(available)
            .build();
    }

}
