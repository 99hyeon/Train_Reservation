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
import jakarta.validation.constraints.Min;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_run_id", nullable = false)
    private TrainRun trainRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    private int stopOrder;

    @Column(nullable = false)
    private LocalTime arrivalAt;

    @Column(nullable = false)
    private LocalTime departureAt;

    @Min(0)
    private int cumulativeFare;

    public static TrainStop createTrainStop(TrainRun trainRun, Station station, int stopOrder,
        LocalTime arrivalAt, LocalTime departureAt, int cumulativeFare) {
        TrainStop newTrainStop = new TrainStop();
        newTrainStop.trainRun = trainRun;
        newTrainStop.station = station;
        newTrainStop.stopOrder = stopOrder;
        newTrainStop.arrivalAt = arrivalAt;
        newTrainStop.departureAt = departureAt;
        newTrainStop.cumulativeFare = cumulativeFare;

        return newTrainStop;
    }

    public Station getStation() { return station; }

    public LocalTime getArrivalAt() {
        return arrivalAt;
    }

    public LocalTime getDepartureAt() {
        return departureAt;
    }

    public int getCumulativeFare(){
        return cumulativeFare;
    }
}
