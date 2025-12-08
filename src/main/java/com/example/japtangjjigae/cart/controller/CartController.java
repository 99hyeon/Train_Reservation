package com.example.japtangjjigae.cart.controller;

import com.example.japtangjjigae.cart.dto.AddSeatToCartRequestDTO;
import com.example.japtangjjigae.cart.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "장바구니 API", description = "장바구니 관련 API 모음")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class CartController {

    private final CartService cartService;

    @Operation(
        summary = "장바구니에 좌석 담기 api"
    )
    @PostMapping("/cart")
    public ResponseEntity<String> addCart(@RequestBody
    AddSeatToCartRequestDTO requestDto) {

        cartService.addSeat(requestDto);

        return ResponseEntity.ok("장바구니에 좌석 담기 완료");
    }

    //todo: 장바구니 조회
}
