package com.example.japtangjjigae.ticket.entity;

import com.example.japtangjjigae.global.util.BaseEntity;
import com.example.japtangjjigae.order.Order;
import com.example.japtangjjigae.train.entity.Seat;
import com.example.japtangjjigae.train.entity.TrainRun;
import com.example.japtangjjigae.train.entity.TrainStop;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ticket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "train_run_id", nullable = false)
    private TrainRun trainRun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_stop_id", nullable = false)
    private TrainStop departureStop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrival_stop_id", nullable = false)
    private TrainStop arrivalStop;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(nullable = false)
    @Min(0)
    private int amount;

    public static Ticket createTicket(TrainRun trainRun, TrainStop departureStop,
        TrainStop arrivalStop, Seat seat, int amount) {
        Ticket newTicket = new Ticket();
        newTicket.trainRun = trainRun;
        newTicket.departureStop = departureStop;
        newTicket.arrivalStop = arrivalStop;
        newTicket.seat = seat;
        newTicket.amount = amount;

        return newTicket;
    }

    public void assignOrder(Order order) {
        this.order = order;
    }
}
