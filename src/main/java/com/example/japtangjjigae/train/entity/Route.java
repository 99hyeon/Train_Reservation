package com.example.japtangjjigae.train.entity;

import com.example.japtangjjigae.station.entity.Station;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_run_id", nullable = false)
    private TrainRun trainRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_station_id", nullable = false)
    private Station originStation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_station_id", nullable = false)
    private Station destinationStation;

    @Column(nullable = false)
    private LocalTime departureAt;

    @Column(nullable = false)
    private LocalTime arrivalAt;

    @Column(nullable = false)
    private int price;

    public static Route createRoute(TrainRun trainRun, Station originStation,
        Station destinationStation, LocalTime departureAt, LocalTime arrivalAt, int price) {
        Route newRoute = new Route();
        newRoute.trainRun = trainRun;
        newRoute.originStation = originStation;
        newRoute.destinationStation = destinationStation;
        newRoute.departureAt = departureAt;
        newRoute.arrivalAt = arrivalAt;
        newRoute.price = price;

        return newRoute;
    }

}
