package com.example.japtangjjigae.redis.pay;

import com.example.japtangjjigae.redis.AbstractRedisStore;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisPayStore extends AbstractRedisStore implements PayStore {

    private static final String KEY_PREFIX = "kakaopay:tid:";

    public RedisPayStore(StringRedisTemplate stringRedisTemplate) {
        super(stringRedisTemplate);
    }

    @Override
    public void save(Long orderId, String tid, long ttlSeconds) {
        String key = KEY_PREFIX + orderId;
        String payload = tid;
        setValue(key, payload, ttlSeconds);
    }

    @Override
    public Optional<String> getTid(Long orderId) {
        return getValue(KEY_PREFIX + orderId);
    }

    @Override
    public void delete(Long orderId) {
        deleteKey(KEY_PREFIX + orderId);
    }
}
