package com.example.japtangjjigae.redis.seathold;

import com.example.japtangjjigae.redis.AbstractRedisStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class RedisSeatHoldStore extends AbstractRedisStore implements SeatHoldStore {

    private static final String KEY_PREFIX = "seat-hold:";
    private static final String INDEX_PREFIX = "seat-hold-index:";

    private static final String SEG_PREFIX = "seat-hold-seg:";

    private final DefaultRedisScript<Long> holdMultiScript = new DefaultRedisScript<>("""
          for i=1,#KEYS do
            if redis.call('EXISTS', KEYS[i]) == 1 then
              return 0
            end
          end
          for i=1,#KEYS do
            redis.call('SET', KEYS[i], ARGV[1], 'EX', ARGV[2])
          end
          return 1
        """, Long.class);

    public RedisSeatHoldStore(StringRedisTemplate stringRedisTemplate) {
        super(stringRedisTemplate);
    }

    @Override
    public boolean holdSeat(Long userId, Long trainRunId, List<Long> seatIds, int depOrder, int arrOrder,
        long ttlSeconds) {
        List<String> keys = new ArrayList<>();
        for (Long seatId : seatIds) {
            for (int seg = depOrder; seg < arrOrder; seg++) {
                keys.add(SEG_PREFIX + trainRunId + ":" + seatId + ":" + seg);
            }
        }
        Long result = stringRedisTemplate.execute(holdMultiScript, keys, String.valueOf(userId),
            String.valueOf(ttlSeconds));
        return result != null && result == 1L;
    }

    @Override
    public List<SeatHold> findOverLappingHolds(Long trainRunId, int requestDepartureOrder,
        int requestArrivalOrder) {
        String indexKey = INDEX_PREFIX + trainRunId;
        Set<String> seatIdStrs = getSetMembers(indexKey);
        if (seatIdStrs == null || seatIdStrs.isEmpty()) {
            return List.of();
        }

        List<String> seatKeys = seatIdStrs.stream()
            .map(seatIdStr -> KEY_PREFIX + trainRunId + ":" + seatIdStr)
            .toList();

        List<String> values = stringRedisTemplate.opsForValue().multiGet(seatKeys);

        List<SeatHold> holds = new ArrayList<>();
        List<String> expiredSeatIdsToRemove = new ArrayList<>();

        int i = 0;
        for (String seatIdStr : seatIdStrs) {
            String v = (values != null ? values.get(i) : null);
            i++;

            if (v == null) {
                expiredSeatIdsToRemove.add(seatIdStr);
                continue;
            }

            String[] parts = v.split(":");
            int depOrder = Integer.parseInt(parts[0]);
            int arrOrder = Integer.parseInt(parts[1]);

            if (depOrder < requestArrivalOrder && requestDepartureOrder < arrOrder) {
                holds.add(new SeatHold(Long.valueOf(seatIdStr), depOrder, arrOrder));
            }
        }

        if (!expiredSeatIdsToRemove.isEmpty()) {
            stringRedisTemplate.opsForSet().remove(indexKey, expiredSeatIdsToRemove.toArray());
        }

        return holds;
    }

    @Override
    public void releaseSeat(Long trainRunId, List<Long> seatIds, int depOrder, int arrOrder) {
        List<String> keys = new ArrayList<>();
        for (Long seatId : seatIds) {
            for (int seg = depOrder; seg < arrOrder; seg++) {
                keys.add(SEG_PREFIX + trainRunId + ":" + seatId + ":" + seg);
            }
        }
        stringRedisTemplate.delete(keys);
    }
}
