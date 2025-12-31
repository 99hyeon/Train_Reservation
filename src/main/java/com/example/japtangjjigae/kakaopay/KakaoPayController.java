package com.example.japtangjjigae.kakaopay;

import com.example.japtangjjigae.kakaopay.KakaoPayResponse.ApproveResponse;
import com.example.japtangjjigae.kakaopay.KakaoPayResponse.ReadyResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "카카오페이 API", description = "카카오페이 관련 API 모음")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/kakao-pay")
public class KakaoPayController {

    private final KakaoPayProvider kakaoPayProvider;

    @PostMapping("/ready")
    public ReadyResponse ready(@RequestBody KakaoPayRequestDTO request) {
        return kakaoPayProvider.ready(request);
    }

    @GetMapping("/approve")
    public ApproveResponse approve(@RequestParam("orderId") Long orderId,
        @RequestParam("pg_token") String pgToken) {

        return kakaoPayProvider.approve(orderId, pgToken);
    }
}
