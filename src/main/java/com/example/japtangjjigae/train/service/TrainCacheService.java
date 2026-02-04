package com.example.japtangjjigae.train.service;

import com.example.japtangjjigae.exception.TrainNotFoundException;
import com.example.japtangjjigae.global.response.code.TrainResponseCode;
import com.example.japtangjjigae.train.dto.CacheTrainInfoDTO;
import com.example.japtangjjigae.train.dto.CacheTrainSearchResponseDTO;
import com.example.japtangjjigae.train.dto.TrainSearchRequestDTO;
import com.example.japtangjjigae.train.entity.Train;
import com.example.japtangjjigae.train.entity.TrainRun;
import com.example.japtangjjigae.train.entity.TrainStop;
import com.example.japtangjjigae.train.repository.SeatRepository;
import com.example.japtangjjigae.train.repository.TrainRepository;
import com.example.japtangjjigae.train.repository.TrainRunRepository;
import com.example.japtangjjigae.train.repository.TrainStopRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainCacheService {

    private final TrainRunRepository trainRunRepository;
    private final TrainStopRepository trainStopRepository;
    private final TrainRepository trainRepository;
    private final SeatRepository seatRepository;

    @Cacheable(
        cacheNames = "trainSearchBase",
        key = "#request.originStationCode + ':' + " +
            "#request.destinationStationCode + ':' + " +
            "#request.runDate + ':' + " +
            "#request.departureTime + ':' + " +
            "#request.member + ':p' + #page"
    )
    @Transactional(readOnly = true)
    public CacheTrainSearchResponseDTO searchTrainDB(TrainSearchRequestDTO request, int page) {
        String originCode = request.getOriginStationCode();
        String destinationCode = request.getDestinationStationCode();

        Pageable pageable = PageRequest.of(page, 10);
        Page<TrainRun> pageTrainRuns = trainRunRepository.findTrainRuns(
            originCode,
            destinationCode,
            request.getRunDate(),
            request.getDepartureTime(), pageable);
        List<TrainRun> trainRuns = pageTrainRuns.getContent();

        List<CacheTrainInfoDTO> trains = new ArrayList<>();

        for (TrainRun trainRun : trainRuns) {
            TrainStop departureTrainStop = getTrainStop(trainRun, originCode);
            TrainStop arrivalTrainStop = getTrainStop(trainRun, destinationCode);

            int price =
                arrivalTrainStop.getCumulativeFare() - departureTrainStop.getCumulativeFare();

            trains.add(new CacheTrainInfoDTO(
                trainRun.getId(),
                trainRun.getTrain().getId(),
                trainRun.getTrain().getTrainCode(),
                departureTrainStop.getDepartureAt(),
                arrivalTrainStop.getArrivalAt(),
                price,
                departureTrainStop.getStopOrder(),
                arrivalTrainStop.getStopOrder()
            ));
        }

        return new CacheTrainSearchResponseDTO(originCode, destinationCode, trains);
    }

    private TrainStop getTrainStop(TrainRun trainRun, String stationCode) {
        return trainStopRepository.findByTrainRunAndStation_Code(
            trainRun, stationCode).orElseThrow(
            () -> new TrainNotFoundException(TrainResponseCode.TRAIN_NOT_FOUND)
        );
    }

    @Cacheable(cacheNames = "trainTotalSeats", key = "#trainId")
    public int getTotalSeatsCached(Long trainId) {
        Train train = trainRepository.getReferenceById(trainId);
        return seatRepository.countByCarriage_Train(train);
    }

}
