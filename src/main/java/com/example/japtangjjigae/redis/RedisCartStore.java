package com.example.japtangjjigae.redis;

import com.example.japtangjjigae.cart.entity.Cart;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisCartStore extends AbstractRedisStore implements CartStore {

    private static final String KEY_PREFIX = "cart:user:";
    private final ObjectMapper objectMapper;

    public RedisCartStore(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        super(stringRedisTemplate);
        this.objectMapper = objectMapper;
    }

    @Override
    public Cart getOrCreate(Long userId) {
        String key = KEY_PREFIX + userId;

        Optional<String> jsonOpt = getValue(key);

        if (jsonOpt.isEmpty()) {
            return Cart.create(userId);
        }

        try {
            return objectMapper.readValue(jsonOpt.get(), Cart.class);

        } catch (JsonProcessingException e) {
            return Cart.create(userId);
        }
    }

    @Override
    public void save(Cart cart, long ttlSeconds) {
        String key = KEY_PREFIX + cart.getUserId();

        try {
            String json = objectMapper.writeValueAsString(cart);
            setValue(key, json, ttlSeconds);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("장바구니 직렬화 실패", e);
        }
    }

    @Override
    public void clear(Long userId) {
        String key = KEY_PREFIX + userId;
        deleteKey(key);
    }
}
