package com.example.japtangjjigae.cart.service;

import com.example.japtangjjigae.cart.dto.AddSeatToCartRequestDTO;
import com.example.japtangjjigae.cart.dto.AddSeatToCartRequestDTO.SeatInfoDTO;
import com.example.japtangjjigae.exception.SeatConflictException;
import com.example.japtangjjigae.jwt.TokenUtil;
import com.example.japtangjjigae.redis.cart.CartStore;
import com.example.japtangjjigae.redis.seathold.SeatHoldStore;
import com.example.japtangjjigae.train.entity.Seat;
import com.example.japtangjjigae.train.entity.TrainRun;
import com.example.japtangjjigae.train.entity.TrainStop;
import com.example.japtangjjigae.train.repository.SeatRepository;
import com.example.japtangjjigae.train.repository.TrainRunRepository;
import com.example.japtangjjigae.train.repository.TrainStopRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
public class CartConcurrencyTest {

    @Autowired
    CartService cartService;

    @Autowired
    TrainRunRepository trainRunRepository;
    @Autowired
    TrainStopRepository trainStopRepository;
    @Autowired
    SeatRepository seatRepository;

    @Autowired
    SeatHoldStore seatHoldStore;
    @Autowired
    CartStore cartStore;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @MockitoBean
    TokenUtil tokenUtil;

    private static final ThreadLocal<Long> USER = new ThreadLocal<>();

    @BeforeEach
    void setup() {
        Mockito.when(tokenUtil.currentUserId())
            .thenAnswer(inv -> USER.get());
    }

    @Test
    @DisplayName("동시에 유저 2명이 같은 좌석 담는 요청 - 성공")
    void sameSeat_twoUsers_concurrently_onlyOneShouldSucceed() throws Exception {
        // given
        TrainRun trainRun = trainRunRepository.findAll().get(0);
        AddSeatToCartRequestDTO req = buildRequestWithSameSeat(trainRun);

        Long trainRunId = trainRun.getId();
        Long seatId = req.getSeatInfoDTOs().get(0).getSeatId();

        TrainStop departure = trainStopRepository.findByTrainRunAndStation_Code(trainRun, "SEOUL").orElseThrow();
        TrainStop arrival = trainStopRepository.findByTrainRunAndStation_Code(trainRun, "BUSAN").orElseThrow();

        List<Long> seatIds = List.of(seatId);
        stringRedisTemplate.getConnectionFactory().getConnection().flushDb();
        seatHoldStore.releaseSeat(trainRunId, seatIds, departure.getStopOrder(),
            arrival.getStopOrder());
        cartStore.clear(1L);
        cartStore.clear(2L);

        int threadCount = 2;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();

        var errors = new CopyOnWriteArrayList<Throwable>();

        Future<?> f1 = pool.submit(() -> run(1L, req, start, done, success, conflict, errors));
        Future<?> f2 = pool.submit(() -> run(2L, req, start, done, success, conflict, errors));

        // when
        start.countDown();
        done.await();

        try {
            f1.get();
            f2.get();
        } catch (ExecutionException e) {
            errors.add(e.getCause());
        }

        pool.shutdownNow();

        if (!errors.isEmpty()) {
            errors.forEach(Throwable::printStackTrace);
            org.assertj.core.api.Assertions.fail("워커 스레드에서 예외 발생. 스택트레이스 확인!");
        }

        // then
        org.assertj.core.api.Assertions.assertThat(success.get()).isEqualTo(1);
        org.assertj.core.api.Assertions.assertThat(conflict.get()).isEqualTo(1);
    }

    private void run(
        Long userId,
        AddSeatToCartRequestDTO req,
        CountDownLatch start,
        CountDownLatch done,
        AtomicInteger success,
        AtomicInteger conflict,
        CopyOnWriteArrayList<Throwable> errors
    ) {
        try {
            USER.set(userId);
            start.await();

            cartService.addSeat(req);
            success.incrementAndGet();

        } catch (SeatConflictException e) {
            conflict.incrementAndGet();

        } catch (Throwable t) {
            errors.add(t);

        } finally {
            USER.remove();
            done.countDown();
        }
    }

