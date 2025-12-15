package com.example.japtangjjigae.redis.seathold;

import com.example.japtangjjigae.redis.AbstractRedisStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisSeatHoldStore extends AbstractRedisStore implements SeatHoldStore {

    private static final String KEY_PREFIX = "seat-hold:";
    private static final String INDEX_PREFIX = "seat-hold-index:";

    public RedisSeatHoldStore(StringRedisTemplate stringRedisTemplate) {
        super(stringRedisTemplate);
    }

    @Override
    public void holdSeat(Long trainRunId, Long seatId, int depOrder, int arrOrder,
        long ttlSeconds) {

        String seatKey = KEY_PREFIX + trainRunId + ":" + seatId;
        String indexKey = INDEX_PREFIX + trainRunId;
        String value = depOrder + ":" + arrOrder;

        // 좌석 개별 TTL
        setValue(seatKey, value, ttlSeconds);

        // 해당 열차의 인덱스에 seatId 추가 (TTL은 안 줘도 됨)
        addSetMember(indexKey, seatId.toString());
    }

    @Override
    public List<SeatHold> findOverLappingHolds(Long trainRunId, int requestDepartureOrder,
        int requestArrivalOrder) {

        String indexKey = INDEX_PREFIX + trainRunId;
        Set<String> seatIds = getSetMembers(indexKey);
        List<SeatHold> holds = new ArrayList<>();

        for (String seatIdStr : seatIds) {
            Long seatId = Long.valueOf(seatIdStr);
            String seatKey = KEY_PREFIX + trainRunId + ":" + seatIdStr;

            // 좌석별 TTL이 끝났으면 value가 null이라 여기서 자연스럽게 걸러짐
            Optional<String> valueOpt = getValue(seatKey);
            if (valueOpt.isEmpty()) {
                // 원하면 인덱스에서 정리도 가능 (고민중)
                // removeSetMember(indexKey, seatIdStr);
                continue;
            }

            String[] parts = valueOpt.get().split(":");
            int depOrder = Integer.parseInt(parts[0]);
            int arrOrder = Integer.parseInt(parts[1]);

            if (depOrder < requestArrivalOrder && requestDepartureOrder < arrOrder) {
                holds.add(new SeatHold(seatId, depOrder, arrOrder));
            }
        }

        return holds;
    }

    @Override
    public void releaseSeat(Long trainRunId, Long seatId) {
        String seatKey = KEY_PREFIX + trainRunId + ":" + seatId;
        String indexKey = INDEX_PREFIX + trainRunId;

        deleteKey(seatKey);
        removeSetMember(indexKey, seatId.toString());
    }
}
