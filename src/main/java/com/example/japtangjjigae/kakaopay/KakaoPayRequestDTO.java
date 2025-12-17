package com.example.japtangjjigae.kakaopay;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoPayRequestDTO {

    private String itemName;
    private List<ItemInfo> itemInfos;


    @Getter
    @Builder
    public static class OrderRequest {

        String itemName;
        String quantity;
        String totalPrice;
    }

    @Getter
    @Builder
    public static class ItemInfo {
        private Long trainRunId;
        private Long departureStopId;
        private Long arrivalStopId;
        private Long seatId;
        private int price;
    }
}
