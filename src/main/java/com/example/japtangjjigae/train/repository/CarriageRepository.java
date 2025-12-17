package com.example.japtangjjigae.train.repository;

import com.example.japtangjjigae.train.entity.Carriage;
import com.example.japtangjjigae.train.entity.Train;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarriageRepository extends JpaRepository<Carriage, Long> {

    List<Carriage> findByTrainOrderByCarriageNumberAsc(Train train);

}
