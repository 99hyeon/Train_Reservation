package com.example.japtangjjigae.train.service;

import com.example.japtangjjigae.exception.handler.SeatNotFoundException;
import com.example.japtangjjigae.exception.handler.TrainNotFoundException;
import com.example.japtangjjigae.global.response.code.TrainResponseCode;
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
import java.util.List;
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

    //todo: 좌석 매진 내역 확인 필요
    @Transactional(readOnly = true)
    public TrainSearchResponseDTO searchTrain(TrainSearchRequestDTO request) {
        List<TrainRun> trainRuns = trainRunRepository.findTrainRuns(
            request.getOriginStationCode(),
            request.getDestinationStationCode(), request.getRunDate(),
            request.getDepartureTime());

        if (trainRuns.isEmpty()) {
            throw new TrainNotFoundException(TrainResponseCode.MATCH_TRAIN_NOT_FOUND);
        }

        List<TrainInfoDTO> trains = new ArrayList<>();

        for (TrainRun trainRun : trainRuns) {
            TrainStop departureTrainStop = getTrainStop(trainRun, request.getOriginStationCode());
            TrainStop arrivalTrainStop = getTrainStop(trainRun,
                request.getDestinationStationCode());

            int price =
                arrivalTrainStop.getCumulativeFare() - departureTrainStop.getCumulativeFare();

            trains.add(new TrainInfoDTO(trainRun.getId(), trainRun.getTrain().getTrainCode(),
                departureTrainStop.getDepartureAt(),
                departureTrainStop.getStation().getName(), arrivalTrainStop.getArrivalAt(),
                arrivalTrainStop.getStation().getName(), price));
        }

        return new TrainSearchResponseDTO(trains);
    }

    private TrainStop getTrainStop(TrainRun trainRun, String stationCode) {
        return trainStopRepository.findByTrainRunAndStation_Code(
            trainRun, stationCode).orElseThrow(
            () -> new TrainNotFoundException(TrainResponseCode.MATCH_TRAIN_NOT_FOUND)
        );
    }

    //todo: 좌석 예약 유무 확인
    @Transactional(readOnly = true)
    public SeatSearchResponseDTO searchSeat(SeatSearchRequestDTO request) {
        TrainRun trainRun = trainRunRepository.findById(request.getTrainRunId()).orElseThrow(
            () -> new TrainNotFoundException(TrainResponseCode.MATCH_TRAIN_NOT_FOUND)
        );

        List<Carriage> carriages = carriageRepository.findByTrainOrderByCarriageNumberAsc(
            trainRun.getTrain());

        if (carriages.isEmpty()) {
            throw new SeatNotFoundException(TrainResponseCode.MATCH_SEAT_NOT_FOUND);
        }

        List<CarriageSeatDTO> carriageSeats = new ArrayList<>();
        for (Carriage carriage : carriages) {
            List<Seat> seats = seatRepository.findByCarriageOrderByRowNumberAscColumnCodeAsc(
                carriage);

            carriageSeats.add(toCarriageSeatDTO(carriage, seats));
        }

        TrainSummary trainSummary = TrainSummary.builder()
            .trainCode(trainRun.getTrain().getTrainCode())
            .build();

        return SeatSearchResponseDTO.builder()
            .trainSummary(trainSummary)
            .carriages(carriageSeats)
            .build();
    }

    private CarriageSeatDTO toCarriageSeatDTO(Carriage carriage, List<Seat> seats) {
        List<SeatDTO> seatDTOS = seats.stream()
            .map(this::toSeatDTO)
            .toList();

        return CarriageSeatDTO.builder()
            .carriageNo(carriage.getCarriageNumber())
            .totalSeatCount(seatDTOS.size())
            .availableSeatCount(seatDTOS.size())
            .seats(seatDTOS)
            .build();
    }

    private SeatDTO toSeatDTO(Seat seat) {
        String seatCode = String.valueOf(seat.getRowNumber()) + seat.getColumnCode();

        return SeatDTO.builder()
            .seatId(seat.getId())
            .seatCode(seatCode)
            .row(seat.getRowNumber())
            .column(String.valueOf(seat.getColumnCode()))
            .available(true)
            .build();
    }

}
