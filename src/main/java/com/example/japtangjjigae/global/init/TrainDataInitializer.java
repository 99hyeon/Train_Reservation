package com.example.japtangjjigae.global.init;

import com.example.japtangjjigae.station.entity.Station;
import com.example.japtangjjigae.station.repository.StationRepository;
import com.example.japtangjjigae.train.entity.Carriage;
import com.example.japtangjjigae.train.entity.Seat;
import com.example.japtangjjigae.train.entity.Train;
import com.example.japtangjjigae.train.entity.TrainRun;
import com.example.japtangjjigae.train.entity.TrainStop;
import com.example.japtangjjigae.train.repository.CarriageRepository;
import com.example.japtangjjigae.train.repository.TrainStopRepository;
import com.example.japtangjjigae.train.repository.SeatRepository;
import com.example.japtangjjigae.train.repository.TrainRepository;
import com.example.japtangjjigae.train.repository.TrainRunRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TrainDataInitializer implements CommandLineRunner {

    private final TrainRepository trainRepository;
    private final CarriageRepository carriageRepository;
    private final SeatRepository seatRepository;
    private final TrainRunRepository trainRunRepository;
    private final StationRepository stationRepository;
    private final TrainStopRepository trainStopRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (stationRepository.count() > 0 || trainRepository.count() > 0) {
            return;
        }

        initStations();
        initTrainsAndSeats();
        initTrainRunsAndTrainStop();
    }

    private void initStations() {
        List<Station> stations = List.of(Station.createStation("SEOUL", "서울역"),
            Station.createStation("SUWON", "수원역"), Station.createStation("DAEJEON", "대전역"),
            Station.createStation("DONGDAEGU", "동대구역"), Station.createStation("ULSAN", "울산역"),
            Station.createStation("BUSAN", "부산역"));

        stationRepository.saveAll(stations);
    }

    private void initTrainsAndSeats() {
        // 경부선 하행
        List<Train> trains = List.of(Train.createTrain("GB101"), Train.createTrain("GB103"),
            Train.createTrain("GB105"));
        trainRepository.saveAll(trains);

        List<Carriage> carriages = new ArrayList<>();
        List<Seat> seats = new ArrayList<>();

        char[] columns = {'A', 'B', 'C', 'D'};
        for (Train train : trains) {
            for (int carriageNum = 1; carriageNum <= 6; carriageNum++) {
                Carriage carriage = Carriage.createCarriage(train, carriageNum);
                carriages.add(carriage);

                for (int row = 1; row <= 10; row++) {
                    for (char col : columns) {
                        Seat seat = Seat.createSeat(carriage, row, col);
                        seats.add(seat);
                    }
                }
            }
        }

        carriageRepository.saveAll(carriages);
        seatRepository.saveAll(seats);
    }

    private void initTrainRunsAndTrainStop() {
        LocalDate runDate = LocalDate.now().plusDays(1);

        Train gb101 = trainRepository.findByTrainCode("GB101").orElseThrow();
        Train gb103 = trainRepository.findByTrainCode("GB103").orElseThrow();
        Train gb105 = trainRepository.findByTrainCode("GB105").orElseThrow();

        TrainRun gbRun1 = TrainRun.createTrainRun(gb101, runDate);
        TrainRun gbRun3 = TrainRun.createTrainRun(gb103, runDate);
        TrainRun gbRun5 = TrainRun.createTrainRun(gb105, runDate);

        trainRunRepository.saveAll(List.of(gbRun1, gbRun3, gbRun5));

        Station seoul = stationRepository.findByCode("SEOUL").orElseThrow();
        Station suwon = stationRepository.findByCode("SUWON").orElseThrow();
        Station daejeon = stationRepository.findByCode("DAEJEON").orElseThrow();
        Station dongdaegu = stationRepository.findByCode("DONGDAEGU").orElseThrow();
        Station ulsan = stationRepository.findByCode("ULSAN").orElseThrow();
        Station busan = stationRepository.findByCode("BUSAN").orElseThrow();

        List<TrainStop> trainStops = new ArrayList<>();

        // 경부선 GB101: 서울 -> 수원 -> 대전 -> 동대구 -> 울산 -> 부산
        trainStops.add(
            TrainStop.createTrainStop(gbRun1, seoul, 1, LocalTime.of(8, 40), LocalTime.of(9, 0), 0));
        trainStops.add(
            TrainStop.createTrainStop(gbRun1, suwon, 2, LocalTime.of(9, 20), LocalTime.of(9, 23),
                8000));
        trainStops.add(
            TrainStop.createTrainStop(gbRun1, daejeon, 3, LocalTime.of(9, 55), LocalTime.of(9, 58),
                21000));
        trainStops.add(
            TrainStop.createTrainStop(gbRun1, dongdaegu, 4, LocalTime.of(10, 48), LocalTime.of(10, 51),
                41000));
        trainStops.add(
            TrainStop.createTrainStop(gbRun1, ulsan, 5, LocalTime.of(11, 22), LocalTime.of(11, 24),
                50000));
        trainStops.add(
            TrainStop.createTrainStop(gbRun1, busan, 6, LocalTime.of(11, 49), LocalTime.of(12, 10),
                58000));

        // 경부선 GB103: 서울 -> 수원 -> 대전 -> 동대구 -> 울산 -> 부산
        trainStops.add(
            TrainStop.createTrainStop(gbRun3, seoul, 1, LocalTime.of(9, 40), LocalTime.of(10, 0), 0));
        trainStops.add(
            TrainStop.createTrainStop(gbRun3, suwon, 2, LocalTime.of(10, 20), LocalTime.of(10, 23),
                8000));
        trainStops.add(
            TrainStop.createTrainStop(gbRun3, daejeon, 3, LocalTime.of(10, 55), LocalTime.of(10, 58),
                21000));
        trainStops.add(
            TrainStop.createTrainStop(gbRun3, dongdaegu, 4, LocalTime.of(11, 48), LocalTime.of(11, 51),
                41000));
        trainStops.add(
            TrainStop.createTrainStop(gbRun3, ulsan, 5, LocalTime.of(12, 22), LocalTime.of(12, 24),
                50000));
        trainStops.add(
            TrainStop.createTrainStop(gbRun3, busan, 6, LocalTime.of(12, 49), LocalTime.of(13, 10),
                58000));

        // 경부선 GB105: 서울 -> 수원 -> 대전 -> 동대구 -> 울산 -> 부산
        trainStops.add(
            TrainStop.createTrainStop(gbRun5, seoul, 1, LocalTime.of(10, 40), LocalTime.of(11, 0), 0));
        trainStops.add(
            TrainStop.createTrainStop(gbRun5, suwon, 2, LocalTime.of(11, 20), LocalTime.of(11, 23),
                8000));
        trainStops.add(
            TrainStop.createTrainStop(gbRun5, daejeon, 3, LocalTime.of(11, 55), LocalTime.of(11, 58),
                21000));
        trainStops.add(
            TrainStop.createTrainStop(gbRun5, dongdaegu, 4, LocalTime.of(12, 48), LocalTime.of(12, 51),
                41000));
        trainStops.add(
            TrainStop.createTrainStop(gbRun5, ulsan, 5, LocalTime.of(13, 22), LocalTime.of(13, 24),
                50000));
        trainStops.add(
            TrainStop.createTrainStop(gbRun5, busan, 6, LocalTime.of(13, 49), LocalTime.of(14, 10),
                58000));

        trainStopRepository.saveAll(trainStops);
    }
}
