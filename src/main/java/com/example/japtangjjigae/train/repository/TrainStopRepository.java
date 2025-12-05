package com.example.japtangjjigae.train.repository;

import com.example.japtangjjigae.train.entity.TrainRun;
import com.example.japtangjjigae.train.entity.TrainStop;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainStopRepository extends JpaRepository<TrainStop, Long> {
    Optional<TrainStop> findByTrainRunAndStation_Code(TrainRun trainRun, String stationCode);
}
