package com.example.japtangjjigae.order;

import com.example.japtangjjigae.global.util.BaseEntity;
import com.example.japtangjjigae.ticket.PayStatus;
import com.example.japtangjjigae.ticket.entity.Ticket;
import com.example.japtangjjigae.user.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Ticket> tickets = new ArrayList<>();

    @NotNull
    private PayStatus payStatus;

    public static Order createOrder(User user, List<Ticket> tickets, PayStatus payStatus) {
        Order newOrder = new Order();
        newOrder.user = user;
        newOrder.payStatus = payStatus;

        for (Ticket ticket : tickets) {
            newOrder.addTicket(ticket); // ⭐ 핵심
        }

        return newOrder;
    }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
        ticket.assignOrder(this);
    }

    public Long getId(){
        return id;
    }

    public void statusSetApprove(){
        payStatus = PayStatus.COMPLETE;
    }
}
