package com.example.japtangjjigae.train.repository;

import com.example.japtangjjigae.train.entity.Carriage;
import com.example.japtangjjigae.train.entity.Seat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByCarriageOrderByRowNumberAscColumnCodeAsc(Carriage carriage);
}
