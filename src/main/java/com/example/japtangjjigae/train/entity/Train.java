package com.example.japtangjjigae.train.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Train {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String trainCode;

    public static Train createTrain(String trainCode){
        Train newTrain = new Train();
        newTrain.trainCode = trainCode;

        return newTrain;
    }

    public String getTrainCode() {
        return trainCode;
    }

}
