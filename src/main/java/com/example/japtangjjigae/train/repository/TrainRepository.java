package com.example.japtangjjigae.train.repository;

import com.example.japtangjjigae.train.entity.Train;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainRepository extends JpaRepository<Train, Long> {
    Optional<Train> findByTrainCode(String trainCode);

}
