package com.example.japtangjjigae.station.repository;

import com.example.japtangjjigae.station.entity.Station;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<Station, Long> {
    Optional<Station> findByCode(String code);
}
