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
public class Carriage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(nullable = false)
    private int carriageNumber;

    public static Carriage createCarriage(Train train, int carriageNumber){
        Carriage newCarriage = new Carriage();
        newCarriage.train = train;
        newCarriage.carriageNumber = carriageNumber;

        return newCarriage;
    }

}
