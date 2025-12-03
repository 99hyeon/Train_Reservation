package com.example.japtangjjigae.global.init;

import com.example.japtangjjigae.station.entity.Station;
import com.example.japtangjjigae.station.repository.StationRepository;
import com.example.japtangjjigae.train.entity.Carriage;
import com.example.japtangjjigae.train.entity.Route;
import com.example.japtangjjigae.train.entity.Seat;
import com.example.japtangjjigae.train.entity.Train;
import com.example.japtangjjigae.train.entity.TrainRun;
import com.example.japtangjjigae.train.repository.CarriageRepository;
import com.example.japtangjjigae.train.repository.RouteRepository;
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
    private final RouteRepository routeRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (stationRepository.count() > 0 || trainRepository.count() > 0) {
            return;
        }

        initStations();
        initTrainsAndSeats();
        initTrainRunsAndRoutes();
    }

    private void initStations() {
        List<Station> stations = List.of(
            Station.createStation("SEOUL", "서울역"),
            Station.createStation("SUWON", "수원역"),
            Station.createStation("DAEJEON", "대전역"),
            Station.createStation("DONGDAEGU", "동대구역"),
            Station.createStation("ULSAN", "울산역"),
            Station.createStation("BUSAN", "부산역")
        );

        stationRepository.saveAll(stations);
    }

    private void initTrainsAndSeats() {
        // 경부선 하행
        List<Train> trains = List.of(
            Train.createTrain("GB101"),
            Train.createTrain("GB103"),
            Train.createTrain("GB105")
        );
        trainRepository.saveAll(trains);

        List<Carriage> carriages = new ArrayList<>();
        List<Seat> seats = new ArrayList<>();

        char[] columns = {'A', 'B', 'C', 'D'};
        for(Train train : trains){
            for(int carriageNum = 1; carriageNum <= 6; carriageNum++){
                Carriage carriage = Carriage.createCarriage(train, carriageNum);
                carriages.add(carriage);

                for(int row = 1; row <= 10; row++){
                    for(char col : columns){
                        Seat seat = Seat.createSeat(carriage, row, col);
                        seats.add(seat);
                    }
                }
            }
        }

        carriageRepository.saveAll(carriages);
        seatRepository.saveAll(seats);
    }

    private void initTrainRunsAndRoutes() {
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

        List<Route> routes = new ArrayList<>();

        // 경부선 GB101: 서울 -> 수원 -> 대전 -> 동대구 -> 울산 -> 부산
        routes.add(Route.createRoute(gbRun1, seoul, suwon,
            LocalTime.of(9, 0), LocalTime.of(9, 20), 8000));
        routes.add(Route.createRoute(gbRun1, suwon, daejeon,
            LocalTime.of(9, 22), LocalTime.of(10, 17), 13000));
        routes.add(Route.createRoute(gbRun1, daejeon, dongdaegu,
            LocalTime.of(10, 20), LocalTime.of(11, 10), 20000));
        routes.add(Route.createRoute(gbRun1, dongdaegu, ulsan,
            LocalTime.of(11, 13), LocalTime.of(11, 48), 10000));
        routes.add(Route.createRoute(gbRun1, ulsan, busan,
            LocalTime.of(11, 50), LocalTime.of(12, 15), 8000));

        // 경부선 GB103: 서울 -> 수원 -> 대전 -> 동대구 -> 울산 -> 부산
        routes.add(Route.createRoute(gbRun3, seoul, suwon,
            LocalTime.of(10, 0), LocalTime.of(10, 20), 8000));
        routes.add(Route.createRoute(gbRun3, suwon, daejeon,
            LocalTime.of(10, 22), LocalTime.of(11, 17), 13000));
        routes.add(Route.createRoute(gbRun3, daejeon, dongdaegu,
            LocalTime.of(11, 20), LocalTime.of(12, 10), 20000));
        routes.add(Route.createRoute(gbRun3, dongdaegu, ulsan,
            LocalTime.of(12, 13), LocalTime.of(12, 48), 10000));
        routes.add(Route.createRoute(gbRun3, ulsan, busan,
            LocalTime.of(12, 50), LocalTime.of(13, 15), 8000));

        // 경부선 GB105: 서울 -> 수원 -> 대전 -> 동대구 -> 울산 -> 부산
        routes.add(Route.createRoute(gbRun5, seoul, suwon,
            LocalTime.of(11, 0), LocalTime.of(11, 20), 8000));
        routes.add(Route.createRoute(gbRun5, suwon, daejeon,
            LocalTime.of(11, 22), LocalTime.of(12, 17), 13000));
        routes.add(Route.createRoute(gbRun5, daejeon, dongdaegu,
            LocalTime.of(12, 20), LocalTime.of(13, 10), 20000));
        routes.add(Route.createRoute(gbRun5, dongdaegu, ulsan,
            LocalTime.of(13, 13), LocalTime.of(13, 48), 10000));
        routes.add(Route.createRoute(gbRun5, ulsan, busan,
            LocalTime.of(13, 50), LocalTime.of(14, 15), 8000));

        routeRepository.saveAll(routes);
    }
}
