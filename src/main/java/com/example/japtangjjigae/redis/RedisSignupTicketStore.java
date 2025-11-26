package com.example.japtangjjigae.redis;

import com.example.japtangjjigae.user.common.OAuthProvider;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisSignupTicketStore implements SignupTicketStore {

    private final StringRedisTemplate stringRedisTemplate;
    private static final String KEY = "signup:ticket:";

    @Override
    public void save(String ticket, SignupTicketValue value, long ttlSeconds) {
        String key = KEY + ticket;
        String payload = value.kakaoId() + ":" + value.provider();
        stringRedisTemplate.opsForValue().set(key, payload, Duration.ofSeconds(ttlSeconds));
    }

    @Override
    public Optional<SignupTicketValue> get(String ticket) {
        String s = stringRedisTemplate.opsForValue().get(KEY + ticket);

        if(s == null) return Optional.empty();

        String[] p = s.split(":", 2);

        return Optional.of(new SignupTicketValue(Long.parseLong(p[0]), OAuthProvider.valueOf(p[1])));
    }

    @Override
    public void invalidate(String ticket) {
        stringRedisTemplate.delete(KEY + ticket);
    }
}
