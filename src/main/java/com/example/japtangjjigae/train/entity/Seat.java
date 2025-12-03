package com.example.japtangjjigae.train.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carriage_id", nullable = false)
    private Carriage carriage;

    @Column(name = "row_no", nullable = false)
    private int rowNumber;

    @Column(nullable = false, length = 1)
    private Character columnCode;

    public static Seat createSeat(Carriage carriage, int rowNumber, Character columnCode){
        Seat newSeat = new Seat();
        newSeat.carriage = carriage;
        newSeat.rowNumber = rowNumber;
        newSeat.columnCode = columnCode;

        return newSeat;
    }
}
