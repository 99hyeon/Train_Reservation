package com.example.japtangjjigae.train.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.japtangjjigae.global.response.code.TrainResponseCode;
import com.example.japtangjjigae.order.Order;
import com.example.japtangjjigae.order.OrderRepository;
import com.example.japtangjjigae.redis.cart.CartStore;
import com.example.japtangjjigae.redis.seathold.SeatHoldStore;
import com.example.japtangjjigae.redis.seathold.SeatHoldStore.SeatHold;
import com.example.japtangjjigae.ticket.PayStatus;
import com.example.japtangjjigae.ticket.entity.Ticket;
import com.example.japtangjjigae.ticket.repository.TicketRepository;
import com.example.japtangjjigae.train.entity.Carriage;
import com.example.japtangjjigae.train.entity.Seat;
import com.example.japtangjjigae.train.entity.TrainRun;
import com.example.japtangjjigae.train.entity.TrainStop;
import com.example.japtangjjigae.train.repository.CarriageRepository;
import com.example.japtangjjigae.train.repository.SeatRepository;
import com.example.japtangjjigae.train.repository.TrainRepository;
import com.example.japtangjjigae.train.repository.TrainRunRepository;
import com.example.japtangjjigae.train.repository.TrainStopRepository;
import com.example.japtangjjigae.user.common.OAuthProvider;
import com.example.japtangjjigae.user.entity.User;
import com.example.japtangjjigae.user.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
class TrainControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TrainRunRepository trainRunRepository;
    @Autowired
    TrainStopRepository trainStopRepository;
    @Autowired
    TrainRepository trainRepository;
    @Autowired
    SeatRepository seatRepository;
    @Autowired
    TicketRepository ticketRepository;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    CarriageRepository carriageRepository;

    @MockitoBean
    SeatHoldStore seatHoldStore;

    @AfterEach
    void tearDown() {
        ticketRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
        Mockito.reset(seatHoldStore);
    }

    @Test
    @DisplayName("조건에 해당되는 기차가 없을 경우")
    void match_train_not_found() throws Exception {
        //when & then
        mockMvc.perform(get("/api/v1/trains")
                .param("originStationCode", "SEOUL")
                .param("destinationStationCode", "BUSAN")
                .param("runDate", LocalDate.now().plusDays(1).toString())
                .param("departureTime", "14:00")
                .param("member", "1")
                .param("page", "0")
            )
            .andExpect(status().is(TrainResponseCode.MATCH_TRAIN_NOT_FOUND.getCode()))
            .andExpect(
                jsonPath("$.message").value(TrainResponseCode.MATCH_TRAIN_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("기차 중 한 대가 매진일 경우 - 좌석 예약으로 매진")
    void search_trains_one_train_is_sold_out() throws Exception {
        //given
        TrainRun soldOutTrainRun = trainRunRepository.findAll().get(0);

        TrainStop departure = trainStopRepository.findByTrainRunAndStation_Code(soldOutTrainRun,
            "SEOUL").orElseThrow();
        TrainStop arrival = trainStopRepository.findByTrainRunAndStation_Code(soldOutTrainRun,
            "BUSAN").orElseThrow();

        List<Seat> seats = seatRepository.findByCarriage_Train(soldOutTrainRun.getTrain());

        User user = userRepository.save(User.createUser(
            "123", OAuthProvider.KAKAO, "홍길동", "010-1234-5678"
        ));

        int price = arrival.getCumulativeFare() - departure.getCumulativeFare();
        List<Ticket> tickets = seats.stream()
            .map(seat -> Ticket.createTicket(
                soldOutTrainRun,
                departure,
                arrival,
                seat,
                price
            ))
            .toList();

        orderRepository.save(Order.createOrder(
            user, tickets, PayStatus.COMPLETE
        ));

        //when & then
        mockMvc.perform(get("/api/v1/trains")
                .param("originStationCode", "SEOUL")
                .param("destinationStationCode", "BUSAN")
                .param("runDate", LocalDate.now().plusDays(1).toString())
                .param("departureTime", "06:00")
                .param("member", "1")
                .param("page", "0")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.trains").isArray())
            .andExpect(jsonPath("$.data.trains[?(@.trainRunId == %d)].soldOut",
                soldOutTrainRun.getId()).value(true));
    }

    @Test
    @DisplayName("기차 중 한 대가 매진일 경우 - 좌석 홀드로 매진")
    void search_trains_one_train_is_sold_out_with_hold_seat() throws Exception {
        //given
        TrainRun soldOutTrainRun = trainRunRepository.findAll().get(0);

        TrainStop departure = trainStopRepository.findByTrainRunAndStation_Code(soldOutTrainRun,
            "SEOUL").orElseThrow();
        TrainStop arrival = trainStopRepository.findByTrainRunAndStation_Code(soldOutTrainRun,
            "BUSAN").orElseThrow();
        int depOrder = departure.getStopOrder();
        int arrOrder = arrival.getStopOrder();

        List<Seat> seats = seatRepository.findByCarriage_Train(soldOutTrainRun.getTrain());

        List<SeatHold> holds = seats.stream()
            .map(seat -> new SeatHold(seat.getId(), depOrder, arrOrder))
            .toList();

        given(seatHoldStore.findOverLappingHolds(eq(soldOutTrainRun.getId()), eq(depOrder),
            eq(arrOrder))).willReturn(holds);

        //when & then
        mockMvc.perform(get("/api/v1/trains")
                .param("originStationCode", "SEOUL")
                .param("destinationStationCode", "BUSAN")
                .param("runDate", LocalDate.now().plusDays(1).toString())
                .param("departureTime", "06:00")
                .param("member", "1")
                .param("page", "0")
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.trains").isArray())
            .andExpect(jsonPath("$.data.trains[?(@.trainRunId == %d)].soldOut",
                soldOutTrainRun.getId()).value(true));
    }

    //--------------------------------------------------------------------


    //좌석 조회
    @Test
    @DisplayName("해당 조건의 운행 기차가 존재 안 할 때")
    void trainRun_not_found() throws Exception {
        //when & then
        mockMvc.perform(get("/api/v1/seats")
                .param("trainRunId", "54321")
                .param("originStationCode", "SEOUL")
                .param("destinationStationCode", "BUSAN")
                .param("member", "1")
            )
            .andExpect(status().is(TrainResponseCode.MATCH_TRAIN_NOT_FOUND.getCode()))
            .andExpect(
                jsonPath("$.message").value(TrainResponseCode.MATCH_TRAIN_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("예약된 좌석 체크가 잘 된다")
    void reservation_seat_check_success() throws Exception {
        //given
        TrainRun trainRun = trainRunRepository.findAll().get(0);

        TrainStop departure = trainStopRepository.findByTrainRunAndStation_Code(trainRun,
            "SEOUL").orElseThrow();
        TrainStop arrival = trainStopRepository.findByTrainRunAndStation_Code(trainRun,
            "BUSAN").orElseThrow();

        List<Carriage> carriages = carriageRepository.findByTrainOrderByCarriageNumberAsc(
            trainRun.getTrain());

        List<Seat> seats = seatRepository.findByCarriageOrderByRowNumberAscColumnCodeAsc(
            carriages.get(0));

        User user = userRepository.save(User.createUser(
            "123", OAuthProvider.KAKAO, "홍길동", "010-1234-5678"
        ));

        int price = arrival.getCumulativeFare() - departure.getCumulativeFare();
        List<Ticket> tickets = seats.stream()
            .map(seat -> Ticket.createTicket(
                trainRun,
                departure,
                arrival,
                seat,
                price
            ))
            .toList();

        orderRepository.save(Order.createOrder(
            user, tickets, PayStatus.COMPLETE
        ));

        //when
        MvcResult result = mockMvc.perform(get("/api/v1/seats")
                .param("trainRunId", trainRun.getId().toString())
                .param("originStationCode", "SEOUL")
                .param("destinationStationCode", "BUSAN")
                .param("member", "1")
            )
            .andExpect(status().is(TrainResponseCode.SEAT_FOUND.getCode()))
            .andReturn();

        //then
        String body = result.getResponse().getContentAsString();
        JsonNode root = new ObjectMapper().readTree(body);

        JsonNode carriagesNode = root.at("/data/carriages");

        for(JsonNode carriageNode : carriagesNode){
            int carriageNo = carriageNode.path("carriageNo").asInt();
            int totalSeatCount = carriageNode.path("totalSeatCount").asInt();
            int availableSeatCount = carriageNode.path("availableSeatCount").asInt();
            JsonNode seatsNode = carriageNode.path("seats");

            assertThat(seatsNode.isArray()).isTrue();

            if(carriageNo == 1){
                assertThat(availableSeatCount).isEqualTo(0);

                for(JsonNode seatNode : seatsNode){
                    boolean available = seatNode.path("available").asBoolean();
                    assertThat(available).isFalse();
                }
            }else{
                assertThat(availableSeatCount).isEqualTo(totalSeatCount);

                for(JsonNode seatNode : seatsNode){
                    boolean available = seatNode.path("available").asBoolean();
                    assertThat(available).isTrue();
                }
            }
        }
    }

    //홀드 좌석 잘 필터링 되는지
    @Test
    @DisplayName("예약된 좌석 체크가 잘 된다")
    void hold_seat_check_success() throws Exception {
        //given
        TrainRun trainRun = trainRunRepository.findAll().get(0);

        TrainStop departure = trainStopRepository.findByTrainRunAndStation_Code(trainRun,
            "SEOUL").orElseThrow();
        TrainStop arrival = trainStopRepository.findByTrainRunAndStation_Code(trainRun,
            "BUSAN").orElseThrow();
        int depOrder = departure.getStopOrder();
        int arrOrder = arrival.getStopOrder();

        List<Carriage> carriages = carriageRepository.findByTrainOrderByCarriageNumberAsc(
            trainRun.getTrain());

        List<Seat> seats = seatRepository.findByCarriageOrderByRowNumberAscColumnCodeAsc(
            carriages.get(0));

        List<SeatHold> holds = seats.stream()
            .map(seat -> new SeatHold(seat.getId(), depOrder, arrOrder))
            .toList();

        given(seatHoldStore.findOverLappingHolds(eq(trainRun.getId()), eq(depOrder),
            eq(arrOrder))).willReturn(holds);

        //when
        MvcResult result = mockMvc.perform(get("/api/v1/seats")
                .param("trainRunId", trainRun.getId().toString())
                .param("originStationCode", "SEOUL")
                .param("destinationStationCode", "BUSAN")
                .param("member", "1")
            )
            .andExpect(status().is(TrainResponseCode.SEAT_FOUND.getCode()))
            .andReturn();

        //then
        String body = result.getResponse().getContentAsString();
        JsonNode root = new ObjectMapper().readTree(body);

        JsonNode carriagesNode = root.at("/data/carriages");

        for(JsonNode carriageNode : carriagesNode){
            int carriageNo = carriageNode.path("carriageNo").asInt();
            int totalSeatCount = carriageNode.path("totalSeatCount").asInt();
            int availableSeatCount = carriageNode.path("availableSeatCount").asInt();
            JsonNode seatsNode = carriageNode.path("seats");

            assertThat(seatsNode.isArray()).isTrue();

            if(carriageNo == 1){
                assertThat(availableSeatCount).isEqualTo(0);

                for(JsonNode seatNode : seatsNode){
                    boolean available = seatNode.path("available").asBoolean();
                    assertThat(available).isFalse();
                }
            }else{
                assertThat(availableSeatCount).isEqualTo(totalSeatCount);

                for(JsonNode seatNode : seatsNode){
                    boolean available = seatNode.path("available").asBoolean();
                    assertThat(available).isTrue();
                }
            }
        }
    }

}