    @Test
    @DisplayName("동시에 유저 2명이 같은 좌석 담기 - 구간 경계선으로 안 겹침 - 둘 다 성공(success=2)")
    void sameSeat_twoUsers_concurrently_nonOverlappingSegments_bothShouldSucceed() throws Exception {
        // given
        TrainRun trainRun = trainRunRepository.findAll().get(0);

        List<Seat> seats = seatRepository.findByCarriage_Train(trainRun.getTrain());
        Long seatId = seats.get(0).getId();
        Long trainRunId = trainRun.getId();
        List<Long> seatIds = List.of(seatId);

        AddSeatToCartRequestDTO req1 = buildRequestWithSameSeatAndRoute(trainRun, seatId, "SEOUL", "DAEJEON");
        AddSeatToCartRequestDTO req2 = buildRequestWithSameSeatAndRoute(trainRun, seatId, "DAEJEON", "BUSAN");

        TrainStop dep1 = trainStopRepository.findByTrainRunAndStation_Code(trainRun, "SEOUL").orElseThrow();
        TrainStop arr1 = trainStopRepository.findByTrainRunAndStation_Code(trainRun, "DAEJEON").orElseThrow();
        TrainStop dep2 = arr1;
        TrainStop arr2 = trainStopRepository.findByTrainRunAndStation_Code(trainRun, "BUSAN").orElseThrow();

        stringRedisTemplate.getConnectionFactory().getConnection().flushDb();

        seatHoldStore.releaseSeat(trainRunId, seatIds, dep1.getStopOrder(), arr1.getStopOrder());
        seatHoldStore.releaseSeat(trainRunId, seatIds, dep2.getStopOrder(), arr2.getStopOrder());

        cartStore.clear(1L);
        cartStore.clear(2L);

        int threadCount = 2;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);

        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();
        var errors = new CopyOnWriteArrayList<Throwable>();

        Future<?> f1 = pool.submit(() -> run(1L, req1, start, done, success, conflict, errors));
        Future<?> f2 = pool.submit(() -> run(2L, req2, start, done, success, conflict, errors));

        // when
        start.countDown();
        done.await();

        try {
            f1.get();
            f2.get();
        } catch (ExecutionException e) {
            errors.add(e.getCause());
        }

        pool.shutdownNow();

        if (!errors.isEmpty()) {
            errors.forEach(Throwable::printStackTrace);
            org.assertj.core.api.Assertions.fail("워커 스레드에서 예외 발생. 스택트레이스 확인!");
        }

        // then
        org.assertj.core.api.Assertions.assertThat(success.get()).isEqualTo(2);
        org.assertj.core.api.Assertions.assertThat(conflict.get()).isEqualTo(0);
    }

    private AddSeatToCartRequestDTO buildRequestWithSameSeat(TrainRun trainRun) {
        // TODO: departureStopId/arrivalStopId/seatId/price 세팅
        TrainStop departure = trainStopRepository.findByTrainRunAndStation_Code(trainRun, "SEOUL").orElseThrow();
        TrainStop arrival = trainStopRepository.findByTrainRunAndStation_Code(trainRun, "BUSAN").orElseThrow();

        List<Seat> seats = seatRepository.findByCarriage_Train(trainRun.getTrain());
        Long seatId = seats.get(0).getId();

        int price = arrival.getCumulativeFare() - departure.getCumulativeFare();

        SeatInfoDTO seatInfo = SeatInfoDTO.builder()
            .seatId(seatId)
            .price(price)
            .build();

        List<SeatInfoDTO> list = new ArrayList<>();
        list.add(seatInfo);

        return new AddSeatToCartRequestDTO(list, departure.getId(), arrival.getId());
    }

    private AddSeatToCartRequestDTO buildRequestWithSameSeatAndRoute(
        TrainRun trainRun,
        Long seatId,
        String departureStationCode,
        String arrivalStationCode
    ) {
        TrainStop departure = trainStopRepository
            .findByTrainRunAndStation_Code(trainRun, departureStationCode)
            .orElseThrow();

        TrainStop arrival = trainStopRepository
            .findByTrainRunAndStation_Code(trainRun, arrivalStationCode)
            .orElseThrow();

        int price = arrival.getCumulativeFare() - departure.getCumulativeFare();

        SeatInfoDTO seatInfo = SeatInfoDTO.builder()
            .seatId(seatId)
            .price(price)
            .build();

        List<SeatInfoDTO> list = new ArrayList<>();
        list.add(seatInfo);

        return new AddSeatToCartRequestDTO(list, departure.getId(), arrival.getId());
    }

}
