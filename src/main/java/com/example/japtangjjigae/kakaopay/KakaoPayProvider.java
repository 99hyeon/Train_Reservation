package com.example.japtangjjigae.kakaopay;

import com.example.japtangjjigae.exception.SeatNotFoundException;
import com.example.japtangjjigae.exception.TrainNotFoundException;
import com.example.japtangjjigae.exception.UserNotFoundException;
import com.example.japtangjjigae.global.response.code.TrainResponseCode;
import com.example.japtangjjigae.global.response.code.UserResponseCode;
import com.example.japtangjjigae.kakaopay.KakaoPayRequestDTO.ItemInfo;
import com.example.japtangjjigae.kakaopay.KakaoPayResponse.ApproveResponse;
import com.example.japtangjjigae.kakaopay.KakaoPayResponse.ReadyResponse;
import com.example.japtangjjigae.order.Order;
import com.example.japtangjjigae.order.OrderRepository;
import com.example.japtangjjigae.redis.pay.PayStore;
import com.example.japtangjjigae.ticket.PayStatus;
import com.example.japtangjjigae.ticket.entity.Ticket;
import com.example.japtangjjigae.ticket.repository.TicketRepository;
import com.example.japtangjjigae.train.entity.Seat;
import com.example.japtangjjigae.train.entity.TrainRun;
import com.example.japtangjjigae.train.entity.TrainStop;
import com.example.japtangjjigae.train.repository.SeatRepository;
import com.example.japtangjjigae.train.repository.TrainRunRepository;
import com.example.japtangjjigae.train.repository.TrainStopRepository;
import com.example.japtangjjigae.user.entity.User;
import com.example.japtangjjigae.user.repository.UserRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

//todo: 예외처리들 수정 필요
@Component
@Transactional
@RequiredArgsConstructor
public class KakaoPayProvider {

    @Value("${kakaopay.secretKey}")
    private String secretKey;
    @Value("${kakaopay.cid}")
    private String cid;
    private static final long PAY_TTL_SECONDS = 60 * 15L; // 15분

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TrainRunRepository trainRunRepository;
    private final TrainStopRepository trainStopRepository;
    private final SeatRepository seatRepository;
    private final OrderRepository orderRepository;
    private final PayStore payStore;

    public ReadyResponse ready(Long userId, KakaoPayRequestDTO request) {
        Order order = createOrder(userId, request);
        Map<String, String> parameters = new HashMap<>();

        parameters.put("cid", cid); // 가맹점 코드, 테스트용은 TC0ONETIME
        parameters.put("partner_order_id", String.valueOf(order.getId())); // 주문번호, 임시 : 1234567890 // 이건 뭐임?
        parameters.put("partner_user_id", String.valueOf(userId)); // 회원아이디, 임시 : 1234567890  // 이것 또한 뭐임??
        parameters.put("item_name", request.getItemName()); // 상품명
        parameters.put("quantity", String.valueOf(request.getItemInfos().size())); // 상품 수량
        parameters.put("total_amount", String.valueOf(request.getItemInfos())); // 상품 총액 - 계산 필요
        parameters.put("tax_free_amount", "0"); // 상품 비과세 금액
        parameters.put("approval_url",
            "http://localhost:8080/api/v1/kakao-pay/approve?orderId=" + order.getId()); // 결제 성공 시 redirct URL
        parameters.put("cancel_url", "http://localhost:8080/api/v1/kakao-pay/cancel"); // 결제 취소 시
        parameters.put("fail_url", "http://localhost:8080/kakao-pay/fail"); // 결제 실패 시

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(parameters, getHeaders());

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://open-api.kakaopay.com/online/v1/payment/ready";
        ResponseEntity<ReadyResponse> response = restTemplate.postForEntity(url, entity,
            ReadyResponse.class);

        payStore.save(order.getId(), response.getBody().getTid(), PAY_TTL_SECONDS);
        return response.getBody();
    }

    private Order createOrder(Long userId, KakaoPayRequestDTO request) {
        User user = userRepository.findById(userId).orElseThrow(
            () -> new UserNotFoundException(UserResponseCode.USER_NOT_FOUND)
        );

        List<Ticket> ticketList = new ArrayList<>();
        for(ItemInfo itemInfo : request.getItemInfos()){
            TrainRun trainRun = trainRunRepository.findById(itemInfo.getTrainRunId()).orElseThrow(
                () -> new TrainNotFoundException(TrainResponseCode.TRAIN_NOT_FOUND)
            );
            TrainStop departureStop = trainStopRepository.findById(itemInfo.getDepartureStopId()).orElseThrow(
                () -> new TrainNotFoundException(TrainResponseCode.TRAIN_NOT_FOUND)
            );
            TrainStop arrivalStop = trainStopRepository.findById(itemInfo.getArrivalStopId()).orElseThrow(
                () -> new TrainNotFoundException(TrainResponseCode.TRAIN_NOT_FOUND)
            );
            Seat seat = seatRepository.findById(itemInfo.getSeatId()).orElseThrow(
                () -> new SeatNotFoundException(TrainResponseCode.MATCH_SEAT_NOT_FOUND)
            );

            Ticket newTicket = Ticket.createTicket(trainRun, departureStop, arrivalStop, seat,
                itemInfo.getPrice());

            ticketList.add(newTicket);
        }

        ticketRepository.saveAll(ticketList);
        Order order = Order.createOrder(user, ticketList, PayStatus.READY);
        return orderRepository.save(order);
    }

    public ApproveResponse approve(Long userId, Long orderId, String pgToken) {
        Order order = orderRepository.findById(orderId).orElse(null);

        Map<String, String> parameters = new HashMap<>();
        parameters.put("cid", cid);
        parameters.put("tid", String.valueOf(payStore.getTid(orderId)));   //null 처리 신경
        parameters.put("partner_order_id", String.valueOf(orderId));
        parameters.put("partner_user_id", String.valueOf(userId));
        parameters.put("pg_token", pgToken); // 결제승인 요청을 인증하는 토큰

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(parameters, getHeaders());

        RestTemplate restTemplate = new RestTemplate();
        String url = "https://open-api.kakaopay.com/online/v1/payment/approve";
        ResponseEntity<ApproveResponse> response = restTemplate.postForEntity(url, entity, ApproveResponse.class);

        order.statusSetApprove();
        payStore.delete(orderId);

        return response.getBody();
    }

    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "SECRET_KEY " + secretKey);
        headers.add("Content-type", "application/json");
        return headers;
    }
}
