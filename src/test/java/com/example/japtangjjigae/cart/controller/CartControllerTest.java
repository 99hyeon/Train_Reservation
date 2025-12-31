package com.example.japtangjjigae.cart.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.japtangjjigae.cart.entity.Cart;
import com.example.japtangjjigae.global.response.code.TrainResponseCode;
import com.example.japtangjjigae.global.response.code.UserResponseCode;
import com.example.japtangjjigae.oauth2.CustomOAuth2User;
import com.example.japtangjjigae.redis.cart.CartStore;
import com.example.japtangjjigae.redis.seathold.SeatHoldStore;
import com.example.japtangjjigae.train.entity.Seat;
import com.example.japtangjjigae.train.entity.TrainRun;
import com.example.japtangjjigae.train.entity.TrainStop;
import com.example.japtangjjigae.train.repository.SeatRepository;
import com.example.japtangjjigae.train.repository.TrainRunRepository;
import com.example.japtangjjigae.train.repository.TrainStopRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class CartControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TrainRunRepository trainRunRepository;
    @Autowired
    TrainStopRepository trainStopRepository;
    @Autowired
    SeatRepository seatRepository;

    @MockitoBean
    SeatHoldStore seatHoldStore;
    @MockitoBean
    CartStore cartStore;


    @AfterEach
    void tearDown() {
        Mockito.reset(seatHoldStore);
        Mockito.reset(cartStore);
    }

    @Test
    @DisplayName("존재하지 않는 정차역일 경우")
    void trainStop_not_found() throws Exception {
        //given
        CustomOAuth2User principal = Mockito.mock(CustomOAuth2User.class);
        Mockito.when(principal.getUserId()).thenReturn(1L);

        Authentication auth = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String body = objectMapper.writeValueAsString(Map.of(
            "seatInfoDTOs", List.of(
                Map.of("seatId", 0L, "price", 12000),
                Map.of("seatId", 1L, "price", 12000)
            ),
            "departureStopId", 321L,
            "arrivalStopId", 321L
        ));

        //when & then
        mockMvc.perform(post("/api/v1/cart")
                .with(authentication(auth))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().is(TrainResponseCode.MATCH_TRAIN_NOT_FOUND.getCode()))
            .andExpect(
                jsonPath("$.message").value(TrainResponseCode.MATCH_TRAIN_NOT_FOUND.getMessage()));
    }

    @Test
    @Transactional
    @DisplayName("장바구니에 이미 존재하는 좌석일 경우")
    void cart_conflict_seat() throws Exception {
        //given
        CustomOAuth2User principal = Mockito.mock(CustomOAuth2User.class);
        Mockito.when(principal.getUserId()).thenReturn(1L);

        Authentication auth = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        TrainRun trainRun = trainRunRepository.findAll().get(0);
        TrainStop departure = trainStopRepository.findByTrainRunAndStation_Code(trainRun,
            "SEOUL").orElseThrow();
        TrainStop arrival = trainStopRepository.findByTrainRunAndStation_Code(trainRun,
            "BUSAN").orElseThrow();

        List<Seat> seats = seatRepository.findByCarriage_Train(trainRun.getTrain());
        Long seatId = seats.get(0).getId();

        Cart cart = new Cart();
        int price = arrival.getCumulativeFare() - departure.getCumulativeFare();
        cart.addItem(
            trainRun.getId(),
            seatId,
            departure.getStation().getCode(),
            departure.getDepartureAt(),
            arrival.getStation().getCode(),
            arrival.getArrivalAt(),
            price
        );

        Mockito.when(cartStore.getOrCreate(principal.getUserId())).thenReturn(cart);

        String body = objectMapper.writeValueAsString(Map.of(
            "seatInfoDTOs", List.of(
                Map.of("seatId", seatId, "price", price),
                Map.of("seatId", 5L, "price", price)
            ),
            "departureStopId", departure.getId(),
            "arrivalStopId", arrival.getId()
        ));

        //when & then
        mockMvc.perform(post("/api/v1/cart")
                .with(authentication(auth))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().is(TrainResponseCode.EXIST_SEAT.getCode()))
            .andExpect(jsonPath("$.message").value(TrainResponseCode.EXIST_SEAT.getMessage()));

        Mockito.verify(cartStore, Mockito.never()).save(Mockito.any(), Mockito.anyLong());
        Mockito.verify(seatHoldStore, Mockito.never()).holdSeat(
            Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyLong()
        );
    }

    @Test
    @Transactional
    @DisplayName("같은 좌석 여러개 담을 경우")
    void add_cart_same_seat() throws Exception {
        //given
        CustomOAuth2User principal = Mockito.mock(CustomOAuth2User.class);
        Mockito.when(principal.getUserId()).thenReturn(1L);

        Authentication auth = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        TrainRun trainRun = trainRunRepository.findAll().get(0);
        TrainStop departure = trainStopRepository.findByTrainRunAndStation_Code(trainRun,
            "SEOUL").orElseThrow();
        TrainStop arrival = trainStopRepository.findByTrainRunAndStation_Code(trainRun,
            "BUSAN").orElseThrow();

        List<Seat> seats = seatRepository.findByCarriage_Train(trainRun.getTrain());
        Long seatId = seats.get(0).getId();

        int price = arrival.getCumulativeFare() - departure.getCumulativeFare();

        Mockito.when(cartStore.getOrCreate(1L)).thenReturn(new Cart());

        String body = objectMapper.writeValueAsString(Map.of(
            "seatInfoDTOs", List.of(
                Map.of("seatId", seatId, "price", price),
                Map.of("seatId", seatId, "price", price)
            ),
            "departureStopId", departure.getId(),
            "arrivalStopId", arrival.getId()
        ));

        //when & then
        mockMvc.perform(post("/api/v1/cart")
                .with(authentication(auth))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().is(TrainResponseCode.EXIST_SEAT.getCode()))
            .andExpect(jsonPath("$.message").value(TrainResponseCode.EXIST_SEAT.getMessage()));

        Mockito.verify(cartStore, Mockito.never()).save(Mockito.any(), Mockito.anyLong());
        Mockito.verify(seatHoldStore, Mockito.never()).holdSeat(
            Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyLong()
        );
    }

    @Test
    @Transactional
    @DisplayName("좌석 장바구니에 담기 성공")
    void add_cart_success() throws Exception {
        //given
        CustomOAuth2User principal = Mockito.mock(CustomOAuth2User.class);
        Mockito.when(principal.getUserId()).thenReturn(1L);

        Authentication auth = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        TrainRun trainRun = trainRunRepository.findAll().get(0);
        TrainStop departure = trainStopRepository.findByTrainRunAndStation_Code(trainRun,
            "SEOUL").orElseThrow();
        TrainStop arrival = trainStopRepository.findByTrainRunAndStation_Code(trainRun,
            "BUSAN").orElseThrow();

        List<Seat> seats = seatRepository.findByCarriage_Train(trainRun.getTrain());
        Long seatId1 = seats.get(0).getId();
        Long seatId2 = seats.get(1).getId();

        int price = arrival.getCumulativeFare() - departure.getCumulativeFare();

        Mockito.when(cartStore.getOrCreate(1L)).thenReturn(new Cart());

        String body = objectMapper.writeValueAsString(Map.of(
            "seatInfoDTOs", List.of(
                Map.of("seatId", seatId1, "price", price),
                Map.of("seatId", seatId2, "price", price)
            ),
            "departureStopId", departure.getId(),
            "arrivalStopId", arrival.getId()
        ));

        //when & then
        mockMvc.perform(post("/api/v1/cart")
                .with(authentication(auth))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk());

        Mockito.verify(cartStore, Mockito.times(2)).save(Mockito.any(Cart.class), Mockito.anyLong());

        Mockito.verify(seatHoldStore, Mockito.times(2)).holdSeat(
            Mockito.eq(trainRun.getId()),
            Mockito.anyLong(),
            Mockito.eq(departure.getStopOrder()),
            Mockito.eq(arrival.getStopOrder()),
            Mockito.anyLong()
        );
    }

    // ------------------------------------------------------

    @Test
    @DisplayName("장바구니 조회 - 장바구니 비어있을 경우")
    void get_cart_empty() throws Exception {
        //given
        CustomOAuth2User principal = Mockito.mock(CustomOAuth2User.class);
        Mockito.when(principal.getUserId()).thenReturn(1L);

        Authentication auth = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        Mockito.when(cartStore.getOrCreate(1L)).thenReturn(new Cart());

        //when & then
        mockMvc.perform(get("/api/v1/cart")
                .with(authentication(auth)))
            .andExpect(status().is(UserResponseCode.CART_SEAT_NOT_FOUND.getCode()))
            .andExpect(jsonPath("$.message").value(UserResponseCode.CART_SEAT_NOT_FOUND.getMessage()));

        Mockito.verify(cartStore, Mockito.times(1)).getOrCreate(1L);
    }

    @Test
    @Transactional
    @DisplayName("장바구니 조회 - 성공")
    void get_cart_success() throws Exception {
        //given
        CustomOAuth2User principal = Mockito.mock(CustomOAuth2User.class);
        Mockito.when(principal.getUserId()).thenReturn(1L);

        Authentication auth = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        TrainRun trainRun = trainRunRepository.findAll().get(0);
        TrainStop departure = trainStopRepository.findByTrainRunAndStation_Code(trainRun, "SEOUL")
            .orElseThrow();
        TrainStop arrival = trainStopRepository.findByTrainRunAndStation_Code(trainRun, "BUSAN")
            .orElseThrow();

        List<Seat> seats = seatRepository.findByCarriage_Train(trainRun.getTrain());
        Long seatId1 = seats.get(0).getId();
        Long seatId2 = seats.get(1).getId();

        int price = arrival.getCumulativeFare() - departure.getCumulativeFare();

        Cart cart = new Cart();
        cart.addItem(
            trainRun.getId(),
            seatId1,
            departure.getStation().getCode(),
            departure.getDepartureAt(),
            arrival.getStation().getCode(),
            arrival.getArrivalAt(),
            price
        );
        cart.addItem(
            trainRun.getId(),
            seatId2,
            departure.getStation().getCode(),
            departure.getDepartureAt(),
            arrival.getStation().getCode(),
            arrival.getArrivalAt(),
            price
        );

        Mockito.when(cartStore.getOrCreate(1L)).thenReturn(cart);

        //when & then
        mockMvc.perform(get("/api/v1/cart")
                .with(authentication(auth)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value(TrainResponseCode.TRAIN_FOUND.getMessage()))
            .andExpect(jsonPath("$.data.seats").isArray())
            .andExpect(jsonPath("$.data.seats.length()").value(2))
            .andExpect(jsonPath("$.data.seats[0].originCode").value("SEOUL"))
            .andExpect(jsonPath("$.data.seats[0].destinationCode").value("BUSAN"))
            .andExpect(jsonPath("$.data.seats[0].price").value(price));

        Mockito.verify(cartStore, Mockito.times(1)).getOrCreate(1L);
    }
}