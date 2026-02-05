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
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainCacheService {

    private static final String TRAIN_SEARCH_CACHE = "trainSearchBase";
    private static final String TRAIN_TOTAL_SEATS_CACHE = "trainTotalSeats";

    private final CacheManager cacheManager;
    private final TrainRunRepository trainRunRepository;
    private final TrainStopRepository trainStopRepository;
    private final TrainRepository trainRepository;
    private final SeatRepository seatRepository;

    private final Object[] locks = initLocks(1024);
    private static Object[] initLocks(int size) {
        Object[] arr = new Object[size];

        for (int i = 0; i < size; i++) arr[i] = new Object();

        return arr;
    }

    private Object lockFor(String key) {
        return locks[(key.hashCode() & 0x7fffffff) % locks.length];
    }

    public CacheTrainSearchResponseDTO searchTrainBaseCached(TrainSearchRequestDTO request, int page) {
        String key = trainSearchBaseKey(request, page);

        Cache cache = cacheManager.getCache(TRAIN_SEARCH_CACHE);
        if (cache == null) throw new IllegalStateException("Cache not found: " + TRAIN_SEARCH_CACHE);

        CacheTrainSearchResponseDTO cached = cache.get(key, CacheTrainSearchResponseDTO.class);
        if (cached != null) return cached;

        synchronized (lockFor(key)) {
            CacheTrainSearchResponseDTO cached2 = cache.get(key, CacheTrainSearchResponseDTO.class);
            if (cached2 != null) return cached2;

            CacheTrainSearchResponseDTO loaded = loadTrainSearchBaseFromDB(request, page);
            cache.put(key, loaded);
            return loaded;
        }
    }

    @Transactional(readOnly = true)
    public CacheTrainSearchResponseDTO loadTrainSearchBaseFromDB(TrainSearchRequestDTO request, int page) {
        String originCode = request.getOriginStationCode();
        String destinationCode = request.getDestinationStationCode();

        Pageable pageable = PageRequest.of(page, 10);
        Page<TrainRun> pageTrainRuns = trainRunRepository.findTrainRuns(
            originCode, destinationCode, request.getRunDate(), request.getDepartureTime(), pageable
        );

        List<CacheTrainInfoDTO> trains = new ArrayList<>();
        for (TrainRun trainRun : pageTrainRuns.getContent()) {
            TrainStop departureTrainStop = getTrainStop(trainRun, originCode);
            TrainStop arrivalTrainStop = getTrainStop(trainRun, destinationCode);

            int price = arrivalTrainStop.getCumulativeFare() - departureTrainStop.getCumulativeFare();

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

    public int getTotalSeatsCached2(Long trainId) {
        String key = String.valueOf(trainId);

        Cache cache = cacheManager.getCache(TRAIN_TOTAL_SEATS_CACHE);
        if (cache == null) throw new IllegalStateException("Cache not found: " + TRAIN_TOTAL_SEATS_CACHE);

        Integer cached = cache.get(key, Integer.class);
        if (cached != null) return cached;

        synchronized (lockFor(TRAIN_TOTAL_SEATS_CACHE + ":" + key)) {
            Integer cached2 = cache.get(key, Integer.class);
            if (cached2 != null) return cached2;

            Train train = trainRepository.getReferenceById(trainId);
            int loaded = seatRepository.countByCarriage_Train(train);

            cache.put(key, loaded);
            return loaded;
        }
    }

    private String trainSearchBaseKey(TrainSearchRequestDTO request, int page) {
        return request.getOriginStationCode() + ":" +
            request.getDestinationStationCode() + ":" +
            request.getRunDate() + ":" +
            request.getDepartureTime() + ":" +
            request.getMember() + ":p" + page;
    }

}
